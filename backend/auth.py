import jwt
import hashlib
from datetime import datetime, timedelta
from fastapi import Header, HTTPException, Depends
from config import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()

def verify_password(password: str, hashed: str) -> bool:
    return hash_password(password) == hashed

def create_access_token(user_id: int) -> str:
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload = {"sub": str(user_id), "exp": expire}
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

def decode_token(token: str) -> int:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return int(payload["sub"])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Invalid token")

def _extract_user_id(authorization: str = Header(...)) -> int:
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header")
    token = authorization[7:]
    return decode_token(token)

def get_current_user_id(authorization: str = Header(...)) -> int:
    return _extract_user_id(authorization)

def get_active_user_id(authorization: str = Header(...)) -> int:
    """验证token并检查用户是否被封禁"""
    user_id = _extract_user_id(authorization)
    from database import get_connection
    conn = get_connection()
    try:
        cur = conn.cursor()
        cur.execute("SELECT COALESCE(is_active, 1) FROM users WHERE id=%s", (user_id,))
        row = cur.fetchone()
        if row and row[0] == 0:
            raise HTTPException(status_code=403, detail="该账号已被封禁")
    finally:
        conn.close()
    return user_id
