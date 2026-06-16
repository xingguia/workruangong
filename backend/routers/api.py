from fastapi import APIRouter, HTTPException
from database import get_db
from auth import hash_password, verify_password, create_access_token, get_current_user_id
from schemas import (
    UserRegister, UserLogin, UserUpdate,
    UserResponse, TokenResponse,
    BodyRecordCreate, BodyRecordResponse,
    WorkoutRecordCreate, WorkoutRecordResponse,
    TrainingTaskCreate, TrainingTaskUpdate, TrainingTaskResponse,
    ExercisePlanCreate, ExercisePlanUpdate, ExercisePlanResponse,
    AchievementUpdate, AchievementResponse,
)
import time

router = APIRouter(prefix="/api/v1", tags=["api"])

# ==================== 用户 ====================

@router.post("/auth/register", response_model=TokenResponse)
def register(data: UserRegister):
    with get_db() as conn:
        cur = conn.cursor()
        # 检查手机号
        cur.execute("SELECT id FROM users WHERE phone=%s", (data.phone,))
        if cur.fetchone():
            raise HTTPException(status_code=400, detail="Phone already registered")
        # 创建用户
        pwd_hash = hash_password(data.password)
        cur.execute(
            "INSERT INTO users (phone, nickname, password_hash) VALUES (%s,%s,%s)",
            (data.phone, data.nickname, pwd_hash)
        )
        user_id = cur.lastrowid
        token = create_access_token(user_id)
        return {
            "access_token": token,
            "user": _get_user_dict(cur, user_id)
        }

@router.post("/auth/login", response_model=TokenResponse)
def login(data: UserLogin):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id, password_hash FROM users WHERE phone=%s", (data.phone,))
        row = cur.fetchone()
        if not row or not verify_password(data.password, row[1]):
            raise HTTPException(status_code=401, detail="Invalid phone or password")
        token = create_access_token(row[0])
        return {
            "access_token": token,
            "user": _get_user_dict(cur, row[0])
        }

@router.get("/user/profile", response_model=UserResponse)
def get_profile(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        return _get_user_dict(cur, user_id)

@router.put("/user/profile", response_model=UserResponse)
def update_profile(data: UserUpdate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        fields = []
        values = []
        for k, v in data.dict(exclude_none=True).items():
            fields.append(f"{k}=%s")
            values.append(v)
        if fields:
            values.append(user_id)
            cur.execute(f"UPDATE users SET {','.join(fields)} WHERE id=%s", values)
        return _get_user_dict(cur, user_id)

@router.put("/user/vip")
def update_vip(is_vip: bool, expire_time: str = None, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET is_vip=%s, vip_expire_time=%s WHERE id=%s",
                    (1 if is_vip else 0, expire_time, user_id))
        return {"ok": True}

@router.put("/user/body-data")
def update_body_data(height: int, weight: float, body_fat: float = 0, waist: float = 0, hip: float = 0,
                     user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET height=%s, weight=%s, body_fat=%s, waist=%s, hip=%s WHERE id=%s",
                    (height, weight, body_fat, waist, hip, user_id))
        return {"ok": True}

@router.put("/user/assessment-completed")
def mark_assessment_completed(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET assessment_completed=1 WHERE id=%s", (user_id,))
        return {"ok": True}

@router.put("/user/username-set")
def mark_username_set(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("UPDATE users SET username_set=1 WHERE id=%s", (user_id,))
        return {"ok": True}

@router.put("/user/settings")
def update_settings(workout_reminder: bool = None, achievement_notification: bool = None,
                    user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        if workout_reminder is not None:
            cur.execute("UPDATE users SET workout_reminder=%s WHERE id=%s", (1 if workout_reminder else 0, user_id))
        if achievement_notification is not None:
            cur.execute("UPDATE users SET achievement_notification=%s WHERE id=%s", (1 if achievement_notification else 0, user_id))
        return {"ok": True}

@router.get("/user/check-nickname")
def check_nickname(nickname: str):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT id FROM usernames WHERE username=%s", (nickname,))
        return {"available": cur.fetchone() is None}

@router.post("/user/reserve-nickname")
def reserve_nickname(nickname: str):
    with get_db() as conn:
        cur = conn.cursor()
        try:
            cur.execute("INSERT INTO usernames (username) VALUES (%s)", (nickname,))
            return {"ok": True}
        except:
            raise HTTPException(status_code=400, detail="Username already taken")

def _get_user_dict(cur, user_id):
    cur.execute("SELECT * FROM users WHERE id=%s", (user_id,))
    row = cur.fetchone()
    if not row:
        raise HTTPException(status_code=404, detail="User not found")
    cols = [d[0] for d in cur.description]
    d = dict(zip(cols, row))
    return UserResponse(
        id=d["id"], phone=d["phone"], nickname=d["nickname"],
        avatar=d.get("avatar"), gender=d.get("gender"),
        fitness_goal=d.get("fitness_goal"),
        height=d.get("height", 0), weight=d.get("weight", 0),
        body_fat=d.get("body_fat", 0), waist=d.get("waist", 0), hip=d.get("hip", 0),
        is_vip=bool(d.get("is_vip", 0)), level=d.get("level", 1),
        vip_expire_time=str(d["vip_expire_time"]) if d.get("vip_expire_time") else None,
        workout_reminder=bool(d.get("workout_reminder", 1)),
        achievement_notification=bool(d.get("achievement_notification", 1)),
        assessment_completed=bool(d.get("assessment_completed", 0)),
        username_set=bool(d.get("username_set", 0)),
    )

# ==================== 身体记录 ====================

@router.get("/body-records", response_model=list[BodyRecordResponse])
def get_body_records(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM body_records WHERE user_id=%s ORDER BY timestamp DESC", (user_id,))
        return [_body_record_row(r, user_id) for r in cur.fetchall()]

@router.post("/body-records", response_model=BodyRecordResponse)
def create_body_record(data: BodyRecordCreate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        ts = int(time.time() * 1000)
        cur.execute(
            "INSERT INTO body_records (user_id, height, weight, body_fat, waist, hip, timestamp) VALUES (%s,%s,%s,%s,%s,%s,%s)",
            (user_id, data.height, data.weight, data.body_fat, data.waist, data.hip, ts)
        )
        rid = cur.lastrowid
        return BodyRecordResponse(id=rid, user_id=user_id, height=data.height, weight=data.weight,
                                  body_fat=data.body_fat, waist=data.waist, hip=data.hip, timestamp=ts)

@router.delete("/body-records/{record_id}")
def delete_body_record(record_id: int, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM body_records WHERE id=%s AND user_id=%s", (record_id, user_id))
        return {"ok": True}

def _body_record_row(r, uid):
    return BodyRecordResponse(id=r[0], user_id=uid, height=r[2], weight=r[3],
                              body_fat=r[4], waist=r[5], hip=r[6], timestamp=r[7])

# ==================== 训练记录 ====================

@router.get("/workout-records", response_model=list[WorkoutRecordResponse])
def get_workout_records(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM workout_records WHERE user_id=%s ORDER BY timestamp DESC", (user_id,))
        return [_workout_record_row(r, user_id) for r in cur.fetchall()]

@router.post("/workout-records", response_model=WorkoutRecordResponse)
def create_workout_record(data: WorkoutRecordCreate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        ts = int(time.time() * 1000)
        cur.execute(
            "INSERT INTO workout_records (user_id, task_id, exercise_name, timestamp, duration, sets, reps, weight, calories_burned, calories, notes, muscle_group) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
            (user_id, data.task_id, data.exercise_name, ts, data.duration, data.sets, data.reps, data.weight, data.calories_burned, data.calories, data.notes, data.muscle_group)
        )
        rid = cur.lastrowid
        return WorkoutRecordResponse(
            id=rid, user_id=user_id, task_id=data.task_id, exercise_name=data.exercise_name,
            timestamp=ts, duration=data.duration, sets=data.sets, reps=data.reps,
            weight=data.weight, calories_burned=data.calories_burned, calories=data.calories,
            notes=data.notes, muscle_group=data.muscle_group
        )

@router.delete("/workout-records/{record_id}")
def delete_workout_record(record_id: int, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM workout_records WHERE id=%s AND user_id=%s", (record_id, user_id))
        return {"ok": True}

def _workout_record_row(r, uid):
    return WorkoutRecordResponse(
        id=r[0], user_id=uid, task_id=r[2], exercise_name=r[3], timestamp=r[4],
        duration=r[5], sets=r[6], reps=r[7], weight=r[8], calories_burned=r[9],
        calories=r[10], notes=r[11], muscle_group=r[12]
    )

# ==================== 训练任务 ====================

@router.get("/training-tasks", response_model=list[TrainingTaskResponse])
def get_training_tasks(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM training_tasks WHERE user_id=%s ORDER BY date DESC", (user_id,))
        return [_training_task_row(r, user_id) for r in cur.fetchall()]

@router.post("/training-tasks", response_model=TrainingTaskResponse)
def create_training_task(data: TrainingTaskCreate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO training_tasks (user_id, date, name, description, duration, status, exercise_type, reps, sets, weight, muscle_group, sub_muscle, calories_recorded, treadmill_speed, treadmill_incline) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
            (user_id, data.date, data.name, data.description, data.duration, data.status,
             data.exercise_type, data.reps, data.sets, data.weight, data.muscle_group,
             data.sub_muscle, 1 if data.calories_recorded else 0, data.treadmill_speed, data.treadmill_incline)
        )
        tid = cur.lastrowid
        return _training_task_dict(cur, tid, user_id)

@router.put("/training-tasks/{task_id}", response_model=TrainingTaskResponse)
def update_training_task(task_id: int, data: TrainingTaskUpdate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        fields = []
        values = []
        if data.status is not None:
            fields.append("status=%s"); values.append(data.status)
        if data.calories_recorded is not None:
            fields.append("calories_recorded=%s"); values.append(1 if data.calories_recorded else 0)
        if data.reps is not None:
            fields.append("reps=%s"); values.append(data.reps)
        if data.sets is not None:
            fields.append("sets=%s"); values.append(data.sets)
        if data.weight is not None:
            fields.append("weight=%s"); values.append(data.weight)
        if fields:
            values.extend([task_id, user_id])
            cur.execute(f"UPDATE training_tasks SET {','.join(fields)} WHERE id=%s AND user_id=%s", values)
        return _training_task_dict(cur, task_id, user_id)

@router.delete("/training-tasks/{task_id}")
def delete_training_task(task_id: int, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM training_tasks WHERE id=%s AND user_id=%s", (task_id, user_id))
        return {"ok": True}

def _training_task_row(r, uid):
    return TrainingTaskResponse(
        id=r[0], user_id=uid, date=r[2], name=r[3], description=r[4], duration=r[5],
        status=r[6], exercise_type=r[7], reps=r[8], sets=r[9], weight=r[10],
        muscle_group=r[11], sub_muscle=r[12], calories_recorded=bool(r[13]),
        treadmill_speed=r[14], treadmill_incline=r[15]
    )

def _training_task_dict(cur, tid, uid):
    cur.execute("SELECT * FROM training_tasks WHERE id=%s", (tid,))
    return _training_task_row(cur.fetchone(), uid)

# ==================== 训练计划 ====================

@router.get("/exercise-plans", response_model=list[ExercisePlanResponse])
def get_exercise_plans(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM exercise_plans WHERE user_id=%s ORDER BY day_of_week", (user_id,))
        return [ExercisePlanResponse(id=r[0], user_id=user_id, day_of_week=r[2], status=r[3], completion_status=r[4]) for r in cur.fetchall()]

@router.post("/exercise-plans", response_model=ExercisePlanResponse)
def create_exercise_plan(data: ExercisePlanCreate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO exercise_plans (user_id, day_of_week, status, completion_status) VALUES (%s,%s,%s,%s) ON DUPLICATE KEY UPDATE status=VALUES(status), completion_status=VALUES(completion_status)",
            (user_id, data.day_of_week, data.status, data.completion_status)
        )
        pid = cur.lastrowid or 0
        return ExercisePlanResponse(id=pid, user_id=user_id, day_of_week=data.day_of_week, status=data.status, completion_status=data.completion_status)

@router.put("/exercise-plans/{day_of_week}", response_model=ExercisePlanResponse)
def update_exercise_plan(day_of_week: int, data: ExercisePlanUpdate, user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        fields = []
        values = []
        if data.status is not None:
            fields.append("status=%s"); values.append(data.status)
        if data.completion_status is not None:
            fields.append("completion_status=%s"); values.append(data.completion_status)
        if fields:
            values.extend([user_id, day_of_week])
            cur.execute(f"UPDATE exercise_plans SET {','.join(fields)} WHERE user_id=%s AND day_of_week=%s", values)
        cur.execute("SELECT * FROM exercise_plans WHERE user_id=%s AND day_of_week=%s", (user_id, day_of_week))
        r = cur.fetchone()
        if r:
            return ExercisePlanResponse(id=r[0], user_id=user_id, day_of_week=r[2], status=r[3], completion_status=r[4])
        raise HTTPException(status_code=404, detail="Plan not found")

# ==================== 成就 ====================

@router.get("/achievements", response_model=list[AchievementResponse])
def get_achievements(user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute("SELECT * FROM achievements WHERE user_id=%s", (user_id,))
        return [AchievementResponse(id=r[0], user_id=user_id, achievement_type=r[2], unlocked=bool(r[3]),
                                     unlock_time=r[4], displayed=bool(r[5]), display_position=r[6]) for r in cur.fetchall()]

@router.post("/achievements")
def save_achievement(achievement_type: str, unlocked: bool = True,
                     user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        ts = int(time.time() * 1000) if unlocked else 0
        cur.execute(
            "INSERT INTO achievements (user_id, achievement_type, unlocked, unlock_time) VALUES (%s,%s,%s,%s) ON DUPLICATE KEY UPDATE unlocked=VALUES(unlocked), unlock_time=VALUES(unlock_time)",
            (user_id, achievement_type, 1 if unlocked else 0, ts)
        )
        return {"ok": True}

@router.put("/achievements/{achievement_type}")
def update_achievement(achievement_type: str, data: AchievementUpdate,
                       user_id: int = __import__("fastapi").Depends(get_current_user_id)):
    with get_db() as conn:
        cur = conn.cursor()
        fields = []
        values = []
        if data.displayed is not None:
            fields.append("displayed=%s"); values.append(1 if data.displayed else 0)
        if data.display_position is not None:
            fields.append("display_position=%s"); values.append(data.display_position)
        if fields:
            values.extend([user_id, achievement_type])
            cur.execute(f"UPDATE achievements SET {','.join(fields)} WHERE user_id=%s AND achievement_type=%s", values)
        return {"ok": True}
