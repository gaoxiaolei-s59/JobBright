# JobBacked Project Overview / 项目总览

## 1. Project Summary / 项目简介

**EN**

JobBacked is a prototype job platform project with:

- a Spring Boot backend
- a React frontend
- shared framework utilities
- file upload and object storage support
- recommended jobs and user dashboard modules

It is designed around a student/new-grad job seeking workflow: sign in, upload a resume, enter the job dashboard, filter recommended jobs, and maintain personal profile information.

**中文**

JobBacked 是一个职位平台原型项目，当前包含：

- Spring Boot 后端
- React 前端
- 公共框架模块
- 文件上传与对象存储能力
- 推荐职位与用户工作台模块

当前整体流程围绕学生/校招用户求职路径设计：登录、上传简历、进入职位工作台、筛选推荐职位、维护个人资料与套餐信息。

---

## 2. Repository Structure / 仓库结构

```text
JobBacked
├── job-backed/          # Backend application
├── job-framework/       # Shared framework utilities
├── job-rag/             # File/RAG related services
├── job-frontend-react/  # Frontend application
└── docs/                # Project documentation
```

**Key directories / 关键目录**

- `job-backed/`: main backend for auth, dashboard, recommendation, resume upload
- `job-framework/`: common result wrappers, exception handling, auth context, DB configuration
- `job-rag/`: file storage/RAG-side file handling and MQ consumer logic
- `job-frontend-react/`: current dashboard-style frontend prototype
- `docs/`: API docs, style docs, module docs

---

## 3. Current Features / 当前功能

### 3.1 Authentication / 登录注册

**EN**

- User registration
- User login
- Current user info retrieval
- Token-based authentication using `X-Access-Token`

**中文**

- 用户注册
- 用户登录
- 获取当前用户信息
- 基于 `X-Access-Token` 的登录态认证

Related APIs / 相关接口:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

---

### 3.2 Resume Management / 简历管理

**EN**

- Upload current resume
- Replace previous active resume
- Store file in object storage
- Read current resume info
- Resume processing message dispatch is optional when RocketMQ is configured

**中文**

- 上传当前简历
- 替换旧的生效简历
- 将文件写入对象存储
- 获取当前简历信息
- 如果配置了 RocketMQ，会发送简历处理消息；未配置时也可正常启动和上传

Related APIs / 相关接口:

- `POST /api/user/resume/upload`
- `GET /api/user/resume/current`

Related code / 相关代码:

- [UserResumeController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/UserResumeController.java)
- [UserResumeServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/impl/UserResumeServiceImpl.java)

---

### 3.3 Home Overview / 首页概览

**EN**

The backend provides overview cards for the dashboard homepage, including:

- fresh job count
- average match rate
- average shortlist time
- resume score
- profile completion rate

**中文**

后端已经提供首页概览数据，包括：

- 新增岗位数
- 平均匹配率
- 平均首轮筛选时间
- 简历评分
- 资料完成度

Related API / 相关接口:

- `GET /api/home/overview`

---

### 3.4 Recommended Jobs / 推荐职位

**EN**

The recommendation module supports:

- keyword search
- country filter
- title filter
- experience level filter
- employment type filter
- work mode filter
- date posted filter
- `industryName` filter through `company.industry_name`
- paginated response with `hasMore`

**中文**

推荐职位模块当前支持：

- 关键词搜索
- 国家筛选
- 职位名称筛选
- 经验等级筛选
- 用工类型筛选
- 工作模式筛选
- 发布时间筛选
- 通过 `company.industry_name` 进行 `industryName` 行业筛选
- 分页返回，并提供 `hasMore`

Related API / 相关接口:

- `GET /api/jobs/recommended`

Related code / 相关代码:

- [RecommendController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/RecommendController.java)
- [RecommendServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/impl/RecommendServiceImpl.java)
- [JobPostMapper.xml](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/resources/mapper/JobPostMapper.xml)

---

### 3.5 User Dashboard / 用户工作台

**EN**

The user dashboard API provides:

- display name
- plan name
- resume score
- profile completion rate
- actionable tips

**中文**

用户工作台接口当前提供：

- 展示名称
- 套餐名称
- 简历评分
- 资料完整度
- 求职提醒项

Related API / 相关接口:

- `GET /api/user/dashboard`

Related code / 相关代码:

- [UserController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/UserController.java)
- [UserServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/impl/UserServiceImpl.java)

---

### 3.6 Frontend Dashboard / 前端工作台

**EN**

The frontend currently includes:

- login and register page
- resume upload gate
- recommended jobs dashboard
- left navigation with icons
- right user rail
- separated Resume Center and Profile views
- local demo interactions for upgrading plan
- responsive layout

**中文**

前端当前已经包含：

- 登录/注册页面
- 简历上传前置页
- 推荐职位工作台
- 带图标的左侧导航
- 右侧用户侧栏
- 已拆分的“简历中心”和“个人资料”
- 套餐升级的前端演示交互
- 响应式布局

Related code / 相关代码:

- [App.jsx](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-frontend-react/src/App.jsx)
- [styles.css](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-frontend-react/src/styles.css)

---

### 3.7 URL File Download / URL 文件下载

**EN**

The `job-rag` module now supports:

- download file by file ID from object storage
- download file directly by URL
- convert downloaded URL content into `MultipartFile`

**中文**

`job-rag` 模块当前还支持：

- 通过文件 ID 从对象存储下载文件
- 通过 URL 直接下载文件
- 将 URL 下载结果转换成 `MultipartFile`

Related code / 相关代码:

- [FileService.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/service/FileService.java)
- [FileServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/service/impl/FileServiceImpl.java)

---

## 4. Core User Flow / 核心运行流程

### 4.1 Frontend User Flow / 前端用户流程

**EN**

1. Open the frontend application.
2. Log in or register.
3. Frontend requests `/api/auth/me`.
4. Frontend requests `/api/user/resume/current`.
5. If there is no active resume, user enters the resume upload gate.
6. Upload resume through `/api/user/resume/upload`.
7. After upload, frontend loads:
   - `/api/home/overview`
   - `/api/user/dashboard`
   - `/api/jobs/recommended`
8. User enters the main dashboard.
9. User can:
   - filter jobs
   - open Resume Center
   - maintain profile
   - switch plan in frontend demo mode

**中文**

1. 打开前端页面。
2. 用户登录或注册。
3. 前端请求 `/api/auth/me`。
4. 前端请求 `/api/user/resume/current`。
5. 如果没有当前生效简历，则进入简历上传前置页。
6. 通过 `/api/user/resume/upload` 上传简历。
7. 上传成功后，前端继续加载：
   - `/api/home/overview`
   - `/api/user/dashboard`
   - `/api/jobs/recommended`
8. 用户进入主工作台。
9. 用户可继续：
   - 筛选职位
   - 进入简历中心
   - 维护个人资料
   - 在前端演示模式下切换套餐

---

### 4.2 Resume Upload Backend Flow / 简历上传后端流程

**EN**

1. Controller receives `MultipartFile`.
2. Service validates login state and file info.
3. Existing active resume is marked inactive.
4. File is uploaded to object storage.
5. Resume metadata is stored in database.
6. If RocketMQ is available, a resume-processing message is sent.
7. If RocketMQ is not available, upload still succeeds and the message is skipped.

**中文**

1. Controller 接收 `MultipartFile`。
2. Service 校验登录状态和文件信息。
3. 原有生效简历被置为失效。
4. 文件被上传到对象存储。
5. 简历元数据写入数据库。
6. 如果 RocketMQ 可用，则发送简历处理消息。
7. 如果 RocketMQ 不可用，也不会影响上传成功，只会跳过消息发送。

---

## 5. Local Run Guide / 本地运行说明

### 5.1 Requirements / 环境依赖

**EN**

- JDK 17
- Maven
- Node.js 18+
- MySQL
- Redis
- Object storage service compatible with current RustFS/S3 configuration
- RocketMQ is optional in the current local startup mode

**中文**

- JDK 17
- Maven
- Node.js 18+
- MySQL
- Redis
- 兼容当前 RustFS/S3 配置的对象存储服务
- RocketMQ 在当前本地模式下是可选的

---

### 5.2 Backend Start / 后端启动

From project root / 在项目根目录执行：

```bash
./mvnw -pl job-backed -am spring-boot:run
```

Backend default URL / 后端默认地址：

```text
http://localhost:10010
```

Notes / 说明：

- backend reads config from [application.yml](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/resources/application.yml)
- if RocketMQ is not configured, the backend can still start normally

---

### 5.3 Frontend Start / 前端启动

```bash
cd job-frontend-react
npm install
npm run dev
```

Frontend default URL / 前端默认地址：

```text
http://localhost:5173
```

Optional environment variable / 可选环境变量：

```bash
VITE_API_BASE_URL=http://localhost:10010
```

---

### 5.4 Build Verification / 构建校验

**Backend**

```bash
./mvnw -pl job-backed -am -DskipTests compile
```

**RAG module**

```bash
./mvnw -pl job-rag -am -DskipTests compile
```

**Frontend**

```bash
cd job-frontend-react
npm run build
```

---

## 6. Main APIs / 主要接口

| Module | Method | Path | Description |
|---|---|---|---|
| Auth | `POST` | `/api/auth/register` | Register user |
| Auth | `POST` | `/api/auth/login` | Login |
| Auth | `GET` | `/api/auth/me` | Current user |
| Resume | `POST` | `/api/user/resume/upload` | Upload resume |
| Resume | `GET` | `/api/user/resume/current` | Current resume |
| Home | `GET` | `/api/home/overview` | Overview cards |
| Jobs | `GET` | `/api/jobs/recommended` | Recommended jobs |
| User | `GET` | `/api/user/dashboard` | User dashboard info |

---

## 7. Current Notes / 当前注意事项

**EN**

- Plan switching in the frontend is currently demo-only and stored locally.
- Saved filters are still frontend mock data.
- Resume scoring is still partially mock/default based.
- RocketMQ consumer-side resume processing flow is still being expanded.

**中文**

- 套餐切换当前还是前端演示态，数据保存在本地。
- 已保存筛选目前还是前端 mock 数据。
- 简历评分目前仍有一部分是默认/演示值。
- RocketMQ 消费侧的简历处理链路还在继续完善中。

---

## 8. Recommended Next Steps / 建议下一步

**EN**

- Add persistent APIs for profile preference and membership plan
- Add saved filter CRUD
- Add real resume score calculation
- Add end-to-end MQ processing for uploaded resumes
- Add frontend routing and page-level module split

**中文**

- 增加个人资料偏好与套餐持久化接口
- 增加已保存筛选的增删改查
- 增加真实简历评分计算
- 打通上传简历后的 MQ 全链路处理
- 为前端增加路由和页面级拆分

