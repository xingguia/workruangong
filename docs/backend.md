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
| POST | /auth/register | 用户注册 |
| POST | /auth/login | 用户登录 |
| GET | /user/profile | 获取用户信息 |
| PUT | /user/profile | 更新用户信息 |
| PUT | /user/vip | 更新VIP状态 |
| PUT | /user/body-data | 更新身体数据 |
| PUT | /user/assessment-completed | 设置评估完成状态 |
| PUT | /user/username-set | 设置用户名状态 |
| PUT | /user/settings | 更新用户设置 |
| POST | /user/change-password | 修改密码 |
| DELETE | /user/account | 注销账号 |
| POST | /user/feedback | 提交反馈 |
| GET | /user/feedback | 获取反馈列表 |
| GET | /user/feedback/{id}/messages | 获取反馈消息 |
| POST | /user/feedback/{id}/messages | 发送反馈消息 |
| PUT | /user/feedback/{id}/read | 标记反馈已读 |
| GET | /user/feedback/unread-count | 获取未读反馈数量 |
| GET | /user/export-data | 导出用户数据 |
| GET | /user/check-nickname | 检查昵称是否可用 |
| POST | /user/reserve-nickname | 预留昵称 |
| GET | /body-records | 获取身体记录 |
| POST | /body-records | 添加身体记录 |
| DELETE | /body-records/{id} | 删除身体记录 |
| GET | /workout-records | 获取训练记录 |
| POST | /workout-records | 添加训练记录 |
| DELETE | /workout-records/{id} | 删除训练记录 |
| GET | /training-tasks | 获取训练任务 |
| POST | /training-tasks | 创建训练任务 |
| PUT | /training-tasks/{id} | 更新训练任务 |
| DELETE | /training-tasks/{id} | 删除训练任务 |
| GET | /exercise-plans | 获取训练计划 |
| POST | /exercise-plans | 创建训练计划 |
| PUT | /exercise-plans/{day_of_week} | 更新训练计划 |
| GET | /achievements | 获取成就列表 |
| POST | /achievements | 创建成就 |
| PUT | /achievements/{type} | 更新成就状态 |
| GET | /exercises | 获取动作库 |
| GET | /announcements | 获取公告列表 |

### 管理端接口 (`/api/v1/admin`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /admin/login | 管理员登录 |
| GET | /admin/me | 当前管理员信息 |
| GET | /admin/notifications | 通知列表 |
| GET | /admin/dashboard | 仪表盘数据 |
| GET | /admin/users | 用户列表 |
| GET | /admin/users/{id} | 用户详情 |
| PUT | /admin/users/{id} | 修改用户信息 |
| PUT | /admin/users/{id}/vip | 开通/取消VIP |
| PUT | /admin/users/{id}/ban | 封禁/解封用户 |
| DELETE | /admin/users/{id} | 删除用户 |
| PUT | /admin/users/{id}/password | 修改用户密码 |
| GET | /admin/workout-records | 训练记录列表 |
| GET | /admin/training-tasks | 训练任务列表 |
| DELETE | /admin/workout-records/{id} | 删除训练记录 |
| GET | /admin/statistics/users | 用户统计数据 |
| GET | /admin/statistics/training | 训练统计数据 |
| GET | /admin/statistics/equipment | 器材使用统计 |
| GET | /admin/statistics/retention | 留存率统计 |
| GET | /admin/statistics/vip | VIP转化统计 |
| GET | /admin/admins | 管理员列表 |
| POST | /admin/admins | 新增管理员 |
| DELETE | /admin/admins/{id} | 删除管理员 |
| GET | /admin/exercises | 动作库列表 |
| GET | /admin/exercises/{id} | 动作详情 |
| POST | /admin/exercises | 新增动作 |
| PUT | /admin/exercises/{id} | 修改动作 |
| DELETE | /admin/exercises/{id} | 删除动作 |
| GET | /admin/achievements | 成就列表 |
| GET | /admin/announcements | 公告列表 |
| GET | /admin/announcements/{id} | 公告详情 |
| POST | /admin/announcements | 新增公告 |
| PUT | /admin/announcements/{id} | 修改公告 |
| DELETE | /admin/announcements/{id} | 删除公告 |
| PUT | /admin/settings | 更新设置 |
| GET | /admin/feedback | 反馈列表 |
| GET | /admin/feedback/{id} | 反馈详情 |
| GET | /admin/feedback/{id}/messages | 反馈消息列表 |
| POST | /admin/feedback/{id}/messages | 发送反馈消息 |
| PUT | /admin/feedback/{id}/status | 更新反馈状态 |
| DELETE | /admin/feedback/{id} | 删除反馈 |
| POST | /admin/feedback/batch-delete | 批量删除反馈 |
| PUT | /admin/feedback/batch-status | 批量更新反馈状态 |

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
