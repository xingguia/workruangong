# Android 客户端文档

## 环境要求

- Android Studio Hedgehog 或更新版本
- JDK 17
- Android SDK 34
- 最低支持 Android 7.0 (API 24)

## 构建与运行

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 修改 `ApiClient.java` 中的 `BASE_URL` 为后端地址
4. 连接设备或启动模拟器，点击 Run

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── api/
│   └── ApiClient.java         # 网络请求封装 (OkHttp)
├── model/
│   ├── User.java              # 用户模型
│   ├── WorkoutRecord.java     # 训练记录模型
│   └── TrainingTask.java      # 训练任务模型
├── ui/screens/
│   ├── login/
│   │   ├── LoginFragment.java       # 登录页
│   │   └── RegisterFragment.java    # 注册页
│   ├── assessment/
│   │   └── AssessmentFragment.java  # 健身评估 (5步)
│   ├── home/
│   │   └── HomeFragment.java        # 首页
│   ├── training/
│   │   └── TrainingFragment.java    # 训练记录
│   └── profile/
│       └── ProfileFragment.java     # 个人中心
└── util/
    ├── SessionManager.java    # 登录状态管理
    ├── UserManager.java       # 用户数据缓存
    ├── WorkoutRecordManager.java  # 训练记录管理
    └── TrainingTaskManager.java   # 训练任务管理
```

## 主要功能

### 登录注册
- 手机号 + 密码登录/注册
- JWT Token 自动存储和刷新

### 健身评估 (5步)
1. 选择训练日（周一~周日）
2. 选择训练部位（胸、背、腿、肩、臂、核心）
3. 选择训练器械
4. 设置训练目标
5. 填写身体数据（身高、体重，自动计算 BMI）

### 首页
- 今日训练概览
- 训练任务列表（可标记完成/未完成）
- 卡路里消耗统计

### 训练记录
- 按日期查看训练历史
- 记录组数、次数、重量、时长
- 自动计算卡路里消耗

### 个人中心
- 查看/编辑个人资料
- 身体数据趋势图
- VIP 会员状态
- 成就系统
- 设置（训练提醒、成就通知）

## 注意事项

- 首次安装需完成健身评估才能使用完整功能
- 用户名需唯一，设置后可修改
- 训练记录会自动同步到服务器
- 离线数据会在联网后自动上传
