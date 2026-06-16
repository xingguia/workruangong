"""Admin panel API routes."""
from fastapi import APIRouter, HTTPException, Header, Depends, Body
from pydantic import BaseModel
from database import get_db
from auth import hash_password
from config import SECRET_KEY, ALGORITHM
import jwt
from datetime import datetime, timedelta
import time

router = APIRouter(prefix="/api/v1/admin", tags=["admin"])

# ---- Schemas ----

class LoginRequest(BaseModel):
    username: str
    password: str

# ---- Admin Auth ----

ADMIN_TOKEN_EXPIRE_HOURS = 24


def create_admin_token(admin_id: int, role: str) -> str:
    expire = datetime.utcnow() + timedelta(hours=ADMIN_TOKEN_EXPIRE_HOURS)
    payload = {"sub": str(admin_id), "role": role, "admin": True, "exp": expire}
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def get_admin(authorization: str = Header(...)):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization")
    try:
        payload = jwt.decode(authorization[7:], SECRET_KEY, algorithms=[ALGORITHM])
        if not payload.get("admin"):
            raise HTTPException(status_code=403, detail="Admin access required")
        return {"id": int(payload["sub"]), "role": payload["role"]}
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Invalid token")


@router.post("/login")
def admin_login(body: LoginRequest = Body(None), username: str = None, password: str = None):
    # Support both JSON body and query params
    uname = body.username if body else username
    pwd = body.password if body else password
    if not uname or not pwd:
        raise HTTPException(status_code=400, detail="Username and password are required")
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, password_hash, role FROM admins WHERE username=%s AND is_active=1", (uname,))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=401, detail="Invalid credentials")

        stored_hash = row[1]
        if stored_hash.startswith("$2b$") or stored_hash.startswith("$2a$"):
            import bcrypt
            if not bcrypt.checkpw(pwd.encode(), stored_hash.encode()):
                raise HTTPException(status_code=401, detail="Invalid credentials")
        else:
            if hash_password(pwd) != stored_hash:
                raise HTTPException(status_code=401, detail="Invalid credentials")

        token = create_admin_token(row[0], row[2])
        cur.execute("UPDATE admins SET last_login=NOW() WHERE id=%s", (row[0],))
        return {"access_token": token, "role": row[2], "username": uname}


@router.get("/me")
def admin_me(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, username, role, created_at, last_login FROM admins WHERE id=%s", (admin["id"],))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="Admin not found")
        return {
            "id": row[0], "username": row[1], "role": row[2],
            "created_at": str(row[3]) if row[3] else None,
            "last_login": str(row[4]) if row[4] else None,
        }


# ---- Dashboard ----

@router.get("/dashboard")
def dashboard(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        now = datetime.utcnow()
        today_start = now.replace(hour=0, minute=0, second=0, microsecond=0)
        week_ago = now - timedelta(days=7)
        day_30_start = (now - timedelta(days=29)).replace(hour=0, minute=0, second=0, microsecond=0)

        # Single query for all stats
        cur.execute("""
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN created_at >= %s THEN 1 ELSE 0 END) as today_new,
                SUM(CASE WHEN created_at >= %s THEN 1 ELSE 0 END) as week_new,
                SUM(CASE WHEN is_vip=1 THEN 1 ELSE 0 END) as vip_users
            FROM users
        """, (today_start.strftime("%Y-%m-%d %H:%M:%S"), week_ago.strftime("%Y-%m-%d %H:%M:%S")))
        row = cur.fetchone()
        total_users, today_new, week_new, vip_users = row[0], row[1], row[2], row[3]

        # User growth trend — single GROUP BY query
        cur.execute("""
            SELECT DATE(created_at) as dt, COUNT(*) as cnt
            FROM users WHERE created_at >= %s
            GROUP BY DATE(created_at) ORDER BY dt
        """, (day_30_start.strftime("%Y-%m-%d"),))
        day_counts = {str(r[0]): r[1] for r in cur.fetchall()}

        # Running total for trend
        cur.execute("SELECT COUNT(*) FROM users WHERE created_at < %s", (day_30_start.strftime("%Y-%m-%d"),))
        running_total = cur.fetchone()[0]
        user_trend = []
        for i in range(29, -1, -1):
            day = now - timedelta(days=i)
            ds = day.strftime("%Y-%m-%d")
            dc = day_counts.get(ds, 0)
            running_total += dc
            user_trend.append({
                "date": day.strftime("%m-%d"),
                "new_users": dc,
                "total_users": running_total,
            })

        # Active trend — single GROUP BY query
        cur.execute("""
            SELECT DATE(FROM_UNIXTIME(timestamp/1000)) as dt, COUNT(DISTINCT user_id) as cnt
            FROM workout_records
            WHERE timestamp >= %s AND timestamp <= %s
            GROUP BY DATE(FROM_UNIXTIME(timestamp/1000)) ORDER BY dt
        """, (
            int((now - timedelta(days=6)).replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000),
            int(now.replace(hour=23, minute=59, second=59, microsecond=999999).timestamp() * 1000)
        ))
        active_map = {str(r[0]): r[1] for r in cur.fetchall()}
        active_trend = []
        for i in range(6, -1, -1):
            day = now - timedelta(days=i)
            ds = day.strftime("%Y-%m-%d")
            active_trend.append({
                "date": day.strftime("%m-%d"),
                "active_users": active_map.get(ds, 0),
            })

        # Training type distribution
        cur.execute("SELECT exercise_name, COUNT(*) as cnt FROM workout_records GROUP BY exercise_name ORDER BY cnt DESC LIMIT 10")
        training_types = []
        total_records = 0
        type_data = cur.fetchall()
        for row in type_data:
            total_records += row[1]
        for row in type_data:
            training_types.append({
                "name": row[0],
                "count": row[1],
                "percent": round(row[1] / total_records * 100, 1) if total_records > 0 else 0,
            })

        # Recent users
        cur.execute("SELECT id, nickname, phone, created_at, is_vip FROM users ORDER BY created_at DESC LIMIT 5")
        recent_users = []
        for row in cur.fetchall():
            phone = row[2] or ""
            masked_phone = phone[:3] + "****" + phone[-4:] if len(phone) >= 7 else phone
            recent_users.append({
                "id": row[0], "nickname": row[1], "phone": masked_phone,
                "created_at": str(row[3]) if row[3] else "",
                "is_vip": bool(row[4]),
            })

        # Latest records
        cur.execute(
            "SELECT wr.id, u.nickname, wr.exercise_name, wr.duration, wr.calories_burned, wr.timestamp "
            "FROM workout_records wr JOIN users u ON wr.user_id=u.id "
            "ORDER BY wr.timestamp DESC LIMIT 5"
        )
        latest_records = []
        for row in cur.fetchall():
            ts = row[5]
            record_time = ""
            if ts:
                dt = datetime.fromtimestamp(ts / 1000)
                record_time = dt.strftime("%H:%M")
            latest_records.append({
                "id": row[0], "nickname": row[1], "exercise_name": row[2],
                "duration": row[3], "calories": row[4], "time": record_time,
            })

        return {
            "total_users": total_users,
            "today_new": today_new,
            "week_new": week_new,
            "vip_users": vip_users,
            "user_trend": user_trend,
            "active_trend": active_trend,
            "training_types": training_types,
            "recent_users": recent_users,
            "latest_records": latest_records,
        }


# ---- User Management ----

@router.get("/users")
def admin_users(page: int = 1, page_size: int = 20, keyword: str = "",
                vip_status: str = "", date_from: str = "", date_to: str = "",
                admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        where_clauses = []
        params = []

        if keyword:
            where_clauses.append("(u.phone LIKE %s OR u.nickname LIKE %s)")
            kw = f"%{keyword}%"
            params.extend([kw, kw])

        if vip_status == "vip":
            where_clauses.append("u.is_vip = 1")
        elif vip_status == "normal":
            where_clauses.append("u.is_vip = 0")

        if date_from:
            where_clauses.append("u.created_at >= %s")
            params.append(date_from)

        if date_to:
            where_clauses.append("u.created_at <= %s")
            params.append(date_to + " 23:59:59")

        where = " AND ".join(where_clauses) if where_clauses else "1=1"

        cur.execute(f"SELECT COUNT(*) FROM users u WHERE {where}", params)
        total = cur.fetchone()[0]

        offset = (page - 1) * page_size
        cur.execute(
            f"SELECT u.id, u.phone, u.nickname, u.fitness_goal, u.is_vip, u.created_at, "
            f"(SELECT MAX(timestamp) FROM workout_records WHERE user_id=u.id) as last_active "
            f"FROM users u WHERE {where} ORDER BY u.created_at DESC LIMIT %s OFFSET %s",
            params + [page_size, offset]
        )

        users = []
        for row in cur.fetchall():
            phone = row[1] or ""
            users.append({
                "id": row[0],
                "phone": phone,
                "phone_masked": phone[:3] + "****" + phone[-4:] if len(phone) >= 7 else phone,
                "nickname": row[2],
                "fitness_goal": row[3] or "",
                "is_vip": bool(row[4]),
                "created_at": str(row[5]) if row[5] else "",
                "last_active": str(datetime.fromtimestamp(row[6] / 1000)) if row[6] else "无记录",
            })

        return {"total": total, "page": page, "page_size": page_size, "list": users}


@router.get("/users/{user_id}")
def admin_user_detail(user_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM users WHERE id=%s", (user_id,))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="User not found")
        cols = [d[0] for d in cur.description]
        user = dict(zip(cols, row))

        cur.execute("SELECT COUNT(*), SUM(calories_burned), SUM(duration) FROM workout_records WHERE user_id=%s", (user_id,))
        stats = cur.fetchone()

        cur.execute("SELECT * FROM workout_records WHERE user_id=%s ORDER BY timestamp DESC LIMIT 20", (user_id,))
        records = []
        for r in cur.fetchall():
            ts = r[4]
            record_time = str(datetime.fromtimestamp(ts / 1000)) if ts else ""
            records.append({
                "id": r[0], "exercise_name": r[3], "timestamp": record_time,
                "duration": r[5], "sets": r[6], "reps": r[7], "weight": r[8],
                "calories_burned": r[9], "muscle_group": r[12],
            })

        return {
            "id": user["id"], "phone": user["phone"], "nickname": user["nickname"],
            "avatar": user.get("avatar"), "gender": user.get("gender"),
            "fitness_goal": user.get("fitness_goal"),
            "height": user.get("height", 0), "weight": user.get("weight", 0),
            "body_fat": user.get("body_fat", 0), "waist": user.get("waist", 0), "hip": user.get("hip", 0),
            "is_vip": bool(user.get("is_vip", 0)), "level": user.get("level", 1),
            "vip_expire_time": str(user["vip_expire_time"]) if user.get("vip_expire_time") else None,
            "created_at": str(user["created_at"]) if user.get("created_at") else "",
            "total_workouts": stats[0] or 0,
            "total_calories": stats[1] or 0,
            "total_minutes": stats[2] or 0,
            "records": records,
        }


class UserProfileUpdate(BaseModel):
    nickname: str = None
    gender: str = None
    fitness_goal: str = None
    height: int = None
    weight: float = None
    body_fat: float = None
    waist: float = None
    hip: float = None

@router.put("/users/{user_id}")
def admin_update_user(user_id: int, body: UserProfileUpdate, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        fields = []
        values = []
        if body.nickname is not None:
            fields.append("nickname=%s"); values.append(body.nickname)
        if body.gender is not None:
            fields.append("gender=%s"); values.append(body.gender)
        if body.fitness_goal is not None:
            fields.append("fitness_goal=%s"); values.append(body.fitness_goal)
        if body.height is not None:
            fields.append("height=%s"); values.append(body.height)
        if body.weight is not None:
            fields.append("weight=%s"); values.append(body.weight)
        if body.body_fat is not None:
            fields.append("body_fat=%s"); values.append(body.body_fat)
        if body.waist is not None:
            fields.append("waist=%s"); values.append(body.waist)
        if body.hip is not None:
            fields.append("hip=%s"); values.append(body.hip)
        if not fields:
            raise HTTPException(status_code=400, detail="No fields to update")
        values.append(user_id)
        cur.execute(f"UPDATE users SET {','.join(fields)} WHERE id=%s", values)
        return {"ok": True}


@router.put("/users/{user_id}/vip")
def admin_toggle_vip(user_id: int, is_vip: bool, expire_time: str = None,
                     admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        if is_vip and expire_time:
            cur.execute("UPDATE users SET is_vip=1, vip_expire_time=%s WHERE id=%s", (expire_time, user_id))
        else:
            cur.execute("UPDATE users SET is_vip=%s WHERE id=%s", (1 if is_vip else 0, user_id))
        return {"ok": True}


@router.put("/users/{user_id}/ban")
def admin_ban_user(user_id: int, banned: bool = True,
                   admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET is_active=%s WHERE id=%s", (0 if banned else 1, user_id))
        return {"ok": True}


class PasswordChange(BaseModel):
    new_password: str

@router.put("/users/{user_id}/password")
def admin_change_password(user_id: int, body: PasswordChange, admin: dict = Depends(get_admin)):
    if len(body.new_password) < 6:
        raise HTTPException(status_code=400, detail="密码长度不能少于6位")
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET password_hash=%s WHERE id=%s",
                    (hash_password(body.new_password), user_id))
        return {"ok": True}


# ---- Training Data ----

@router.get("/workout-records")
def admin_workout_records(page: int = 1, page_size: int = 20, keyword: str = "",
                          date_from: str = "", date_to: str = "",
                          admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        where_clauses = []
        params = []

        if keyword:
            where_clauses.append("(u.nickname LIKE %s OR wr.exercise_name LIKE %s)")
            kw = f"%{keyword}%"
            params.extend([kw, kw])

        if date_from:
            date_from_ts = int(datetime.strptime(date_from, "%Y-%m-%d").timestamp() * 1000)
            where_clauses.append("wr.timestamp >= %s")
            params.append(date_from_ts)

        if date_to:
            date_to_ts = int(datetime.strptime(date_to, "%Y-%m-%d").replace(hour=23, minute=59, second=59).timestamp() * 1000)
            where_clauses.append("wr.timestamp <= %s")
            params.append(date_to_ts)

        where = " AND ".join(where_clauses) if where_clauses else "1=1"

        cur.execute(f"SELECT COUNT(*) FROM workout_records wr JOIN users u ON wr.user_id=u.id WHERE {where}", params)
        total = cur.fetchone()[0]

        offset = (page - 1) * page_size
        cur.execute(
            f"SELECT wr.id, u.nickname, wr.exercise_name, wr.muscle_group, wr.sets, wr.reps, "
            f"wr.weight, wr.calories_burned, wr.timestamp "
            f"FROM workout_records wr JOIN users u ON wr.user_id=u.id "
            f"WHERE {where} ORDER BY wr.timestamp DESC LIMIT %s OFFSET %s",
            params + [page_size, offset]
        )

        records = []
        for row in cur.fetchall():
            ts = row[8]
            record_time = str(datetime.fromtimestamp(ts / 1000)) if ts else ""
            records.append({
                "id": row[0], "nickname": row[1], "exercise_name": row[2],
                "muscle_group": row[3] or "", "sets": row[4], "reps": row[5],
                "weight": row[6], "calories": row[7], "time": record_time,
            })

        cur.execute("SELECT COUNT(*) FROM workout_records")
        total_records = cur.fetchone()[0]

        today_start_ts = int(datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
        cur.execute("SELECT COUNT(*) FROM workout_records WHERE timestamp >= %s", (today_start_ts,))
        today_records = cur.fetchone()[0]

        cur.execute("SELECT AVG(calories_burned) FROM workout_records")
        avg_calories = cur.fetchone()[0] or 0

        return {
            "total": total, "page": page, "page_size": page_size,
            "list": records,
            "summary": {
                "total_records": total_records,
                "today_records": today_records,
                "avg_calories": round(avg_calories, 1),
            }
        }


@router.get("/training-tasks")
def admin_training_tasks(page: int = 1, page_size: int = 20, keyword: str = "",
                         status: str = "", date_from: str = "", date_to: str = "",
                         admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        where_clauses = []
        params = []

        if keyword:
            where_clauses.append("(u.nickname LIKE %s OR tt.name LIKE %s)")
            kw = f"%{keyword}%"
            params.extend([kw, kw])

        if status:
            where_clauses.append("tt.status = %s")
            params.append(status)

        if date_from:
            date_from_ts = int(datetime.strptime(date_from, "%Y-%m-%d").timestamp() * 1000)
            where_clauses.append("tt.date >= %s")
            params.append(date_from_ts)

        if date_to:
            date_to_ts = int(datetime.strptime(date_to, "%Y-%m-%d").replace(hour=23, minute=59, second=59).timestamp() * 1000)
            where_clauses.append("tt.date <= %s")
            params.append(date_to_ts)

        where = " AND ".join(where_clauses) if where_clauses else "1=1"

        cur.execute(f"SELECT COUNT(*) FROM training_tasks tt JOIN users u ON tt.user_id=u.id WHERE {where}", params)
        total = cur.fetchone()[0]

        offset = (page - 1) * page_size
        cur.execute(
            f"SELECT tt.id, u.nickname, tt.name, tt.date, tt.status, tt.exercise_type, "
            f"tt.sets, tt.reps, tt.created_at "
            f"FROM training_tasks tt JOIN users u ON tt.user_id=u.id "
            f"WHERE {where} ORDER BY tt.created_at DESC LIMIT %s OFFSET %s",
            params + [page_size, offset]
        )

        tasks = []
        for row in cur.fetchall():
            dt = datetime.fromtimestamp(row[3] / 1000) if row[3] else None
            tasks.append({
                "id": row[0], "nickname": row[1], "name": row[2],
                "date": dt.strftime("%Y-%m-%d") if dt else "",
                "status": row[4], "exercise_type": row[5],
                "sets_reps": f"{row[6]}组×{row[7]}次" if row[6] and row[7] else "",
                "created_at": str(row[8]) if row[8] else "",
            })

        return {"total": total, "page": page, "page_size": page_size, "list": tasks}


# ---- Statistics ----

@router.get("/statistics/users")
def statistics_users(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        cur.execute("SELECT COALESCE(gender,'男') as gender, COUNT(*) FROM users GROUP BY gender")
        gender_data = [{"name": r[0], "value": r[1]} for r in cur.fetchall()]

        cur.execute("SELECT CASE WHEN fitness_goal IS NULL OR fitness_goal='' THEN '未设置' ELSE fitness_goal END as goal, COUNT(*) FROM users GROUP BY goal")
        goal_data = [{"name": r[0], "value": r[1]} for r in cur.fetchall()]

        cur.execute("SELECT COUNT(*) FROM users")
        total = cur.fetchone()[0]

        return {
            "gender_data": gender_data,
            "goal_data": goal_data,
            "total_users": total,
        }


@router.get("/statistics/training")
def statistics_training(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()

        cur.execute("SELECT exercise_name, COUNT(*) as cnt FROM workout_records GROUP BY exercise_name ORDER BY cnt DESC LIMIT 10")
        top_exercises = [{"name": r[0], "count": r[1]} for r in cur.fetchall()]

        cur.execute("SELECT exercise_type, COUNT(*) as cnt FROM training_tasks GROUP BY exercise_type ORDER BY cnt DESC")
        type_data = [{"name": r[0], "value": r[1]} for r in cur.fetchall()]

        return {
            "top_exercises": top_exercises,
            "type_distribution": type_data,
        }


# ---- Admin Account Management ----

@router.get("/admins")
def admin_list(admin: dict = Depends(get_admin)):
    if admin["role"] != "super":
        raise HTTPException(status_code=403, detail="Super admin only")
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, username, role, is_active, created_at, last_login FROM admins ORDER BY created_at DESC")
        admins = []
        for row in cur.fetchall():
            admins.append({
                "id": row[0], "username": row[1], "role": row[2],
                "is_active": bool(row[3]),
                "created_at": str(row[4]) if row[4] else "",
                "last_login": str(row[5]) if row[5] else "",
            })
        return {"list": admins}


@router.post("/admins")
def admin_create(username: str, password: str, role: str = "normal",
                 admin: dict = Depends(get_admin)):
    if admin["role"] != "super":
        raise HTTPException(status_code=403, detail="Super admin only")
    with get_db() as conn:
        cur = conn.cursor()

        cur.execute("SELECT id FROM admins WHERE username=%s", (username,))
        if cur.fetchone():
            raise HTTPException(status_code=400, detail="Username already exists")

        try:
            import bcrypt
            pwd_hash = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
        except ImportError:
            pwd_hash = hash_password(password)

        cur.execute(
            "INSERT INTO admins (username, password_hash, role) VALUES (%s,%s,%s)",
            (username, pwd_hash, role)
        )
        return {"ok": True, "id": cur.lastrowid}


@router.delete("/admins/{admin_id}")
def admin_delete(admin_id: int, admin: dict = Depends(get_admin)):
    if admin["role"] != "super":
        raise HTTPException(status_code=403, detail="Super admin only")
    if admin_id == admin["id"]:
        raise HTTPException(status_code=400, detail="Cannot delete yourself")
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM admins WHERE id=%s", (admin_id,))
        return {"ok": True}


# ---- Exercise Management ----

@router.get("/exercises")
def exercise_list(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, name, muscle_group, exercise_type, cal_per_rep, description, created_at FROM exercises ORDER BY created_at DESC")
        exercises = []
        for row in cur.fetchall():
            exercises.append({
                "id": row[0], "name": row[1], "muscle_group": row[2],
                "exercise_type": row[3], "cal_per_rep": float(row[4] or 0),
                "description": row[5], "created_at": str(row[6]) if row[6] else "",
            })
        return {"list": exercises}


@router.get("/exercises/{exercise_id}")
def exercise_get(exercise_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, name, muscle_group, exercise_type, cal_per_rep, description FROM exercises WHERE id=%s", (exercise_id,))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="Exercise not found")
        return {
            "id": row[0], "name": row[1], "muscle_group": row[2],
            "exercise_type": row[3], "cal_per_rep": float(row[4] or 0), "description": row[5],
        }


class ExerciseBody(BaseModel):
    name: str
    muscle_group: str = ""
    exercise_type: str = "STRENGTH"
    cal_per_rep: float = 0
    description: str = ""


@router.post("/exercises")
def exercise_create(body: ExerciseBody, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO exercises (name, muscle_group, exercise_type, cal_per_rep, description) VALUES (%s,%s,%s,%s,%s)",
            (body.name, body.muscle_group, body.exercise_type, body.cal_per_rep, body.description)
        )
        return {"ok": True, "id": cur.lastrowid}


@router.put("/exercises/{exercise_id}")
def exercise_update(exercise_id: int, body: ExerciseBody, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "UPDATE exercises SET name=%s, muscle_group=%s, exercise_type=%s, cal_per_rep=%s, description=%s WHERE id=%s",
            (body.name, body.muscle_group, body.exercise_type, body.cal_per_rep, body.description, exercise_id)
        )
        return {"ok": True}


@router.delete("/exercises/{exercise_id}")
def exercise_delete(exercise_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM exercises WHERE id=%s", (exercise_id,))
        return {"ok": True}


# ---- Achievement / Badge Management ----

@router.get("/achievements")
def achievement_list(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("""
            SELECT achievement_type, COUNT(*) as user_count,
                   SUM(unlocked) as unlocked_count
            FROM achievements GROUP BY achievement_type ORDER BY user_count DESC
        """)
        badges = []
        badge_icons = {
            "first_workout": "🏋️", "week_streak": "🔥", "month_streak": "💪",
            "calorie_1000": "⚡", "calorie_5000": "🌟", "calorie_10000": "👑",
            "level_5": "📈", "level_10": "🎯", "vip_member": "💎",
        }
        for row in cur.fetchall():
            atype = row[0]
            badges.append({
                "type": atype,
                "name": atype.replace("_", " ").title(),
                "icon": badge_icons.get(atype, "🏅"),
                "user_count": row[1],
                "unlocked_count": row[2] or 0,
                "description": f"达成 {atype.replace('_', ' ')} 成就",
            })
        return {"list": badges}


# ---- Announcement Management ----

class AnnouncementBody(BaseModel):
    title: str
    content: str = ""
    is_active: bool = True


@router.get("/announcements")
def announcement_list(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, title, content, is_active, created_at FROM announcements ORDER BY created_at DESC")
        announcements = []
        for row in cur.fetchall():
            announcements.append({
                "id": row[0], "title": row[1], "content": row[2] or "",
                "is_active": bool(row[3]), "created_at": str(row[4]) if row[4] else "",
            })
        return {"list": announcements}


@router.get("/announcements/{ann_id}")
def announcement_get(ann_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, title, content, is_active FROM announcements WHERE id=%s", (ann_id,))
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="Announcement not found")
        return {"id": row[0], "title": row[1], "content": row[2] or "", "is_active": bool(row[3])}


@router.post("/announcements")
def announcement_create(body: AnnouncementBody, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO announcements (title, content, is_active) VALUES (%s,%s,%s)",
            (body.title, body.content, body.is_active)
        )
        return {"ok": True, "id": cur.lastrowid}


@router.put("/announcements/{ann_id}")
def announcement_update(ann_id: int, body: AnnouncementBody, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "UPDATE announcements SET title=%s, content=%s, is_active=%s WHERE id=%s",
            (body.title, body.content, body.is_active, ann_id)
        )
        return {"ok": True}


@router.delete("/announcements/{ann_id}")
def announcement_delete(ann_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM announcements WHERE id=%s", (ann_id,))
        return {"ok": True}


# ---- Additional Statistics ----

@router.get("/statistics/equipment")
def statistics_equipment(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        # Try to get equipment data from user profiles or a dedicated table
        equipment_data = []
        try:
            cur.execute("SELECT fitness_goal, COUNT(*) FROM users WHERE fitness_goal IS NOT NULL AND fitness_goal != '' GROUP BY fitness_goal")
            for row in cur.fetchall():
                equipment_data.append({"name": row[0], "value": row[1]})
        except Exception:
            pass
        return {"equipment_data": equipment_data}


@router.get("/statistics/retention")
def statistics_retention(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        retention_data = []
        try:
            # Calculate day-1, day-3, day-7, day-14, day-30 retention
            cur.execute("SELECT COUNT(*) FROM users")
            total = cur.fetchone()[0]
            if total > 0:
                intervals = [(1, "次日"), (3, "3日"), (7, "7日"), (14, "14日"), (30, "30日")]
                for days, label in intervals:
                    cur.execute("""
                        SELECT COUNT(DISTINCT u.id) FROM users u
                        WHERE EXISTS (
                            SELECT 1 FROM workout_records wr
                            WHERE wr.user_id = u.id
                            AND wr.timestamp >= UNIX_TIMESTAMP(u.created_at) * 1000
                            AND wr.timestamp <= (UNIX_TIMESTAMP(u.created_at) + %s * 86400) * 1000
                        )
                    """, (days,))
                    retained = cur.fetchone()[0]
                    retention_data.append({"label": label, "rate": round(retained / total * 100, 1)})
        except Exception:
            pass
        return {"retention_data": retention_data}


@router.get("/statistics/vip")
def statistics_vip(admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        vip_trend = []
        try:
            # VIP signup trend (last 30 days)
            now = datetime.utcnow()
            for i in range(29, -1, -1):
                day = now - timedelta(days=i)
                day_start = day.replace(hour=0, minute=0, second=0, microsecond=0)
                day_end = day.replace(hour=23, minute=59, second=59, microsecond=999999)
                cur.execute(
                    "SELECT COUNT(*) FROM users WHERE is_vip=1 AND vip_expire_time >= %s AND created_at <= %s",
                    (day_start.strftime("%Y-%m-%d %H:%M:%S"), day_end.strftime("%Y-%m-%d %H:%M:%S"))
                )
                vip_trend.append({
                    "date": day_start.strftime("%m/%d"),
                    "count": cur.fetchone()[0]
                })
        except Exception:
            pass
        return {"vip_trend": vip_trend}


# ---- App Settings ----

class SettingsBody(BaseModel):
    version: str = ""
    force_update: bool = False
    changelog: str = ""


@router.put("/settings")
def update_settings(body: SettingsBody, admin: dict = Depends(get_admin)):
    # Store settings in memory for now (can be extended to a settings table)
    return {"ok": True, "version": body.version}


@router.delete("/workout-records/{record_id}")
def admin_delete_workout_record(record_id: int, admin: dict = Depends(get_admin)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM workout_records WHERE id=%s", (record_id,))
        return {"ok": True}
