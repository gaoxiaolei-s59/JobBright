# JobBacked 接口文档

本文档基于当前代码中的 `Controller`、DTO 和配置整理，覆盖主业务服务、RAG 流式服务和爬虫服务，适合作为前后端联调与后端自测参考。

最后更新：2026-05-08

## 1. 服务与通用约定

### 1.1 服务端口

| 模块 | 服务名 | 默认端口 | 说明 |
| --- | --- | ---: | --- |
| `job-backed` | `Job-backed` | `10010` | 用户、认证、首页、职位、简历主业务接口 |
| `job-rag` | `job-rag` | `10020` | RAG/LLM 流式对话接口 |
| `clawler` | - | `10030` | 职位采集与采集结果查询接口 |

本地示例地址：

```text
http://localhost:10010
http://localhost:10020
http://localhost:10030
```

### 1.2 主业务统一返回结构

`job-backed` 中除文件流接口外，大部分接口返回：

```json
{
  "code": "0",
  "message": null,
  "data": {},
  "requestId": null
}
```

说明：

- `code = "0"` 表示成功。
- `message` 成功时当前实现通常为空，失败时返回错误说明。
- `data` 为业务数据，无返回数据时为 `null`。
- `requestId` 字段存在于统一响应模型中，当前代码未统一赋值。

### 1.3 鉴权

`job-backed` 对 `/api/**` 启用登录态拦截，以下接口除外：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/send`
- `GET /api/auth/captcha`
- `GET /api/health`
- `OPTIONS /api/**`

其余主业务接口请求头需要携带：

```http
X-Access-Token: {accessToken}
```

Token 来自登录接口响应中的 `data.accessToken`。

### 1.4 CORS

`job-backed` 对 `/api/**` 允许跨域：

- `allowedOriginPatterns = *`
- `allowedMethods = *`
- `allowedHeaders = *`
- 暴露响应头：`X-Access-Token`

### 1.5 上传限制

`job-backed` 简历上传配置：

- 单文件最大：`50MB`
- 单请求最大：`100MB`
- `POST /api/user/resume/upload` 受 Redisson 信号量限流控制。
- 上传并发过高时返回 HTTP `429`：

```json
{
  "code": "429",
  "message": "当前上传人数过多，请稍后再试"
}
```

## 2. 主业务接口：job-backed

### 2.1 认证

#### 2.1.1 获取登录图形验证码

```http
GET /api/auth/captcha
```

鉴权：不需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "captchaKey": "captcha_abc",
    "imageData": "data:image/png;base64,...",
    "expiresIn": 300
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `captchaKey` | string | 图形验证码业务 Key，登录时传回 |
| `imageData` | string | 图片 Data URL，可直接赋值给 `img.src` |
| `expiresIn` | number | 过期时间，单位秒 |

#### 2.1.2 发送邮箱验证码

```http
POST /api/auth/send
Content-Type: application/json
```

鉴权：不需要。

请求体：

```json
{
  "email": "admin@example.com"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | string | 是 | 邮箱，后端校验非空和邮箱格式 |

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.1.3 用户注册

```http
POST /api/auth/register
Content-Type: application/json
```

鉴权：不需要。

请求体：

```json
{
  "username": "admin",
  "email": "admin@example.com",
  "displayName": "管理员",
  "password": "123456",
  "code": "123456"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 否 | 用户名 |
| `email` | string | 否 | 邮箱 |
| `displayName` | string | 否 | 显示名称 |
| `password` | string | 否 | 密码 |
| `code` | string | 否 | 邮箱验证码 |

说明：当前 DTO 未加 Bean Validation 注解，但业务层可能继续校验字段合法性。

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.1.4 用户登录

```http
POST /api/auth/login
Content-Type: application/json
```

鉴权：不需要。

请求体：

```json
{
  "account": "admin@example.com",
  "password": "123456",
  "captchaKey": "captcha_abc",
  "captchaCode": "8K3D"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `account` | string | 是 | 账号或邮箱 |
| `password` | string | 是 | 密码 |
| `captchaKey` | string | 是 | 图形验证码 Key |
| `captchaCode` | string | 是 | 用户输入的图形验证码 |

成功响应：

```json
{
  "code": "0",
  "data": {
    "accessToken": "xxxxxxxx"
  }
}
```

#### 2.1.5 获取当前用户

```http
GET /api/auth/me
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "userId": 10001,
    "username": "admin",
    "email": "admin@example.com",
    "displayName": "管理员"
  }
}
```

### 2.2 首页与用户

#### 2.2.1 首页概览

```http
GET /api/home/overview
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "freshJobCount": 1286,
    "averageMatchRate": 82,
    "averageShortlistMinutes": 9,
    "resumeScore": 88,
    "profileCompletionRate": 54
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `freshJobCount` | number | 新鲜岗位数量 |
| `averageMatchRate` | number | 平均匹配率 |
| `averageShortlistMinutes` | number | 平均入围耗时，单位分钟 |
| `resumeScore` | number | 简历评分 |
| `profileCompletionRate` | number | 用户资料完整度 |

#### 2.2.2 用户工作台

```http
GET /api/user/dashboard
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "displayName": "管理员",
    "planName": "Free",
    "resumeScore": 88,
    "profileCompletionRate": 54,
    "tips": ["完善简历可提升推荐效果"]
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `displayName` | string | 用户展示名称 |
| `planName` | string | 套餐名称 |
| `resumeScore` | number | 简历评分 |
| `profileCompletionRate` | number | 用户资料完整度 |
| `tips` | string[] | 求职提醒 |

#### 2.2.3 新手引导状态

```http
GET /api/user/onboarding/status
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "profileCompleted": true,
    "resumeUploaded": true,
    "onboardingCompleted": true
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `profileCompleted` | boolean | 是否已完成资料画像 |
| `resumeUploaded` | boolean | 是否已上传当前简历 |
| `onboardingCompleted` | boolean | 是否已完成新手引导 |

### 2.3 职位

职位列表类接口使用 `JobPageRequestV2` 作为查询参数。

通用查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `current` | number | 否 | MyBatis-Plus 页码参数 |
| `size` | number | 否 | MyBatis-Plus 每页条数参数 |
| `keyword` | string | 否 | 搜索关键词 |
| `country` | string | 否 | 国家 |
| `title` | string | 否 | 职位名称 |
| `city` | string | 否 | 城市 |
| `experienceLevel` | string | 否 | 经验等级 |
| `employmentType` | string | 否 | 用工类型 |
| `workMode` | string | 否 | 工作模式 |
| `datePosted` | string | 否 | 发布时间 |
| `industryName` | string | 否 | 行业名称 |
| `educationRequirement` | string | 否 | 学历要求 |

#### 2.3.1 推荐职位列表

```http
GET /api/jobs/recommended?current=1&size=10&city=上海&keyword=Java
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "total": 16,
    "hasMore": true,
    "records": [
      {
        "jobId": "JOB2001",
        "companyName": "某科技公司",
        "companyLogo": "https://example.com/logo.png",
        "title": "Java 后端开发实习生",
        "meta": "互联网 / 招聘平台",
        "postedAt": "1天前",
        "salaryRange": "5k-7k",
        "location": "上海 / 闵行区",
        "city": "上海",
        "district": "闵行区",
        "workMode": "现场办公",
        "employmentType": "实习",
        "experienceLevel": "STUDENT",
        "educationRequirement": "本科及以上",
        "preferredMajor": "计算机相关专业",
        "roleCategory": "后端开发",
        "internshipMonths": 5,
        "jobSummary": "参与简历画像、职位推荐和后台管理系统开发。",
        "skillTags": ["Java", "Spring Boot", "MySQL"],
        "highlightTags": ["画像联调", "推荐业务", "校招友好"],
        "applicantCount": 53,
        "matchScore": 98,
        "matchLabel": "高度匹配",
        "matchReason": "命中核心技能：Java、Spring Boot、MySQL",
        "applyUrl": "https://demo.example.com/jobs/JOB2001",
        "liked": false,
        "applied": false
      }
    ]
  }
}
```

职位记录字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `jobId` | string | 职位业务 ID |
| `companyName` | string | 公司名称 |
| `companyLogo` | string | 公司 Logo 地址 |
| `title` | string | 职位标题 |
| `meta` | string | 职位补充信息 |
| `postedAt` | string | 发布时间描述 |
| `salaryRange` | string | 薪资范围 |
| `location` | string | 工作地点展示文本 |
| `city` | string | 城市 |
| `district` | string | 区域 |
| `workMode` | string | 工作模式 |
| `employmentType` | string | 用工类型 |
| `experienceLevel` | string | 经验等级，例如 `STUDENT`、`NEW_GRAD`、`JUNIOR` |
| `educationRequirement` | string | 学历要求 |
| `preferredMajor` | string | 优先专业 |
| `roleCategory` | string | 角色类别 |
| `internshipMonths` | number | 实习时长要求，单位月 |
| `jobSummary` | string | 职位摘要 |
| `skillTags` | string[] | 技能标签 |
| `highlightTags` | string[] | 岗位亮点 |
| `applicantCount` | number | 投递人数 |
| `matchScore` | number | 匹配分数 |
| `matchLabel` | string | 匹配标签 |
| `matchReason` | string | 匹配原因 |
| `applyUrl` | string | 投递链接 |
| `liked` | boolean | 当前用户是否已收藏 |
| `applied` | boolean | 当前用户是否已投递 |

#### 2.3.2 收藏职位列表

```http
GET /api/jobs/favorites?current=1&size=10
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应结构：

```json
{
  "code": "0",
  "data": {
    "total": 3,
    "hasMore": false,
    "records": []
  }
}
```

说明：`records` 内部结构与推荐职位列表一致。

#### 2.3.3 已投递职位列表

```http
GET /api/jobs/applied?current=1&size=10
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应结构：

```json
{
  "code": "0",
  "data": {
    "total": 2,
    "hasMore": false,
    "records": []
  }
}
```

说明：`records` 内部结构与推荐职位列表一致。

#### 2.3.4 收藏职位

```http
POST /api/jobs/{jobId}/favorite
X-Access-Token: {accessToken}
```

鉴权：需要。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `jobId` | string | 是 | 职位业务 ID |

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.3.5 取消收藏职位

```http
DELETE /api/jobs/{jobId}/favorite
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.3.6 标记已投递

```http
POST /api/jobs/{jobId}/apply
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.3.7 取消已投递

```http
DELETE /api/jobs/{jobId}/apply
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

### 2.4 简历

#### 2.4.1 上传简历

```http
POST /api/user/resume/upload
X-Access-Token: {accessToken}
Content-Type: multipart/form-data
```

鉴权：需要。

表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | file | 是 | 简历文件 |

当前上传配置支持最大 50MB 单文件。业务层通常面向 `pdf`、`doc`、`docx` 简历文件。

成功响应：

```json
{
  "code": "0",
  "data": null
}
```

#### 2.4.2 获取当前简历

```http
GET /api/user/resume/current
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

```json
{
  "code": "0",
  "data": {
    "resumeId": "abc123",
    "fileName": "我的简历.pdf",
    "score": 88,
    "status": "ACTIVE",
    "uploadTime": "2026-05-08T10:00:00.000+00:00"
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `resumeId` | string | 简历 ID |
| `fileName` | string | 文件名 |
| `score` | number | 评分 |
| `status` | string | 状态 |
| `uploadTime` | string | 上传时间，Java `Date` 序列化格式取决于 Jackson 配置 |

#### 2.4.3 获取简历预览元信息

```http
GET /api/user/resume/{resumeId}/preview
X-Access-Token: {accessToken}
```

鉴权：需要。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `resumeId` | string | 是 | 简历 ID |

成功响应：

```json
{
  "code": "0",
  "data": {
    "resumeId": "abc123",
    "fileName": "我的简历.pdf",
    "fileExt": "pdf",
    "contentType": "application/pdf",
    "previewType": "INLINE",
    "previewUrl": "/api/user/resume/abc123/file",
    "downloadUrl": "/api/user/resume/abc123/file",
    "updatedAt": "2026-05-08 10:00:00",
    "score": {
      "grade": "A",
      "label": "优秀",
      "scoreValue": 88,
      "urgentFixCount": 1,
      "criticalFixCount": 2,
      "optionalFixCount": 3,
      "summary": "整体匹配度较高"
    },
    "profile": {
      "name": "张三",
      "title": "Java 后端开发",
      "location": "上海",
      "status": "应届生"
    },
    "analysisSummary": "简历结构清晰，项目描述可继续量化。",
    "analysisHighlights": [
      {
        "title": "技术栈匹配",
        "description": "覆盖 Java、Spring Boot、MySQL"
      }
    ],
    "urgentIssues": [
      {
        "title": "补充项目指标",
        "description": "建议增加性能、并发或业务结果数据"
      }
    ],
    "skillGroups": [
      {
        "title": "后端",
        "items": ["Java", "Spring Boot"]
      }
    ],
    "projects": [
      {
        "name": "JobBacked",
        "technologies": ["Java", "React"],
        "bullets": ["负责职位推荐接口开发"]
      }
    ]
  }
}
```

说明：`score`、`profile`、`analysisSummary` 等分析字段在 DTO 中已存在，实际是否有值取决于当前简历分析链路是否已产出数据。

#### 2.4.4 获取简历原始文件流

```http
GET /api/user/resume/{resumeId}/file
X-Access-Token: {accessToken}
```

鉴权：需要。

成功响应：

- HTTP `200 OK`
- Body 为文件二进制流
- 响应类型由文件 `contentType` 决定

响应头示例：

```http
Content-Type: application/pdf
Content-Disposition: inline; filename="resume.pdf"
Cache-Control: no-store
```

### 2.5 健康检查

#### 2.5.1 服务健康检查

```http
GET /api/health
```

鉴权：不需要。

该接口返回 `ApiResponse`，不是 `Result`。

成功响应：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "status": "UP",
    "service": "job-backed"
  }
}
```

## 3. RAG 流式接口：job-rag

### 3.1 流式对话

```http
POST /api/rag/stream/chat
Content-Type: application/json
Accept: text/event-stream
```

服务地址：

```text
http://localhost:10020/api/rag/stream/chat
```

请求体：

```json
{
  "streamId": "stream-001",
  "modelId": "qwen3-max",
  "systemPrompt": "你是一个求职助手",
  "userPrompt": "帮我优化这段项目经历",
  "temperature": 0.7,
  "topP": 0.8,
  "topK": 20,
  "maxTokens": 1024,
  "thinking": false,
  "messages": [
    {
      "role": "USER",
      "content": "我想找 Java 实习"
    }
  ]
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `streamId` | string | 否 | 前端自定义流 ID，不传则服务端生成 |
| `modelId` | string | 否 | 模型 ID，不传则按配置选择默认候选模型 |
| `systemPrompt` | string | 否 | 系统提示词 |
| `userPrompt` | string | 否 | 用户本轮输入，会追加到 `messages` 之后 |
| `temperature` | number | 否 | 采样温度 |
| `topP` | number | 否 | Top P |
| `topK` | number | 否 | Top K |
| `maxTokens` | number | 否 | 最大输出 Token 数 |
| `thinking` | boolean | 否 | 是否选择支持思考的模型并开启思考 |
| `messages` | object[] | 否 | 历史消息 |

`messages` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `role` | string | `SYSTEM`、`USER`、`ASSISTANT` |
| `content` | string | 消息内容 |
| `thinkingContent` | string | 助手思考内容，可选 |
| `thinkingDuration` | number | 思考耗时，单位秒，可选 |

SSE 事件：

| 事件名 | data 示例 | 说明 |
| --- | --- | --- |
| `open` | `{"streamId":"stream-001","modelId":"qwen3-max","provider":"bailian"}` | 连接建立，返回实际流 ID 和模型信息 |
| `delta` | `{"text":"你好"}` | 增量文本片段 |
| `done` | `{"streamId":"stream-001"}` | 输出完成 |
| `cancelled` | `{"streamId":"stream-001"}` | 流被取消 |
| `error` | `{"message":"大模型流式调用失败"}` | 流式调用失败 |

前端处理示例：

```js
const response = await fetch("http://localhost:10020/api/rag/stream/chat", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Accept: "text/event-stream"
  },
  body: JSON.stringify({
    streamId: "stream-001",
    userPrompt: "帮我总结这份简历"
  })
});
```

### 3.2 取消流式对话

```http
DELETE /api/rag/stream/chat/{streamId}
```

服务地址：

```text
http://localhost:10020/api/rag/stream/chat/stream-001
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `streamId` | string | 是 | 要取消的流 ID |

成功响应：

```json
{
  "code": "0",
  "data": {
    "streamId": "stream-001",
    "cancelled": true
  }
}
```

## 4. 爬虫接口：clawler

`clawler` 模块当前接口直接返回业务对象或 MyBatis-Plus `Page`，不使用 `job-backed` 的 `Result` 包装。

服务地址：

```text
http://localhost:10030
```

### 4.1 查询已采集职位

```http
GET /api/jobs?keyword=Java&sourceSite=zhaopin&page=1&size=20
```

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `keyword` | string | 否 | - | 关键词 |
| `sourceSite` | string | 否 | - | 来源站点 |
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `20` | 每页条数 |

成功响应为 MyBatis-Plus `Page<JobPosting>`，结构示例：

```json
{
  "records": [
    {
      "id": 1,
      "jobId": "123",
      "sourceSite": "zhaopin",
      "companyId": "456",
      "title": "Java 后端开发",
      "company": "某公司",
      "location": "上海",
      "salary": "10k-15k",
      "summary": "职位摘要",
      "sourceUrl": "https://example.com/job/123",
      "sourceKey": "zhaopin:123",
      "publishTime": "2026-05-08 10:00:00",
      "crawledAt": "2026-05-08 10:01:00",
      "createdAt": "2026-05-08 10:01:00",
      "updatedAt": "2026-05-08 10:01:00"
    }
  ],
  "total": 1,
  "size": 20,
  "current": 1,
  "pages": 1
}
```

### 4.2 通用页面采集

```http
POST /api/crawler/crawl
Content-Type: application/json
```

请求体：

```json
{
  "siteName": "demo",
  "pageUrl": "https://example.com/jobs",
  "itemSelector": ".job-card",
  "title": {
    "selector": ".job-title",
    "attribute": null,
    "required": true
  },
  "company": {
    "selector": ".company",
    "attribute": null,
    "required": false
  },
  "location": {
    "selector": ".location",
    "attribute": null,
    "required": false
  },
  "salary": {
    "selector": ".salary",
    "attribute": null,
    "required": false
  },
  "summary": {
    "selector": ".summary",
    "attribute": null,
    "required": false
  },
  "jobLink": {
    "selector": "a",
    "attribute": "href",
    "required": false
  },
  "maxItems": 20
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `siteName` | string | 是 | 站点名称 |
| `pageUrl` | string | 是 | 要采集的页面 URL |
| `itemSelector` | string | 是 | 职位卡片 CSS 选择器 |
| `title` | object | 是 | 标题字段选择器 |
| `company` | object | 否 | 公司字段选择器 |
| `location` | object | 否 | 地点字段选择器 |
| `salary` | object | 否 | 薪资字段选择器 |
| `summary` | object | 否 | 摘要字段选择器 |
| `jobLink` | object | 否 | 职位链接字段选择器 |
| `maxItems` | number | 否 | 最多采集数量 |

选择器对象字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `selector` | string | CSS 选择器 |
| `attribute` | string | 读取的属性名；为空时读取文本 |
| `required` | boolean | 字段是否必需 |

成功响应：

```json
{
  "totalFetched": 20,
  "insertedCount": 18,
  "updatedCount": 2,
  "postings": []
}
```

### 4.3 智联招聘单页采集

```http
POST /api/zhaopin/crawl
Content-Type: application/json
```

请求体：

```json
{
  "keyword": "Java",
  "cityId": 538,
  "page": 1,
  "pageSize": 20,
  "workExperience": null,
  "education": null,
  "companyType": null,
  "employmentType": null
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `keyword` | string | 是 | 搜索关键词 |
| `cityId` | number | 否 | 智联城市 ID |
| `page` | number | 否 | 页码，最小 1 |
| `pageSize` | number | 否 | 每页数量，1 到 90 |
| `workExperience` | number | 否 | 工作经验筛选码 |
| `education` | number | 否 | 学历筛选码 |
| `companyType` | number | 否 | 公司类型筛选码 |
| `employmentType` | number | 否 | 雇佣类型筛选码 |

成功响应：

```json
{
  "page": 1,
  "pageSize": 20,
  "totalFetched": 20,
  "insertedCount": 18,
  "updatedCount": 2,
  "postings": []
}
```

### 4.4 智联招聘批量采集

```http
POST /api/zhaopin/crawl/batch
Content-Type: application/json
```

请求体：

```json
{
  "keyword": "Java",
  "cityId": 538,
  "startPage": 1,
  "pageSize": 20,
  "targetCount": 100,
  "maxPages": 5,
  "workExperience": null,
  "education": null,
  "companyType": null,
  "employmentType": null
}
```

成功响应：

```json
{
  "startPage": 1,
  "endPage": 5,
  "pageSize": 20,
  "targetCount": 100,
  "totalFetched": 100,
  "insertedCount": 90,
  "updatedCount": 10,
  "postings": []
}
```

### 4.5 猎聘单页采集

```http
POST /api/liepin/crawl
Content-Type: application/json
```

请求体：

```json
{
  "keyword": "Java",
  "cityCode": "010",
  "page": 1,
  "pageSize": 20
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `keyword` | string | 是 | 搜索关键词 |
| `cityCode` | string | 否 | 猎聘城市编码 |
| `page` | number | 否 | 页码，最小 1 |
| `pageSize` | number | 否 | 每页数量，1 到 90 |

成功响应：

```json
{
  "page": 1,
  "pageSize": 20,
  "totalFetched": 20,
  "insertedCount": 18,
  "updatedCount": 2,
  "postings": []
}
```

### 4.6 猎聘批量采集

```http
POST /api/liepin/crawl/batch
Content-Type: application/json
```

请求体：

```json
{
  "keyword": "Java",
  "cityCode": "010",
  "startPage": 1,
  "pageSize": 20,
  "targetCount": 100,
  "maxPages": 5
}
```

成功响应：

```json
{
  "startPage": 1,
  "endPage": 5,
  "pageSize": 20,
  "targetCount": 100,
  "totalFetched": 100,
  "insertedCount": 90,
  "updatedCount": 10,
  "postings": []
}
```

## 5. 联调建议

主业务前端推荐按下面顺序联调：

1. `GET /api/auth/captcha`
2. `POST /api/auth/login`
3. `GET /api/auth/me`
4. `GET /api/user/onboarding/status`
5. `POST /api/user/resume/upload`
6. `GET /api/user/resume/current`
7. `GET /api/home/overview`
8. `GET /api/jobs/recommended`
9. `POST /api/jobs/{jobId}/favorite`
10. `POST /api/jobs/{jobId}/apply`
11. `GET /api/user/resume/{resumeId}/preview`
12. `GET /api/user/resume/{resumeId}/file`

RAG 前端推荐先调通：

1. `POST /api/rag/stream/chat`
2. `DELETE /api/rag/stream/chat/{streamId}`

爬虫服务推荐先调通：

1. `GET /api/jobs`
2. `POST /api/zhaopin/crawl`
3. `POST /api/liepin/crawl`

## 6. 当前接口清单

### 6.1 job-backed

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/auth/captcha` | 否 | 获取登录图形验证码 |
| `POST` | `/api/auth/send` | 否 | 发送邮箱验证码 |
| `POST` | `/api/auth/register` | 否 | 用户注册 |
| `POST` | `/api/auth/login` | 否 | 用户登录 |
| `GET` | `/api/auth/me` | 是 | 获取当前用户 |
| `GET` | `/api/home/overview` | 是 | 首页概览 |
| `GET` | `/api/user/dashboard` | 是 | 用户工作台 |
| `GET` | `/api/user/onboarding/status` | 是 | 新手引导状态 |
| `GET` | `/api/jobs/recommended` | 是 | 推荐职位列表 |
| `GET` | `/api/jobs/favorites` | 是 | 收藏职位列表 |
| `GET` | `/api/jobs/applied` | 是 | 已投递职位列表 |
| `POST` | `/api/jobs/{jobId}/favorite` | 是 | 收藏职位 |
| `DELETE` | `/api/jobs/{jobId}/favorite` | 是 | 取消收藏 |
| `POST` | `/api/jobs/{jobId}/apply` | 是 | 标记已投递 |
| `DELETE` | `/api/jobs/{jobId}/apply` | 是 | 取消已投递 |
| `POST` | `/api/user/resume/upload` | 是 | 上传简历 |
| `GET` | `/api/user/resume/current` | 是 | 获取当前简历 |
| `GET` | `/api/user/resume/{resumeId}/preview` | 是 | 获取简历预览元信息 |
| `GET` | `/api/user/resume/{resumeId}/file` | 是 | 获取简历原始文件流 |
| `GET` | `/api/health` | 否 | 健康检查 |

### 6.2 job-rag

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/rag/stream/chat` | RAG/LLM 流式对话 |
| `DELETE` | `/api/rag/stream/chat/{streamId}` | 取消流式对话 |

### 6.3 clawler

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/jobs` | 查询已采集职位 |
| `POST` | `/api/crawler/crawl` | 通用页面采集 |
| `POST` | `/api/zhaopin/crawl` | 智联招聘单页采集 |
| `POST` | `/api/zhaopin/crawl/batch` | 智联招聘批量采集 |
| `POST` | `/api/liepin/crawl` | 猎聘单页采集 |
| `POST` | `/api/liepin/crawl/batch` | 猎聘批量采集 |

## 7. 代码位置

主要接口代码：

- [AuthController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/AuthController.java)
- [HomeController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/HomeController.java)
- [UserController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/UserController.java)
- [JobController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/JobController.java)
- [UserResumeController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/UserResumeController.java)
- [HealthController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/controller/HealthController.java)
- [ChatStreamController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-rag/src/main/java/org/puregxl/site/rag/stream/ChatStreamController.java)
- [JobPostingController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/clawler/src/main/java/org/puregxl/site/clawler/controller/JobPostingController.java)
- [JobCrawlerController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/clawler/src/main/java/org/puregxl/site/clawler/controller/JobCrawlerController.java)
- [ZhaopinCrawlerController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/clawler/src/main/java/org/puregxl/site/clawler/controller/ZhaopinCrawlerController.java)
- [LiepinCrawlerController.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/clawler/src/main/java/org/puregxl/site/clawler/controller/LiepinCrawlerController.java)

鉴权、响应和配置代码：

- [UserConfiguration.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/config/UserConfiguration.java)
- [AuthConstant.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/common/constant/AuthConstant.java)
- [UploadRateLimitFilter.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-backed/src/main/java/org/puregxl/site/jobbacked/filter/UploadRateLimitFilter.java)
- [Result.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-framework/src/main/java/org/puregxl/site/framework/result/Result.java)
- [Results.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-framework/src/main/java/org/puregxl/site/framework/web/Results.java)
- [ApiResponse.java](/Users/gaoxaiolei/IdeaProjects/JobBacked/job-framework/src/main/java/org/puregxl/site/framework/common/ApiResponse.java)
