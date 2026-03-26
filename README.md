# JobBacked

这是一个职位平台后端与前端原型仓库，目前已经包含：

- Spring Boot 后端登录系统
- React 前端登录原型
- 前后端联调说明文档

## 目录结构

- `job-backed/`: Spring Boot 后端模块
- `job-framework/`: 公共框架模块，负责自动装配、统一返回、异常处理、鉴权等基础能力
- `job-frontend-react/`: React 前端工程
- `docs/backend-auth-system.md`: 后端登录系统说明

## 当前功能

- 用户注册
- 用户登录
- 获取当前用户
- React 登录/注册页面
- JWT 登录态保存

## 启动方式

### 1. 启动后端

```bash
./mvnw -pl job-backed spring-boot:run
```

后端默认地址：

- `http://localhost:8080`

### 2. 启动前端

```bash
cd job-frontend-react
npm install
npm run dev
```

前端默认地址：

- `http://localhost:5173`

## 文档

- 前端说明: `job-frontend-react/README.md`
- 后端说明: `docs/backend-auth-system.md`

## 演示账号

- 用户名: `demo`
- 密码: `JobBacked123`
