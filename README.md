# 燃动时刻 - 健身管理应用

一款面向健身爱好者的移动应用，包含 Android 客户端和 Python 后端服务，配套 Web 管理后台。

## 项目结构

```
ruangong/
├── app/                          # Android 客户端 (Java)
│   └── src/main/
│       ├── java/com/example/myapplication/
│       │   ├── api/              # 网络请求 (OkHttp)
│       │   ├── model/            # 数据模型
│       │   ├── ui/screens/       # 各页面 Fragment
│       │   │   ├── assessment/   # 健身评估
│       │   │   ├── home/         # 首页
│       │   │   ├── training/     # 训练记录
│       │   │   ├── profile/      # 个人中心
│       │   │   ├── login/        # 登录注册
│       │   │   ├── progress/     # 进度页面
│       │   │   ├── register/     # 注册页面
│       │   │   └── vip/          # VIP页面
│       │   ├── views/            # 自定义视图
│       │   └── util/             # 工具类
│       └── res/                  # 资源文件
├── backend/                      # Python 后端 (FastAPI)
│   ├── main.py                   # 入口文件
│   ├── config.py                 # 配置 (数据库、JWT)
│   ├── database.py               # 数据库连接
│   ├── auth.py                   # 认证工具
│   ├── models.py                 # 建表与初始化
│   ├── schemas.py                # 数据模型定义
│   ├── generate_favicon.py       # 生成图标
│   ├── seed_exercises.py         # 种子数据
│   ├── routers/
│   │   ├── api.py                # 用户端 API
│   │   └── admin.py              # 管理端 API
│   └── static/
│       ├── admin/
│       │   ├── index.html        # 管理后台前端
│       │   └── echarts.min.js    # ECharts图表库
│       ├── favicon.ico           # 网站图标
│       └── favicon_preview.png   # 图标预览
└── docs/                         # 项目文档
```

## 技术栈

| 模块 | 技术 |
|------|------|
| Android 客户端 | Java, OkHttp, ViewBinding |
| 后端服务 | Python FastAPI, PyMySQL |
| 数据库 | MySQL 8.0 |
| 管理后台 | 原生 HTML/CSS/JS, ECharts |
| 认证 | JWT (JSON Web Token) |
| 密码加密 | SHA-256 |

## 快速启动

### 1. 启动 MySQL

确保 MySQL 8.0 服务正在运行，数据库 `fitness_app` 已创建。

```sql
CREATE DATABASE IF NOT EXISTS fitness_app CHARACTER SET utf8mb4;
```

### 2. 启动后端

```bash
cd backend
pip install fastapi uvicorn pymysql pyjwt
python main.py
```

服务启动后：
- API 地址: http://localhost:8000
- 管理后台: http://localhost:8000/admin

### 3. 启动 Android 客户端

使用 Android Studio 打开项目根目录，同步 Gradle 后运行。

> 注意：需修改 `ApiClient.java` 中的 `BASE_URL` 为后端实际地址。

## 默认账号

| 类型 | 用户名 | 密码 |
|------|--------|------|
| 管理后台 | admin | admin123 |

## 数据库表

| 表名 | 说明 |
|------|------|
| users | 用户信息 |
| admins | 管理员账号 |
| body_records | 身体数据记录 |
| workout_records | 训练记录 |
| training_tasks | 训练任务 |
| exercise_plans | 训练计划 |
| achievements | 成就系统 |
| usernames | 用户名唯一性校验 |
| exercises | 训练动作库 |
| announcements | 公告管理 |
| feedback | 用户反馈 |
| feedback_messages | 反馈消息 |
