# 后端服务文档

## 启动方式

```bash
cd backend
python main.py
```

服务运行在 `http://0.0.0.0:8000`

## 配置文件

`backend/config.py`

```python
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "你的密码",
    "database": "fitness_app",
    "charset": "utf8mb4",
}
SECRET_KEY = "fitness-app-secret-key-2024"
```

## API 接口

### 用户端接口 (`/api/v1`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /register | 用户注册 |
| POST | /login | 用户登录 |
| GET | /profile | 获取用户信息 |
| PUT | /profile | 更新用户信息 |
| POST | /body-records | 添加身体记录 |
| GET | /body-records | 获取身体记录 |
| POST | /workout-records | 添加训练记录 |
| GET | /workout-records | 获取训练记录 |
| DELETE | /workout-records/{id} | 删除训练记录 |
| POST | /training-tasks | 创建训练任务 |
| GET | /training-tasks | 获取训练任务 |
| PUT | /training-tasks/{id} | 更新训练任务 |
| DELETE | /training-tasks/{id} | 删除训练任务 |
| GET | /achievements | 获取成就列表 |
| PUT | /achievements/{id} | 更新成就状态 |
| POST | /usernames/check | 检查用户名是否可用 |

### 管理端接口 (`/api/v1/admin`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /admin/login | 管理员登录 |
| GET | /admin/me | 当前管理员信息 |
| GET | /admin/dashboard | 仪表盘数据 |
| GET | /admin/users | 用户列表 |
| GET | /admin/users/{id} | 用户详情 |
| PUT | /admin/users/{id} | 修改用户信息 |
| PUT | /admin/users/{id}/vip | 开通/取消VIP |
| PUT | /admin/users/{id}/ban | 封禁/解封用户 |
| PUT | /admin/users/{id}/password | 修改用户密码 |
| GET | /admin/workout-records | 训练记录列表 |
| GET | /admin/training-tasks | 训练任务列表 |
| DELETE | /admin/workout-records/{id} | 删除训练记录 |
| GET | /admin/statistics/users | 用户统计数据 |
| GET | /admin/statistics/training | 训练统计数据 |
| GET | /admin/admins | 管理员列表 |
| POST | /admin/admins | 新增管理员 |
| DELETE | /admin/admins/{id} | 删除管理员 |

## 认证方式

所有需要认证的接口需在请求头中携带 JWT Token：

```
Authorization: Bearer <token>
```

Token 通过登录接口获取，有效期 7 天。

## 数据库初始化

启动时自动执行 `models.py` 中的 `init_db()`，自动建表并创建默认管理员账号。

如需重置数据库：

```sql
DROP DATABASE fitness_app;
CREATE DATABASE fitness_app CHARACTER SET utf8mb4;
```

重启后端即可自动重建。
