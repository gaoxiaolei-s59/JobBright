# 后端登录系统说明

## 概述

当前 `job-backed` 模块已经实现了一个可运行的最小登录系统，供职位平台前台或管理端接入。

包职责约定：

- `org.puregxl.site.jobbacked.*`: 业务模块代码
- `org.puregxl.site.framework.*`: 全局基础设施和自动装配代码

系统特性：

- 用户注册
- 用户登录
- 获取当前登录用户
- JWT Bearer Token 鉴权
- BCrypt 密码加密
- H2 默认数据库初始化
- MockMvc 集成测试

## 接口列表

### 1. 注册

请求：

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "displayName": "Alice",
  "password": "12345678"
}
```

返回：

```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresInSeconds": 86400,
    "user": {
      "id": 2,
      "username": "alice",
      "email": "alice@example.com",
      "displayName": "Alice",
      "role": "USER"
    }
  }
}
```

### 2. 登录

请求：

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "account": "demo",
  "password": "JobBacked123"
}
```

说明：

- `account` 支持用户名或邮箱

### 3. 获取当前用户

请求：

```http
GET /api/auth/me
Authorization: Bearer <token>
```

## 核心文件说明

- `job-backed/src/main/java/org/puregxl/site/jobbacked/controller/AuthController.java`
  认证接口入口
- `job-backed/src/main/java/org/puregxl/site/jobbacked/service/AuthService.java`
  注册、登录、用户读取核心业务
- `job-framework/src/main/java/org/puregxl/site/framework/security/JwtTokenService.java`
  JWT 生成与解析
- `job-framework/src/main/java/org/puregxl/site/framework/security/AuthInterceptor.java`
  Bearer Token 登录态拦截
- `job-framework/src/main/java/org/puregxl/site/framework/common/ApiResponse.java`
  统一返回结构
- `job-framework/src/main/java/org/puregxl/site/framework/exception/GlobalExceptionHandler.java`
  全局异常处理
- `job-backed/src/main/java/org/puregxl/site/jobbacked/dao/UserAccountDao.java`
  基于 JDBC 的用户数据访问层
- `job-backed/src/main/java/org/puregxl/site/jobbacked/dao/entity/UserAccount.java`
  用户实体
- `job-framework/src/main/java/org/puregxl/site/framework/autoconfigure/FrameworkAutoConfiguration.java`
  framework 模块自动装配入口
- `job-backed/src/main/resources/schema.sql`
  用户表结构初始化
- `job-backed/src/main/resources/data.sql`
  演示用户初始化数据

## 默认账号

- 用户名: `demo`
- 邮箱: `demo@jobbacked.com`
- 密码: `JobBacked123`

## 认证流程

1. 用户调用注册或登录接口。
2. 后端校验用户名、邮箱和密码。
3. 后端签发 JWT。
4. 前端保存 `accessToken`。
5. 前端后续请求受保护接口时，在 `Authorization` 请求头中带上 `Bearer <token>`。
6. 拦截器解析 JWT，识别当前用户。

## 数据库说明

当前默认使用 H2 文件数据库：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/jobbacked;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE
```

优点：

- 本地启动简单
- 不依赖额外数据库服务
- 便于前后端联调

后续切换 MySQL 时，只需要改：

- `spring.datasource.driver-class-name`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## 测试

当前已包含认证流程测试：

```bash
./mvnw -pl job-backed test
```

验证内容：

- 演示账号可以登录
- 登录后可成功访问 `/api/auth/me`

## 后续增强建议

- 增加刷新 Token 机制
- 增加邮箱验证码和找回密码
- 增加用户状态字段，如禁用、未激活
- 增加角色权限模型，如求职者、招聘者、管理员
- 接入 MySQL 和 Flyway/Liquibase 做数据库版本管理
