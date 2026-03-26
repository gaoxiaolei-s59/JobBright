# JobBright React Frontend

这是职位平台的 React 前端工程，负责承接登录、注册以及后续的职位平台前台页面。

## 技术选型

- React 18
- Vite 5
- 原生 CSS
- 通过 JWT 调用 Spring Boot 后端接口

## 当前能力

- 登录页
- 注册页
- 登录成功后的当前用户展示页
- 基于 `localStorage` 的登录态保存
- 可通过环境变量切换后端 API 地址

## 目录说明

- `src/App.jsx`: 页面主入口，包含登录注册逻辑和用户态展示
- `src/main.jsx`: React 挂载入口
- `src/styles.css`: 页面样式
- `.env.example`: 前端环境变量示例
- `vite.config.js`: Vite 配置

## 本地启动

先启动后端：

```bash
./mvnw -pl job-backed spring-boot:run
```

再启动前端：

```bash
cd job-frontend-react
npm install
npm run dev
```

默认访问地址：

- 前端: `http://localhost:5173`
- 后端: `http://localhost:8080`

## 环境变量

复制 `.env.example` 为 `.env`，然后根据需要修改：

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 已对接接口

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

## 演示账号

- 用户名: `demo`
- 密码: `JobBacked123`

## 下一步建议

- 增加 React Router，拆分首页、登录页、职位页和个人中心页
- 增加统一请求封装和错误边界
- 增加职位列表、职位详情、企业详情页
- 引入组件库或设计系统，继续向 Jobright 风格官网靠拢
