# 简历上传与文件存储模块说明

## 1. 模块目标

本文档用于说明当前项目中“用户简历上传、对象存储、文件索引、RAG 文件处理”相关功能的现状，方便后续继续扩展与补充文档。

当前设计遵循以下原则：

- `job-backed` 负责业务主流程
- `job-framework` 负责公共基础能力
- `job-rag` 负责 RAG 侧文件处理与下载能力
- 对象存储使用本地 `RustFS`

## 2. 当前模块划分

### 2.1 `job-backed`

负责：

- 用户登录态校验
- 当前用户上下文获取
- 用户上传简历业务
- 简历文件业务记录落库

### 2.2 `job-rag`

负责：

- 文件上传到对象存储
- 文件索引读取
- 文件下载
- 后续预留文本解析、切块、向量化处理能力

### 2.3 `job-framework`

负责：

- 统一返回结构
- 全局异常处理
- MyBatis-Plus 公共配置

## 3. 当前已完成功能

## 3.1 登录鉴权

已完成：

- 用户注册
- 用户登录
- 当前用户信息获取
- 登录后通过请求头 `X-Access-Token` 传递 uuid
- 后端从 Redis 获取用户信息
- 用户信息放入 `ThreadLocal`

相关位置：

- [AuthController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/AuthController.java)
- [UserConfiguration.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/config/UserConfiguration.java)
- [UserContext.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/common/context/UserContext.java)

## 3.2 用户简历文件表

当前业务库 `job_backed` 已使用 `user_resume_file` 表保存用户简历文件业务信息。

当前实体类：

- [UserResumeFile.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/dao/entity/UserResumeFile.java)

当前字段用于保存：

- 用户 ID
- 简历 ID
- 文件名
- 文件后缀
- 文件类型
- 文件大小
- 对象存储路径
- 文件访问地址
- 是否当前生效
- 创建时间、更新时间、逻辑删除标识

## 3.3 用户上传简历

当前已完成基础上传能力：

- 接口：`POST /api/user/resume/upload`
- 上传参数：`multipart/form-data`
- 表单字段：`file`
- 接口要求登录态
- 自动从当前用户上下文中获取 `userId`
- 上传文件到 RustFS
- 成功后写入 `user_resume_file`
- 如果存在当前生效简历，会把旧记录 `is_current` 更新为 `0`

相关位置：

- [UserResumeController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/UserResumeController.java)
- [UserResumeService.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/UserResumeService.java)
- [UserResumeServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/impl/UserResumeServiceImpl.java)

## 3.4 通用文件上传能力

当前 `job-backed` 中已经抽出通用文件存储服务，用于将文件上传到 RustFS。

当前职责：

- 统一处理对象存储上传
- 自动检查并创建 bucket
- 返回文件访问路径等信息

相关位置：

- [FileStorageService.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/FileStorageService.java)
- [FileStorageServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/service/impl/FileStorageServiceImpl.java)
- [UploadFileInfo.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/dto/file/UploadFileInfo.java)

## 3.5 RustFS 对象存储配置

当前本地对象存储配置如下：

```yml
rustfs:
  url: http://localhost:9000
  access-key-id: rustfsadmin
  secret-access-key: rustfsadmin
```

相关配置类：

- [RustfsProperties.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/config/RustfsProperties.java)
- [RustfsConfiguration.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/config/RustfsConfiguration.java)

## 3.6 RAG 文件接口

`job-rag` 当前已具备以下基础文件能力：

- 文件上传
- 文件详情查询
- 文件下载

当前接口：

- `POST /api/rag/file/upload`
- `GET /api/rag/file/{fileId}`
- `GET /api/rag/file/{fileId}/download`

相关位置：

- [FileController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/controller/FileController.java)
- [FileService.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/service/FileService.java)
- [FileServiceImpl.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/service/impl/FileServiceImpl.java)

## 4. 当前上传链路说明

### 4.1 用户端上传简历链路

当前流程如下：

1. 用户登录成功
2. 前端请求头携带 `X-Access-Token`
3. 后端从 Redis 获取当前用户信息
4. 用户调用 `POST /api/user/resume/upload`
5. `job-backed` 获取 `MultipartFile`
6. `job-backed` 调用通用文件上传服务上传到 RustFS
7. 上传成功后写入 `user_resume_file`
8. 若用户已有当前生效简历，则旧简历标记为非当前版本

### 4.2 RAG 文件处理链路

当前流程如下：

1. 调用 `job-rag` 上传文件接口
2. 文件上传至 RustFS
3. 写入 `rag_file`
4. 可通过文件详情接口查询索引
5. 可通过下载接口读取真实文件

## 5. 当前限制与说明

当前版本仍属于第一阶段可用版本，存在以下限制：

- 用户简历上传目前为同步上传
- 暂未接入 RocketMQ 异步处理
- 暂未实现简历文本解析与切块
- 暂未实现简历列表查询接口
- 暂未实现当前用户最新简历详情接口
- 暂未实现简历删除与版本回滚

## 6. 后续建议扩展章节

后续可以按以下章节继续扩展文档。

## 6.1 用户简历业务能力

建议后续补充：

- 查询当前用户当前简历
- 查询当前用户简历历史版本
- 删除简历
- 设置当前生效简历
- 下载用户简历

## 6.2 文件校验与安全控制

建议后续补充：

- 上传文件大小限制
- 文件类型限制
- 文件内容安全校验
- 病毒扫描或可疑文件校验

## 6.3 RAG 解析能力

建议后续补充：

- 使用 Tika 提取文本
- 简历文本切块
- 切块结果入库
- 后续接入向量库

## 6.4 异步化处理

建议后续补充：

- `job-backed` 上传成功后发送 RocketMQ 消息
- `job-rag` 异步消费消息
- 文件解析状态跟踪
- 失败重试与补偿机制

## 6.5 前端能力

建议后续补充：

- 用户上传简历页面
- 当前简历信息展示
- 历史简历列表
- 简历下载按钮
- 简历解析状态展示

## 7. 当前接口汇总

### 7.1 认证接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### 7.2 用户简历接口

- `POST /api/user/resume/upload`

### 7.3 RAG 文件接口

- `POST /api/rag/file/upload`
- `GET /api/rag/file/{fileId}`
- `GET /api/rag/file/{fileId}/download`

## 8. 当前配置建议

简历上传建议限制：

```yml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
```

原因：

- 简历通常不需要过大文件
- 可以降低上传和解析时的内存压力
- 减少对象存储和后续异步处理压力

## 9. 推荐后续实施顺序

建议按以下顺序继续开发：

1. 完善用户简历查询接口
2. 增加简历下载接口
3. 增加文件大小和类型校验
4. 打通 `job-backed -> job-rag` 的消息通知
5. 接入 Tika 文本提取
6. 完成切块与入库
7. 后续接入向量检索
