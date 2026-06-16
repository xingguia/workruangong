# 数据库配置
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "wynz678hongyang",
    "database": "fitness_app",
    "charset": "utf8mb4",
}

# JWT 配置
SECRET_KEY = "fitness-app-secret-key-2024"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7  # 7 天
