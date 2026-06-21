from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime

# ---------- 用户 ----------
class UserRegister(BaseModel):
    phone: str
    password: str
    nickname: Optional[str] = "健身爱好者"

class UserLogin(BaseModel):
    phone: str
    password: str

class UserUpdate(BaseModel):
    nickname: Optional[str] = None
    avatar: Optional[str] = None
    gender: Optional[str] = None
    fitness_goal: Optional[str] = None
    height: Optional[int] = None
    weight: Optional[float] = None
    body_fat: Optional[float] = None
    waist: Optional[float] = None
    hip: Optional[float] = None
    initial_height: Optional[int] = None
    initial_weight: Optional[float] = None
    initial_body_fat: Optional[float] = None
    initial_waist: Optional[float] = None
    initial_hip: Optional[float] = None

class UserResponse(BaseModel):
    id: int
    phone: str
    nickname: str
    avatar: Optional[str] = None
    gender: Optional[str] = None
    fitness_goal: Optional[str] = None
    height: int
    weight: float
    body_fat: float
    waist: float
    hip: float
    initial_height: int = 0
    initial_weight: float = 0
    initial_body_fat: float = 0
    initial_waist: float = 0
    initial_hip: float = 0
    is_vip: bool
    level: int
    vip_expire_time: Optional[str] = None
    workout_reminder: bool
    achievement_notification: bool
    dark_mode: bool = True
    unit_system: str = "metric"
    reminder_time: str = "18:00"
    assessment_completed: bool
    username_set: bool

# ---------- 身体记录 ----------
class BodyRecordCreate(BaseModel):
    height: int = 0
    weight: float = 0
    body_fat: float = 0
    waist: float = 0
    hip: float = 0

class BodyRecordResponse(BaseModel):
    id: int
    user_id: int
    height: int
    weight: float
    body_fat: float
    waist: float
    hip: float
    timestamp: int

# ---------- 训练记录 ----------
class WorkoutRecordCreate(BaseModel):
    task_id: Optional[int] = None
    exercise_name: str
    duration: int = 0
    sets: int = 0
    reps: int = 0
    weight: float = 0
    calories_burned: float = 0
    calories: float = 0
    notes: Optional[str] = None
    muscle_group: Optional[str] = None

class WorkoutRecordResponse(BaseModel):
    id: int
    user_id: int
    task_id: Optional[int] = None
    exercise_name: str
    timestamp: int
    duration: int
    sets: int
    reps: int
    weight: float
    calories_burned: float
    calories: float
    notes: Optional[str] = None
    muscle_group: Optional[str] = None

# ---------- 训练任务 ----------
class TrainingTaskCreate(BaseModel):
    date: int
    name: str
    description: Optional[str] = None
    duration: int = 0
    status: str = "NOT_STARTED"
    exercise_type: str = "STRENGTH"
    reps: int = 0
    sets: int = 0
    weight: float = 0
    muscle_group: Optional[str] = None
    sub_muscle: str = ""
    calories_recorded: bool = False
    treadmill_speed: float = 0
    treadmill_incline: float = 0

class TrainingTaskUpdate(BaseModel):
    status: Optional[str] = None
    calories_recorded: Optional[bool] = None
    reps: Optional[int] = None
    sets: Optional[int] = None
    weight: Optional[float] = None

class TrainingTaskResponse(BaseModel):
    id: int
    user_id: int
    date: int
    name: str
    description: Optional[str] = None
    duration: int
    status: str
    exercise_type: str
    reps: int
    sets: int
    weight: float
    muscle_group: Optional[str] = None
    sub_muscle: str
    calories_recorded: bool
    treadmill_speed: float
    treadmill_incline: float

# ---------- 训练计划 ----------
class ExercisePlanCreate(BaseModel):
    day_of_week: int
    status: str = "NOT_SET"
    completion_status: str = "NOT_SET"

class ExercisePlanUpdate(BaseModel):
    status: Optional[str] = None
    completion_status: Optional[str] = None

class ExercisePlanResponse(BaseModel):
    id: int
    user_id: int
    day_of_week: int
    status: str
    completion_status: str

# ---------- 成就 ----------
class AchievementUpdate(BaseModel):
    unlocked: Optional[bool] = None
    unlock_time: Optional[int] = None
    displayed: Optional[bool] = None
    display_position: Optional[int] = None

class AchievementResponse(BaseModel):
    id: int
    user_id: int
    achievement_type: str
    unlocked: bool
    unlock_time: int
    displayed: bool
    display_position: int

# ---------- Token ----------
class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse

# ---------- 修改密码 ----------
class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str

# ---------- 意见反馈 ----------
class FeedbackCreate(BaseModel):
    content: str
    contact: Optional[str] = None
    category: Optional[str] = "other"

# ---------- 动作库 ----------
class ExerciseResponse(BaseModel):
    id: int
    name: str
    muscle_group: Optional[str] = None
    sub_muscle: Optional[str] = None
    exercise_type: str = "STRENGTH"
    cal_per_rep: float = 0
    needs_equipment: bool = False
    description: Optional[str] = None
