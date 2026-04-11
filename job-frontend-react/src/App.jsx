import { useEffect, useRef, useState } from "react";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") || "http://localhost:10010";
const TOKEN_KEY = "jobbright_access_token";
const TOKEN_HEADER = "X-Access-Token";

const loginInitialState = {
  account: "demo",
  password: "JobBacked123",
  captchaKey: "",
  captchaCode: ""
};

const registerInitialState = {
  username: "",
  email: "",
  displayName: "",
  password: "",
  verificationCode: ""
};

const sidebarItems = [
  { key: "jobs", label: "职位推荐", badge: null, icon: "briefcase" },
  { key: "resume", label: "简历中心", badge: null, icon: "document" },
  { key: "profile", label: "个人资料", badge: null, icon: "user" },
  { key: "agent", label: "求职助手", badge: null, icon: "spark" },
  { key: "coach", label: "求职辅导", badge: "新", icon: "target" }
];

const savedFilters = ["后端开发 · 上海", "校招岗位 · Java / Go", "AI 平台 · 全职"];

const userDashboardFallback = {
  displayName: "演示用户",
  planName: "免费版",
  resumeScore: 82,
  profileCompletionRate: 42,
  tips: ["上传最新简历", "设置期望城市", "补充岗位关键词"]
};

const userProfileDraftInitialState = {
  expectedCity: "",
  targetRole: "",
  jobTypes: [],
  openToRemote: true,
  requireVisaSupport: false,
  keywordTags: "",
  personalSummary: ""
};

const onboardingJobTypeOptions = ["全职", "合同工", "兼职", "实习"];

const settingsDraftInitialState = {
  emailNotifications: true,
  weeklyReport: true,
  pushNewMatches: true,
  autoLoadMoreJobs: true,
  compactCards: false,
  showMatchReason: true,
  privateProfile: false,
  shareResumeScore: true
};

const planOptions = [
  {
    code: "FREE",
    name: "免费版",
    price: "¥0 / 月",
    description: "适合先跑通简历上传、岗位筛选和求职节奏管理。",
    features: ["基础职位推荐", "简历上传与评分", "个人资料工作台"]
  },
  {
    code: "PLUS",
    name: "加速版",
    price: "¥39 / 月",
    description: "适合正在集中投递的同学，优先提高筛选和跟进效率。",
    features: ["高级筛选组合", "每周简历优化建议", "重点岗位追踪提醒"]
  },
  {
    code: "PRO",
    name: "冲刺版",
    price: "¥99 / 月",
    description: "适合秋招冲刺阶段，强化匹配、提醒和求职助手联动。",
    features: ["优先岗位洞察", "多份简历管理", "个性化投递建议"]
  }
];

const jobFilterInitialState = {
  keyword: "",
  country: "",
  title: "",
  city: "",
  experienceLevel: "",
  employmentType: "",
  workMode: "",
  datePosted: "",
  industryName: "",
  educationRequirement: "",
  current: 1,
  size: 10
};

const experienceLevelOptions = [
  { value: "STUDENT", label: "在校生" },
  { value: "NEW_GRAD", label: "应届生" },
  { value: "JUNIOR", label: "初级岗位" },
  { value: "MID_LEVEL", label: "中级岗位" },
  { value: "SENIOR", label: "高级岗位" }
];

const experienceLevelLabelMap = Object.fromEntries(
  experienceLevelOptions.map((item) => [item.value, item.label])
);

const countryOptions = [
  { value: "", label: "全部国家" },
  { value: "中国大陆", label: "中国大陆" }
];

const cityOptions = [
  { value: "", label: "全部城市" },
  { value: "上海", label: "上海" },
  { value: "北京", label: "北京" },
  { value: "深圳", label: "深圳" },
  { value: "杭州", label: "杭州" },
  { value: "广州", label: "广州" }
];

const employmentTypeOptions = [
  { value: "", label: "全部类型" },
  { value: "全职", label: "全职" },
  { value: "实习", label: "实习" },
  { value: "校招", label: "校招" }
];

const workModeOptions = [
  { value: "", label: "全部模式" },
  { value: "现场办公", label: "现场办公" },
  { value: "混合办公", label: "混合办公" },
  { value: "远程办公", label: "远程办公" }
];

const datePostedOptions = [
  { value: "", label: "全部时间" },
  { value: "24h", label: "24 小时内" },
  { value: "3d", label: "3 天内" },
  { value: "7d", label: "7 天内" }
];

const industryOptions = [
  { value: "", label: "全部行业" },
  { value: "互联网", label: "互联网" },
  { value: "制造业", label: "制造业" },
  { value: "企业 SaaS", label: "企业 SaaS" },
  { value: "云计算", label: "云计算" }
];

const educationOptions = [
  { value: "", label: "全部学历" },
  { value: "大专及以上", label: "大专及以上" },
  { value: "本科", label: "本科" },
  { value: "本科及以上", label: "本科及以上" },
  { value: "硕士及以上", label: "硕士及以上" }
];

const sizeOptions = [
  { value: 10, label: "10" },
  { value: 20, label: "20" },
  { value: 50, label: "50" }
];

const recommendRequestFieldNames = [
  "keyword",
  "country",
  "title",
  "city",
  "experienceLevel",
  "employmentType",
  "workMode",
  "datePosted",
  "industryName",
  "educationRequirement",
  "current",
  "size"
];

const jobTabs = ["推荐职位", "收藏职位", "已投递"];
const jobTabEndpointMap = {
  推荐职位: "/api/jobs/recommended",
  收藏职位: "/api/jobs/favorites",
  已投递: "/api/jobs/applied"
};

const profileTabItems = [
  { key: "basic", label: "个人信息" },
  { key: "education", label: "教育经历" },
  { key: "work", label: "工作经历" },
  { key: "skills", label: "技能标签" }
];

const profilePersonalEditorInitialState = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  countryRegion: "",
  city: "",
  county: "",
  postalCode: "",
  addressLine: "",
  linkedInUrl: "",
  githubUrl: ""
};

const profileWorkEditorInitialState = {
  jobTitle: "",
  company: "",
  jobType: "",
  location: "",
  startDate: "",
  endDate: "",
  currentlyWorkHere: true,
  experienceSummary: "",
  jobDescriptionBullets: []
};

const profileSkillsEditorInitialState = {
  items: [],
  pendingSkill: ""
};

const profileEducationEditorInitialState = {
  school: "",
  degree: "",
  fieldOfStudy: "",
  location: "",
  startDate: "",
  endDate: "",
  highlights: ""
};

const profilePreferencesEditorInitialState = {
  targetRole: "",
  expectedCity: "",
  jobTypes: [],
  openToRemote: true,
  requireVisaSupport: false
};

const jobItems = [
  {
    id: "job-1",
    company: "三星",
    companyLogo: "",
    title: "后端开发实习生",
    meta: "三星电子 / 制造业 / 成长期",
    postedAt: "6 小时前",
    location: "上海",
    city: "上海",
    district: "浦东新区",
    workMode: "现场办公",
    employmentType: "实习",
    experienceLevel: "STUDENT",
    educationRequirement: "本科",
    roleCategory: "后端开发",
    salaryRange: "250-300/天",
    jobSummary: "参与招聘平台接口开发与测试，适合想进入 Java 后端方向的同学。",
    skillTags: ["Java", "Spring Boot", "MySQL"],
    highlightTags: ["实习转正", "导师带教", "业务核心"],
    applicants: 54,
    match: 82,
    sponsor: "岗位画像匹配度较高",
    cta: "一键投递",
    brand: "S",
    theme: "dark"
  },
  {
    id: "job-2",
    company: "Informatica",
    companyLogo: "",
    title: "软件工程师 AMTS（校招生）",
    meta: "数据平台 / 云计算 / 上市公司",
    postedAt: "10 小时前",
    location: "北京",
    city: "北京",
    district: "海淀区",
    workMode: "混合办公",
    employmentType: "全职",
    experienceLevel: "NEW_GRAD",
    educationRequirement: "本科及以上",
    roleCategory: "平台研发",
    salaryRange: "28k-38k * 15薪",
    jobSummary: "负责数据平台服务和云端应用研发，校招友好。",
    skillTags: ["Java", "SQL", "云计算"],
    highlightTags: ["上市公司", "导师培养", "成长路径清晰"],
    applicants: 72,
    match: 98,
    sponsor: "高度匹配，建议优先投递",
    cta: "立即申请",
    brand: "I",
    theme: "orange"
  },
  {
    id: "job-3",
    company: "Salesforce",
    companyLogo: "",
    title: "平台后端工程师（校招）",
    meta: "企业 SaaS / AI Cloud / 上市公司",
    postedAt: "14 小时前重新发布",
    location: "深圳",
    city: "深圳",
    district: "南山区",
    workMode: "混合办公",
    employmentType: "全职",
    experienceLevel: "JUNIOR",
    educationRequirement: "本科及以上",
    roleCategory: "推荐平台",
    salaryRange: "24k-34k * 14薪",
    jobSummary: "参与推荐链路、画像服务与企业级平台后端建设。",
    skillTags: ["Java", "Redis", "推荐系统"],
    highlightTags: ["校招友好", "平台型业务", "核心项目"],
    applicants: 108,
    match: 93,
    sponsor: "和你的简历关键词高度重合",
    cta: "查看岗位",
    brand: "SF",
    theme: "blue"
  }
];

const mockRecommendedJobs = jobItems.map((job, index) => ({
  jobId: job.id,
  companyName: job.company,
  companyLogo: job.companyLogo,
  title: job.title,
  meta: job.meta,
  postedAt: job.postedAt,
  salaryRange: job.salaryRange,
  location: job.location,
  city: job.city,
  district: job.district,
  workMode: job.workMode,
  employmentType: job.employmentType,
  experienceLevel: job.experienceLevel,
  educationRequirement: job.educationRequirement,
  roleCategory: job.roleCategory,
  jobSummary: job.jobSummary,
  skillTags: job.skillTags,
  highlightTags: job.highlightTags,
  applicantCount: job.applicants,
  matchScore: job.match,
  matchLabel: job.match >= 95 ? "高度匹配" : "较高匹配",
  matchReason: job.sponsor,
  applyUrl: "",
  liked: false,
  applied: false,
  brand: job.brand,
  theme: job.theme,
  companyId: `comp-${index + 1}`
}));

function createJobListState(records = [], total = 0, hasMore = false) {
  return {
    total,
    hasMore,
    records
  };
}

function getUserScopedStorageKey(user, scope) {
  const userIdentifier = user?.userId || user?.username || user?.email || "guest";
  return `jobbright_${scope}_${userIdentifier}`;
}

function readLocalJson(key, fallbackValue) {
  try {
    const rawValue = localStorage.getItem(key);
    if (!rawValue) {
      return fallbackValue;
    }
    return JSON.parse(rawValue);
  } catch {
    return fallbackValue;
  }
}

function writeLocalJson(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

function normalizeBoolean(value) {
  return value === true || value === 1;
}

function getCompanyBadgeText(job) {
  if (job.brand) {
    return job.brand;
  }
  return (job.companyName || "企业").slice(0, 2).toUpperCase();
}

function getExperienceLevelLabel(level) {
  return experienceLevelLabelMap[level] || level || "";
}

function isNonEmptyValue(value) {
  return value !== null && value !== undefined && String(value).trim() !== "";
}

function getJobMetaLine(job) {
  if (isNonEmptyValue(job.meta)) {
    return job.meta;
  }
  if (isNonEmptyValue(job.companyName)) {
    return job.companyName;
  }
  return "";
}

function getJobKeyItems(job) {
  return [
    job.location,
    job.workMode,
    job.employmentType,
    getExperienceLevelLabel(job.experienceLevel),
    job.salaryRange || "薪资面议"
  ].filter(isNonEmptyValue);
}

function getJobDetailItems(job) {
  return [
    { icon: "location", label: job.location },
    { icon: "calendar", label: job.employmentType },
    { icon: "desk", label: job.workMode },
    { icon: "badge", label: getExperienceLevelLabel(job.experienceLevel) },
    { icon: "salary", label: job.salaryRange || "薪资面议" },
    { icon: "document", label: job.educationRequirement || "学历不限" }
  ].filter((item) => isNonEmptyValue(item.label));
}

function findJobById(jobId, dataSources, overrides = {}) {
  if (!jobId) {
    return null;
  }
  for (const source of dataSources) {
    const records = Array.isArray(source?.records) ? source.records : [];
    const matched = records.find((item) => item.jobId === jobId);
    if (matched) {
      const override = overrides[jobId] || {};
      return {
        ...matched,
        liked: override.liked ?? matched.liked,
        applied: override.applied ?? matched.applied
      };
    }
  }
  return null;
}

function getJobTagGroups(job) {
  return [
    { title: "技能标签", values: job.skillTags || [] },
    { title: "岗位亮点", values: job.highlightTags || [] },
    { title: "优先专业", values: job.preferredMajor ? String(job.preferredMajor).split(/[、,，/]/).filter(Boolean) : [] }
  ].filter((group) => group.values.length);
}

function getProfileKeywordTags(value) {
  return String(value || "")
    .split(/[，,、/\n]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function getProfileSummaryBullets(value) {
  return String(value || "")
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function getProfileLocationText(profileDraft) {
  const city = profileDraft?.expectedCity?.trim();
  if (city && profileDraft?.openToRemote) {
    return `${city} · 接受远程`;
  }
  if (city) {
    return city;
  }
  if (profileDraft?.openToRemote) {
    return "接受远程办公";
  }
  return "待补充城市";
}

function getAuthDisplayName(user) {
  return user?.displayName || user?.username || "U";
}

function splitDisplayName(value) {
  const parts = String(value || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  return {
    firstName: parts[0] || "",
    lastName: parts.slice(1).join(" ")
  };
}

function buildProfilePersonalEditorDraft(authUser, profileDraft) {
  const displayName = getAuthDisplayName(authUser);
  const { firstName, lastName } = splitDisplayName(displayName);
  return {
    ...profilePersonalEditorInitialState,
    firstName,
    lastName,
    email: authUser?.email || "",
    phone: "",
    countryRegion: "中国大陆",
    city: profileDraft?.expectedCity?.trim() || "",
    linkedInUrl: "",
    githubUrl: ""
  };
}

function buildProfileWorkEditorDraft(profileDraft) {
  const summaryBullets = getProfileSummaryBullets(profileDraft?.personalSummary);
  return {
    ...profileWorkEditorInitialState,
    jobTitle: profileDraft?.targetRole?.trim() || "后端开发实习生",
    company: "[公司名称]",
    jobType: Array.isArray(profileDraft?.jobTypes) && profileDraft.jobTypes.length > 0
      ? profileDraft.jobTypes[0]
      : "实习",
    location: profileDraft?.expectedCity?.trim() || "[城市, 国家]",
    experienceSummary: "补充一段 1-2 句话的职责概述，让招聘方先快速理解你的角色定位。",
    jobDescriptionBullets: summaryBullets.length
      ? summaryBullets
      : [
        "与前端团队协作开发内部工具，基于 Spring Boot 提升团队协作效率。",
        "协助将旧系统迁移到微服务架构，提升系统可维护性与扩展性。",
        "编写单元测试并参与代码评审，保障接口质量与系统稳定性。"
      ]
  };
}

function buildProfileSkillsEditorDraft(profileDraft) {
  const items = getProfileKeywordTags(profileDraft?.keywordTags);
  return {
    ...profileSkillsEditorInitialState,
    items: items.length
      ? items
      : ["Java", "Python", "SQL", "C++", "Spring Boot", "MyBatis", "Django", "MySQL", "Redis", "RabbitMQ", "Docker", "Git"],
    pendingSkill: ""
  };
}

function buildProfileEducationEditorDraft() {
  return {
    ...profileEducationEditorInitialState,
    highlights: ""
  };
}

function buildProfilePreferencesEditorDraft(profileDraft) {
  return {
    ...profilePreferencesEditorInitialState,
    targetRole: profileDraft?.targetRole?.trim() || "",
    expectedCity: profileDraft?.expectedCity?.trim() || "",
    jobTypes: Array.isArray(profileDraft?.jobTypes) ? profileDraft.jobTypes : [],
    openToRemote: profileDraft?.openToRemote !== false,
    requireVisaSupport: Boolean(profileDraft?.requireVisaSupport)
  };
}

function hasCompletedProfileOnboarding(profileDraft) {
  return Boolean(
    profileDraft?.targetRole?.trim()
    && Array.isArray(profileDraft?.jobTypes)
    && profileDraft.jobTypes.length > 0
    && ((profileDraft?.expectedCity && profileDraft.expectedCity.trim()) || profileDraft?.openToRemote)
  );
}

function formatResumeRelativeTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }
  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.max(Math.floor(diffMs / 60000), 0);
  if (diffMinutes < 1) {
    return "刚刚";
  }
  if (diffMinutes < 60) {
    return `${diffMinutes} 分钟前`;
  }
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours} 小时前`;
  }
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 30) {
    return `${diffDays} 天前`;
  }
  return date.toLocaleDateString("zh-CN");
}

function formatResumeAbsoluteTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }
  return date.toLocaleDateString("zh-CN");
}

function buildResumePreviewContent(resumeInfo, profileDraft, authUser) {
  const displayName = getAuthDisplayName(authUser);
  const targetRole = profileDraft?.targetRole?.trim() || "后端开发";
  const city = profileDraft?.expectedCity?.trim() || "上海";
  const keywordTags = (profileDraft?.keywordTags || "")
    .split(/[，,]/)
    .map((item) => item.trim())
    .filter(Boolean);
  const summary = profileDraft?.personalSummary?.trim()
    || "具备扎实的后端开发基础，关注 Java、Spring Boot、MySQL 等技术方向，希望参与平台型和业务核心场景建设。";
  const skillGroups = [
    {
      title: "语言与基础",
      items: keywordTags.length ? keywordTags.slice(0, 4) : ["Java", "SQL", "Linux", "数据结构"]
    },
    {
      title: "后端框架",
      items: ["Spring Boot", "MyBatis", "Redis", "RESTful API"]
    },
    {
      title: "工具与工程化",
      items: ["IntelliJ IDEA", "Postman", "Git", "Maven"]
    }
  ];

  return {
    resumeId: resumeInfo?.resumeId || "",
    fileName: resumeInfo?.fileName || "当前简历",
    updatedAt: resumeInfo?.uploadTime || "",
    previewUrl: resumeInfo?.resumeId ? `/api/user/resume/${resumeInfo.resumeId}/file` : "",
    downloadUrl: resumeInfo?.resumeId ? `/api/user/resume/${resumeInfo.resumeId}/file` : "",
    contentType: resumeInfo?.fileName?.toLowerCase().endsWith(".pdf") ? "application/pdf" : "application/octet-stream",
    score: {
      grade: "A",
      label: "EXCELLENT",
      scoreValue: resumeInfo?.score || 88,
      urgentFixCount: 2,
      criticalFixCount: 1,
      optionalFixCount: 3,
      summary: "技术方向明确，关键词集中在后端开发，但还可以继续强化项目成果和量化表达。"
    },
    profile: {
      name: displayName,
      title: targetRole,
      location: city,
      status: resumeInfo?.status || "ACTIVE"
    },
    analysisSummary: summary,
    analysisHighlights: [
      {
        title: "Impact & Achievements",
        description: "已经有较清晰的后端技术栈表达，下一步重点是让项目经历更量化。"
      },
      {
        title: "Role Alignment",
        description: "关键词和目标岗位一致，适合继续往 Java 后端 / 平台研发方向优化。"
      }
    ],
    urgentIssues: [
      {
        title: "项目成果还不够量化",
        description: "建议把项目中的吞吐量、性能优化比例、接口规模或业务结果写出来。"
      },
      {
        title: "个人摘要可再聚焦",
        description: "可以明确写出你最想投递的岗位方向和最有代表性的技术能力。"
      }
    ],
    skillGroups,
    projects: [
      {
        name: "高并发电商系统",
        technologies: ["Java", "Spring Boot", "MySQL", "Redis"],
        bullets: [
          "设计并实现核心订单与库存接口，支持高并发请求处理。",
          "使用 Redis 做热点数据缓存，优化接口响应时间。",
          "补充更明确的性能和业务指标后，会更适合 LLM 做打分和建议。"
        ]
      }
    ],
    workExperiences: [
      {
        company: "某互联网平台",
        role: "后端开发实习生",
        bullets: [
          "参与订单、库存、商品等核心服务的接口开发与联调。",
          "协助老系统向微服务架构迁移，提升模块可维护性。",
          "编写单元测试并参与代码评审，保障接口稳定性。"
        ]
      }
    ],
    certifications: [
      {
        name: "数据库与中间件能力",
        description: "熟悉 MySQL、Redis、RabbitMQ 等常见后端基础组件。"
      },
      {
        name: "工程化与工具链",
        description: "掌握 Git、Maven、Postman、Linux 基础脚本与排障。"
      }
    ]
  };
}

function getResumeContactCards(previewData, profileDraft, authUser) {
  const name = getAuthDisplayName(authUser);
  return [
    {
      key: "email",
      icon: "mail",
      title: "邮箱",
      value: authUser?.email || "待补充邮箱",
      hint: "建议补充常用邮箱，方便投递与自动填充。"
    },
    {
      key: "phone",
      icon: "phone",
      title: "电话",
      value: "待补充电话",
      hint: "建议补充手机号，用于后续简历完善。"
    },
    {
      key: "location",
      icon: "location",
      title: "地点",
      value: previewData?.profile?.location || getProfileLocationText(profileDraft),
      hint: "可填写城市 / 国家，提升岗位地域匹配。"
    },
    {
      key: "linkedin",
      icon: "link",
      title: "LinkedIn",
      value: `${name} 的职业主页`,
      hint: "可补充公开职业链接，增强招聘方了解。"
    },
    {
      key: "github",
      icon: "github",
      title: "GitHub",
      value: `${name} 的项目仓库`,
      hint: "可展示代表性项目与工程能力。"
    },
    {
      key: "other",
      icon: "globe",
      title: "其他链接",
      value: "个人站点 / 作品集 / 博客",
      hint: "可补充你的技术博客或作品展示页。"
    }
  ];
}

function SidebarIcon({ type }) {
  switch (type) {
    case "briefcase":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M8 7V6a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v1" />
          <path d="M4 9.5c0-1.1.9-2 2-2h12c1.1 0 2 .9 2 2v7.5c0 1.1-.9 2-2 2H6c-1.1 0-2-.9-2-2V9.5Z" />
          <path d="M4 11.5c2.8 1.5 5.4 2.2 8 2.2s5.2-.7 8-2.2" />
        </svg>
      );
    case "document":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M8 4.5h5.5L18 9v10.5a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2v-13a2 2 0 0 1 2-2Z" />
          <path d="M13 4.5V9h5" />
          <path d="M9 13h6" />
          <path d="M9 16h6" />
        </svg>
      );
    case "user":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
          <path d="M5 19a7 7 0 0 1 14 0" />
        </svg>
      );
    case "spark":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 3l1.6 4.4L18 9l-4.4 1.6L12 15l-1.6-4.4L6 9l4.4-1.6L12 3Z" />
          <path d="M18.5 15.5l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8.8-2.2Z" />
        </svg>
      );
    case "target":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 20a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z" />
          <path d="M12 16a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
          <path d="M12 12 19 5" />
          <path d="M16 5h3v3" />
        </svg>
      );
    default:
      return null;
  }
}

function buildUserDashboardView(dashboard, profileDraft, resumeInfo, authUser) {
  const base = {
    ...userDashboardFallback,
    ...(dashboard || {})
  };
  const nextTips = [];
  if (!resumeInfo) {
    nextTips.push("上传最新简历");
  }
  if (!profileDraft.expectedCity) {
    nextTips.push("设置期望城市");
  }
  if (!profileDraft.keywordTags) {
    nextTips.push("补充岗位关键词");
  }
  if (!profileDraft.personalSummary) {
    nextTips.push("补充个人亮点摘要");
  }

  const extraCompletion =
    [profileDraft.expectedCity, profileDraft.targetRole, profileDraft.keywordTags, profileDraft.personalSummary]
      .filter((value) => value && String(value).trim()).length * 12;

  return {
    ...base,
    displayName: base.displayName || getAuthDisplayName(authUser),
    profileCompletionRate: Math.min(
      100,
      Math.max(base.profileCompletionRate || 0, (base.profileCompletionRate || 0) + extraCompletion)
    ),
    tips: nextTips.length ? nextTips : ["保持当前资料新鲜度，持续更新简历和偏好设置"]
  };
}

function CompanyBadge({ job }) {
  const [logoFailed, setLogoFailed] = useState(false);
  const badgeClassName = job.theme ? `company-badge ${job.theme}` : "company-badge";
  const showLogo = Boolean(job.companyLogo) && !logoFailed;

  return (
    <div className={badgeClassName}>
      {showLogo ? (
        <img
          className="company-badge-logo"
          src={job.companyLogo}
          alt={`${job.companyName} logo`}
          onError={() => setLogoFailed(true)}
        />
      ) : (
        <span>{getCompanyBadgeText(job)}</span>
      )}
    </div>
  );
}

function JobDetailIcon({ type }) {
  switch (type) {
    case "location":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 21s6-4.8 6-10a6 6 0 1 0-12 0c0 5.2 6 10 6 10Z" />
          <path d="M12 13.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" />
        </svg>
      );
    case "calendar":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M7 4.5V7" />
          <path d="M17 4.5V7" />
          <path d="M5 9h14" />
          <path d="M6 6.5h12a1.5 1.5 0 0 1 1.5 1.5v9.5A1.5 1.5 0 0 1 18 19H6A1.5 1.5 0 0 1 4.5 17.5V8A1.5 1.5 0 0 1 6 6.5Z" />
        </svg>
      );
    case "desk":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M4.5 10.5 12 5l7.5 5.5v8a1.5 1.5 0 0 1-1.5 1.5h-12A1.5 1.5 0 0 1 4.5 18.5v-8Z" />
          <path d="M9 19v-4.5a1.5 1.5 0 0 1 1.5-1.5h3a1.5 1.5 0 0 1 1.5 1.5V19" />
        </svg>
      );
    case "badge":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M7.5 5.5h9l1.5 4.5L12 18l-6-8 1.5-4.5Z" />
          <path d="M9.5 10.5h5" />
        </svg>
      );
    case "salary":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 3.5v17" />
          <path d="M16 7.5c0-1.7-1.8-3-4-3s-4 1.3-4 3 1.8 3 4 3 4 1.3 4 3-1.8 3-4 3-4-1.3-4-3" />
        </svg>
      );
    case "document":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M8 4.5h6l4 4v10a1.5 1.5 0 0 1-1.5 1.5h-8A1.5 1.5 0 0 1 7 18.5V6A1.5 1.5 0 0 1 8.5 4.5Z" />
          <path d="M14 4.5V9h4" />
          <path d="M9.5 12h5" />
          <path d="M9.5 15h5" />
        </svg>
      );
    default:
      return null;
  }
}

function JobActionIcon({ type }) {
  switch (type) {
    case "skip":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="9" />
          <path d="m8.5 8.5 7 7" />
        </svg>
      );
    case "favorite":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="m12 19-1.3-1.18C6.1 13.7 3.5 11.33 3.5 8.4A4.1 4.1 0 0 1 7.7 4.2c1.64 0 3.21.76 4.3 1.95 1.09-1.19 2.66-1.95 4.3-1.95a4.1 4.1 0 0 1 4.2 4.2c0 2.93-2.6 5.3-7.2 9.43L12 19Z" />
        </svg>
      );
    case "spark":
      return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 3.5 13.7 8 18.2 9.7 13.7 11.4 12 15.9 10.3 11.4 5.8 9.7 10.3 8 12 3.5Z" />
          <path d="m18.2 14.8.7 1.9 1.9.7-1.9.7-.7 1.9-.7-1.9-1.9-.7 1.9-.7.7-1.9Z" />
        </svg>
      );
    default:
      return null;
  }
}

function FilterSelect({ value, options, onChange, placeholder }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (!containerRef.current?.contains(event.target)) {
        setOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const selectedOption = options.find((option) => option.value === value);
  const displayLabel = selectedOption?.label || placeholder;

  return (
    <div className={open ? "custom-select open" : "custom-select"} ref={containerRef}>
      <button
        className="custom-select-trigger"
        onClick={() => setOpen((current) => !current)}
        type="button"
      >
        <span>{displayLabel}</span>
        <span className={open ? "custom-select-caret open" : "custom-select-caret"}>
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="m5 7.5 5 5 5-5" />
          </svg>
        </span>
      </button>

      {open ? (
        <div className="custom-select-menu">
          {options.map((option) => (
            <button
              key={String(option.value)}
              className={option.value === value ? "custom-select-option active" : "custom-select-option"}
              onClick={() => {
                onChange(option.value);
                setOpen(false);
              }}
              type="button"
            >
              <span>{option.label}</span>
              {option.value === value ? <strong>✓</strong> : null}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}

class RequestError extends Error {
  constructor(message, options = {}) {
    super(message);
    this.name = "RequestError";
    this.code = options.code ?? "";
    this.status = options.status ?? 0;
    this.payload = options.payload;
  }
}

async function readResponsePayload(response) {
  const contentType = response.headers.get("content-type") || "";
  const rawText = await response.text();
  if (!rawText) {
    return null;
  }
  if (contentType.includes("application/json")) {
    try {
      return JSON.parse(rawText);
    } catch {
      return { message: rawText };
    }
  }
  try {
    return JSON.parse(rawText);
  } catch {
    return { message: rawText };
  }
}

function getErrorMessage(payload, fallbackMessage) {
  if (!payload) {
    return fallbackMessage;
  }
  if (typeof payload === "string" && payload.trim()) {
    return payload;
  }
  if (typeof payload.message === "string" && payload.message.trim()) {
    return payload.message;
  }
  if (typeof payload.error === "string" && payload.error.trim()) {
    return payload.error;
  }
  if (payload.error && typeof payload.error.message === "string" && payload.error.message.trim()) {
    return payload.error.message;
  }
  return fallbackMessage;
}

function App() {
  const [activeTab, setActiveTab] = useState("推荐职位");
  const [activeSection, setActiveSection] = useState("jobs");
  const [activeProfileTab, setActiveProfileTab] = useState("basic");
  const [profileEditorType, setProfileEditorType] = useState("");
  const [profilePersonalEditorDraft, setProfilePersonalEditorDraft] = useState(null);
  const [profileWorkEditorDraft, setProfileWorkEditorDraft] = useState(null);
  const [profileSkillsEditorDraft, setProfileSkillsEditorDraft] = useState(null);
  const [profileEducationEditorDraft, setProfileEducationEditorDraft] = useState(null);
  const [profilePreferencesEditorDraft, setProfilePreferencesEditorDraft] = useState(null);
  const [filterExpanded, setFilterExpanded] = useState(false);
  const [authView, setAuthView] = useState("login");
  const [loginForm, setLoginForm] = useState(loginInitialState);
  const [registerForm, setRegisterForm] = useState(registerInitialState);
  const [loginCaptcha, setLoginCaptcha] = useState(null);
  const [loginCaptchaLoading, setLoginCaptchaLoading] = useState(false);
  const [sendCodeLoading, setSendCodeLoading] = useState(false);
  const [sendCodeCooldown, setSendCodeCooldown] = useState(0);
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeInfo, setResumeInfo] = useState(null);
  const [resumePreviewOpen, setResumePreviewOpen] = useState(false);
  const [resumePreviewLoading, setResumePreviewLoading] = useState(false);
  const [resumePreviewData, setResumePreviewData] = useState(null);
  const [userDashboard, setUserDashboard] = useState(userDashboardFallback);
  const [userProfileDraft, setUserProfileDraft] = useState(userProfileDraftInitialState);
  const [settingsDraft, setSettingsDraft] = useState(settingsDraftInitialState);
  const [jobFilters, setJobFilters] = useState(jobFilterInitialState);
  const [recommendedJobsData, setRecommendedJobsData] = useState(
    createJobListState([], mockRecommendedJobs.length, mockRecommendedJobs.length > 0)
  );
  const [favoriteJobsData, setFavoriteJobsData] = useState(createJobListState());
  const [appliedJobsData, setAppliedJobsData] = useState(createJobListState());
  const [jobLoading, setJobLoading] = useState(false);
  const [selectedJobId, setSelectedJobId] = useState(null);
  const [pendingApplyFollowUpJobId, setPendingApplyFollowUpJobId] = useState(null);
  const [applyFollowUpLeftPage, setApplyFollowUpLeftPage] = useState(false);
  const [showApplyFollowUpModal, setShowApplyFollowUpModal] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(false);
  const [sessionReady, setSessionReady] = useState(false);
  const [jobActionOverrides, setJobActionOverrides] = useState({});
  const mainPanelRef = useRef(null);
  const loadMoreAnchorRef = useRef(null);
  const resumeUploadInputRef = useRef(null);
  const onboardingResumeInputRef = useRef(null);
  const loadMoreLockRef = useRef(false);
  const profileBasicRef = useRef(null);
  const profileEducationRef = useRef(null);
  const profileWorkRef = useRef(null);
  const profileSkillsRef = useRef(null);
  const profilePreferencesRef = useRef(null);
  const [auth, setAuth] = useState(() => ({
    token: localStorage.getItem(TOKEN_KEY),
    user: null
  }));

  useEffect(() => {
    if (sendCodeCooldown <= 0) {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      setSendCodeCooldown((current) => Math.max(0, current - 1));
    }, 1000);
    return () => window.clearTimeout(timer);
  }, [sendCodeCooldown]);

  useEffect(() => {
    if (auth.token || authView !== "login") {
      return;
    }
    if (loginCaptcha?.captchaKey) {
      return;
    }
    refreshLoginCaptcha();
  }, [auth.token, authView, loginCaptcha?.captchaKey]);
  const currentJobsData =
    activeTab === "收藏职位"
      ? favoriteJobsData
      : activeTab === "已投递"
        ? appliedJobsData
        : recommendedJobsData;
  const hasMoreJobs = activeTab === "推荐职位" && Boolean(recommendedJobsData.hasMore);
  const dashboardView = buildUserDashboardView(
    userDashboard,
    userProfileDraft,
    resumeInfo,
    auth.user
  );
  const isUserHomeSection = activeSection !== "jobs";
  const mergedJobRecords = currentJobsData.records.map((job) => {
    const override = jobActionOverrides[job.jobId] || {};
    return {
      ...job,
      liked: override.liked ?? job.liked,
      applied: override.applied ?? job.applied
    };
  });
  const visibleJobRecords = mergedJobRecords;
  const selectedJob = findJobById(
    selectedJobId,
    [recommendedJobsData, favoriteJobsData, appliedJobsData],
    jobActionOverrides
  );
  const pendingApplyFollowUpJob = findJobById(
    pendingApplyFollowUpJobId,
    [recommendedJobsData, favoriteJobsData, appliedJobsData],
    jobActionOverrides
  );
  const visibleJobTotal = currentJobsData.total;
  const favoriteCount = favoriteJobsData.total;
  const appliedCount = appliedJobsData.total;
  const profileSectionRefs = {
    basic: profileBasicRef,
    education: profileEducationRef,
    work: profileWorkRef,
    skills: profileSkillsRef,
    preferences: profilePreferencesRef
  };
  const profilePersonalView = profilePersonalEditorDraft || buildProfilePersonalEditorDraft(auth.user, userProfileDraft);
  const profileWorkView = profileWorkEditorDraft || buildProfileWorkEditorDraft(userProfileDraft);
  const profileSkillsView = profileSkillsEditorDraft || buildProfileSkillsEditorDraft(userProfileDraft);
  const profileEducationView = profileEducationEditorDraft || buildProfileEducationEditorDraft();
  const profilePreferencesView = profilePreferencesEditorDraft || buildProfilePreferencesEditorDraft(userProfileDraft);

  useEffect(() => {
    if (auth.token) {
      void loadCurrentUser();
      return;
    }
    setSessionReady(true);
  }, []);

  useEffect(() => {
    if (!selectedJobId) {
      return undefined;
    }
    function handleKeyDown(event) {
      if (event.key === "Escape") {
        setSelectedJobId(null);
      }
    }
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [selectedJobId]);

  useEffect(() => {
    if (!message.text || message.type === "error") {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      setMessage((current) => (current.text === message.text ? { type: "", text: "" } : current));
    }, 2600);
    return () => window.clearTimeout(timer);
  }, [message]);

  useEffect(() => {
    if (!pendingApplyFollowUpJobId || showApplyFollowUpModal) {
      return undefined;
    }

    const markLeftPage = () => {
      if (document.visibilityState === "hidden") {
        setApplyFollowUpLeftPage(true);
      }
    };

    const maybeOpenModal = () => {
      if (document.visibilityState === "visible" && applyFollowUpLeftPage) {
        setShowApplyFollowUpModal(true);
      }
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === "hidden") {
        setApplyFollowUpLeftPage(true);
        return;
      }
      if (document.visibilityState === "visible" && applyFollowUpLeftPage) {
        setShowApplyFollowUpModal(true);
      }
    };

    window.addEventListener("blur", markLeftPage);
    window.addEventListener("focus", maybeOpenModal);
    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      window.removeEventListener("blur", markLeftPage);
      window.removeEventListener("focus", maybeOpenModal);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [pendingApplyFollowUpJobId, applyFollowUpLeftPage, showApplyFollowUpModal]);

  useEffect(() => {
    const panel = mainPanelRef.current;
    const anchor = loadMoreAnchorRef.current;
    if (
      !auth.token
      || !panel
      || !anchor
      || jobLoading
      || !hasMoreJobs
      || activeTab !== "推荐职位"
      || isUserHomeSection
    ) {
      return undefined;
    }

    const tryLoadMore = () => {
      if (loadMoreLockRef.current) {
        return;
      }
      const distanceToBottom = panel.scrollHeight - panel.scrollTop - panel.clientHeight;
      if (distanceToBottom > 240) {
        return;
      }
      if (!settingsDraft.autoLoadMoreJobs) {
        return;
      }
      handleLoadMoreRecommended();
    };

    tryLoadMore();
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          tryLoadMore();
        }
      },
      {
        root: panel,
        rootMargin: "0px 0px 240px 0px",
        threshold: 0.01
      }
    );
    observer.observe(anchor);
    panel.addEventListener("scroll", tryLoadMore, { passive: true });
    return () => {
      observer.disconnect();
      panel.removeEventListener("scroll", tryLoadMore);
    };
  }, [auth.token, jobLoading, hasMoreJobs, activeTab, isUserHomeSection, settingsDraft.autoLoadMoreJobs, visibleJobRecords.length]);

  useEffect(() => {
    if (activeSection !== "profile") {
      return undefined;
    }

    const panel = mainPanelRef.current;
    if (!panel) {
      return undefined;
    }

    const updateActiveProfileTab = () => {
      const panelTop = panel.getBoundingClientRect().top;
      let nextTab = profileTabItems[0].key;
      let nearestNegative = Number.NEGATIVE_INFINITY;
      let nearestPositive = Number.POSITIVE_INFINITY;

      profileTabItems.forEach((item) => {
        const element = profileSectionRefs[item.key]?.current;
        if (!element) {
          return;
        }
        const offsetTop = element.getBoundingClientRect().top - panelTop - 92;
        if (offsetTop <= 0 && offsetTop > nearestNegative) {
          nearestNegative = offsetTop;
          nextTab = item.key;
          return;
        }
        if (nearestNegative === Number.NEGATIVE_INFINITY && offsetTop < nearestPositive) {
          nearestPositive = offsetTop;
          nextTab = item.key;
        }
      });

      setActiveProfileTab((current) => (current === nextTab ? current : nextTab));
    };

    updateActiveProfileTab();
    panel.addEventListener("scroll", updateActiveProfileTab, { passive: true });
    window.addEventListener("resize", updateActiveProfileTab);

    return () => {
      panel.removeEventListener("scroll", updateActiveProfileTab);
      window.removeEventListener("resize", updateActiveProfileTab);
    };
  }, [activeSection]);

  useEffect(() => {
    if (!auth.user || !sessionReady) {
      return;
    }

    setProfilePersonalEditorDraft((current) => current || readLocalJson(
      getUserScopedStorageKey(auth.user, "profile_personal_editor"),
      buildProfilePersonalEditorDraft(auth.user, userProfileDraft)
    ));
    setProfileWorkEditorDraft((current) => current || readLocalJson(
      getUserScopedStorageKey(auth.user, "profile_work_editor"),
      buildProfileWorkEditorDraft(userProfileDraft)
    ));
    setProfileSkillsEditorDraft((current) => current || readLocalJson(
      getUserScopedStorageKey(auth.user, "profile_skills_editor"),
      buildProfileSkillsEditorDraft(userProfileDraft)
    ));
    setProfileEducationEditorDraft((current) => current || readLocalJson(
      getUserScopedStorageKey(auth.user, "profile_education_editor"),
      buildProfileEducationEditorDraft()
    ));
    setProfilePreferencesEditorDraft((current) => current || readLocalJson(
      getUserScopedStorageKey(auth.user, "profile_preferences_editor"),
      buildProfilePreferencesEditorDraft(userProfileDraft)
    ));
  }, [auth.user, sessionReady]);

  function handleLoadMoreRecommended() {
    if (loadMoreLockRef.current || jobLoading || !hasMoreJobs || activeTab !== "推荐职位" || isUserHomeSection) {
      return;
    }
    loadMoreLockRef.current = true;
    setJobFilters((current) => {
      const nextFilters = {
        ...current,
        current: Number(current.current || 1) + 1
      };
      void loadRecommendedJobs(nextFilters, true);
      return nextFilters;
    });
  }

  async function request(path, options = {}) {
    const token = localStorage.getItem(TOKEN_KEY);
    let response;
    try {
      response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
          ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
          ...(token ? { [TOKEN_HEADER]: token } : {}),
          ...(options.headers || {})
        }
      });
    } catch (error) {
      throw new RequestError("网络连接失败，请检查前后端服务是否已启动", { payload: error });
    }

    const result = await readResponsePayload(response);
    if (!response.ok) {
      const message = getErrorMessage(
        result,
        response.status === 401 || response.status === 403
          ? "登录状态已失效，请重新登录"
          : `请求失败（${response.status}）`
      );
      throw new RequestError(message, {
        code: result?.code,
        status: response.status,
        payload: result
      });
    }
    if (!result || result.code !== "0") {
      throw new RequestError(getErrorMessage(result, "请求失败"), {
        code: result?.code,
        status: response.status,
        payload: result
      });
    }
    return result.data;
  }

  async function loadCurrentUser() {
    setSessionReady(false);
    try {
      const user = await request("/api/auth/me", { method: "GET" });
      setAuth((current) => ({ ...current, user }));
      const localDraft = loadLocalUserProfileDraft(user);
      loadLocalSettingsDraft(user);
      loadLocalJobActions(user);
      await loadRemoteProfilePreferences(user, localDraft);
      await loadUserDashboard(user);
      await loadCurrentResume();
      await Promise.all([
        loadRecommendedJobs(jobFilterInitialState),
        loadFavoriteJobs(jobFilterInitialState),
        loadAppliedJobs(jobFilterInitialState)
      ]);
    } catch (error) {
      logout(false);
      setMessage({ type: "error", text: error.message || "获取当前用户失败" });
    } finally {
      setSessionReady(true);
    }
  }

  async function loadCurrentResume() {
    try {
      const resume = await request("/api/user/resume/current", { method: "GET" });
      setResumeInfo(resume);
    } catch (error) {
      setResumeInfo(null);
    }
  }

  async function loadUserDashboard(user = auth.user) {
    try {
      const dashboard = await request("/api/user/dashboard", { method: "GET" });
      const localPlanName = readLocalPlanName(user);
      setUserDashboard({
        ...userDashboardFallback,
        ...dashboard,
        ...(localPlanName ? { planName: localPlanName } : {})
      });
    } catch {
      const localPlanName = readLocalPlanName(user);
      setUserDashboard({
        ...userDashboardFallback,
        displayName: getAuthDisplayName(user),
        ...(localPlanName ? { planName: localPlanName } : {})
      });
    }
  }

  function readLocalPlanName(user = auth.user) {
    return localStorage.getItem(getUserScopedStorageKey(user, "plan_name")) || "";
  }

  function loadLocalUserProfileDraft(user = auth.user) {
    const storedDraft = readLocalJson(
      getUserScopedStorageKey(user, "profile_draft"),
      userProfileDraftInitialState
    );
    const nextDraft = {
      ...userProfileDraftInitialState,
      ...storedDraft
    };
    setUserProfileDraft(nextDraft);
    return nextDraft;
  }

  async function loadRemoteProfilePreferences(user = auth.user, localDraft = userProfileDraftInitialState) {
    try {
      const remoteDraft = await request("/api/user/profile/preferences", { method: "GET" });
      const nextDraft = {
        ...userProfileDraftInitialState,
        ...localDraft,
        ...remoteDraft,
        jobTypes: Array.isArray(remoteDraft?.jobTypes) ? remoteDraft.jobTypes : localDraft.jobTypes || []
      };
      setUserProfileDraft(nextDraft);
      writeLocalJson(getUserScopedStorageKey(user, "profile_draft"), nextDraft);
      return nextDraft;
    } catch {
      setUserProfileDraft({
        ...userProfileDraftInitialState,
        ...localDraft
      });
      return localDraft;
    }
  }

  function loadLocalSettingsDraft(user = auth.user) {
    const storedDraft = readLocalJson(
      getUserScopedStorageKey(user, "settings_draft"),
      settingsDraftInitialState
    );
    setSettingsDraft({
      ...settingsDraftInitialState,
      ...storedDraft
    });
  }

  function loadLocalJobActions(user = auth.user) {
    setJobActionOverrides(
      readLocalJson(getUserScopedStorageKey(user, "job_actions"), {})
    );
  }

  function buildRecommendQueryString(params) {
    const searchParams = new URLSearchParams();
    recommendRequestFieldNames.forEach((key) => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== "") {
        searchParams.set(key, String(value));
      }
    });
    return searchParams.toString();
  }

  function filterMockJobs(filters) {
    const normalizedKeyword = filters.keyword.trim().toLowerCase();
    const normalizedTitle = filters.title.trim().toLowerCase();
    const filtered = mockRecommendedJobs.filter((job) => {
      const keywordMatched =
        !normalizedKeyword ||
        [job.title, job.companyName, job.meta, job.location, job.jobSummary, ...(job.skillTags || [])].some((text) =>
          String(text).toLowerCase().includes(normalizedKeyword)
        );
      const countryMatched = !filters.country || filters.country === "中国大陆";
      const titleMatched =
        !normalizedTitle || job.title.toLowerCase().includes(normalizedTitle);
      const cityMatched = !filters.city || job.city === filters.city || job.location.includes(filters.city);
      const experienceMatched =
        !filters.experienceLevel || job.experienceLevel === filters.experienceLevel;
      const employmentTypeMatched =
        !filters.employmentType || job.employmentType === filters.employmentType;
      const workModeMatched = !filters.workMode || job.workMode === filters.workMode;
      const industryNameMatched =
        !filters.industryName || job.meta.includes(filters.industryName);
      const educationRequirementMatched =
        !filters.educationRequirement || job.educationRequirement === filters.educationRequirement;
      return (
        keywordMatched &&
        countryMatched &&
        titleMatched &&
        cityMatched &&
        experienceMatched &&
        employmentTypeMatched &&
        workModeMatched &&
        industryNameMatched &&
        educationRequirementMatched
      );
    });

    const current = Number(filters.current) || 1;
    const size = Number(filters.size) || jobFilterInitialState.size;
    const fromIndex = Math.max((current - 1) * size, 0);
    const toIndex = fromIndex + size;
    return {
      total: filtered.length,
      hasMore: toIndex < filtered.length,
      records: filtered.slice(fromIndex, toIndex)
    };
  }

  function setJobListData(tab, updater) {
    if (tab === "收藏职位") {
      setFavoriteJobsData(updater);
      return;
    }
    if (tab === "已投递") {
      setAppliedJobsData(updater);
      return;
    }
    setRecommendedJobsData(updater);
  }

  function getFallbackJobData(tab, filters) {
    const normalizedFilters = tab === "推荐职位" ? filters : { ...filters, current: 1 };
    if (tab === "推荐职位") {
      return filterMockJobs(normalizedFilters);
    }
    const base = filterMockJobs(normalizedFilters);
    if (tab === "收藏职位") {
      const records = base.records.filter((job) => normalizeBoolean(job.liked));
      return { total: records.length, hasMore: false, records };
    }
    const records = base.records.filter((job) => normalizeBoolean(job.applied));
    return { total: records.length, hasMore: false, records };
  }

  async function loadJobList(tab, nextFilters = jobFilters, append = false, silent = false) {
    if (!silent) {
      setJobLoading(true);
    }
    try {
      const normalizedFilters = tab === "推荐职位" ? nextFilters : { ...nextFilters, current: 1 };
      const queryString = buildRecommendQueryString(normalizedFilters);
      const endpoint = jobTabEndpointMap[tab] || jobTabEndpointMap["推荐职位"];
      const data = await request(`${endpoint}?${queryString}`, { method: "GET" });
      const nextRecords = Array.isArray(data?.records) ? data.records : [];
      setJobListData(tab, (current) => ({
        total: data?.total ?? 0,
        hasMore: Boolean(data?.hasMore),
        records: append ? [...current.records, ...nextRecords] : nextRecords
      }));
    } catch (error) {
      const fallback = getFallbackJobData(tab, nextFilters);
      setJobListData(tab, (current) => ({
        total: fallback.total,
        hasMore: fallback.hasMore,
        records: append ? [...current.records, ...fallback.records] : fallback.records
      }));
      if (!silent) {
        setMessage({
          type: "error",
          text: `${error.message || `${tab}加载失败`}，当前已切换到本地演示数据`
        });
      }
    } finally {
      loadMoreLockRef.current = false;
      if (!silent) {
        setJobLoading(false);
      }
    }
  }

  async function loadRecommendedJobs(nextFilters = jobFilters, append = false, silent = false) {
    await loadJobList("推荐职位", nextFilters, append, silent);
  }

  async function loadFavoriteJobs(nextFilters = jobFilters, append = false, silent = false) {
    await loadJobList("收藏职位", nextFilters, append, silent);
  }

  async function loadAppliedJobs(nextFilters = jobFilters, append = false, silent = false) {
    await loadJobList("已投递", nextFilters, append, silent);
  }

  async function loadJobsByActiveTab(nextFilters = jobFilters, append = false) {
    await loadJobList(activeTab, nextFilters, append, false);
  }

  function hasActiveJobFilters(filters = jobFilters) {
    return [
      filters.keyword,
      filters.country,
      filters.title,
      filters.city,
      filters.experienceLevel,
      filters.employmentType,
      filters.workMode,
      filters.datePosted,
      filters.industryName,
      filters.educationRequirement
    ].some((value) => value !== null && value !== undefined && String(value).trim() !== "");
  }

  function getEmptyStateCopy() {
    if (activeTab === "收藏职位") {
      return {
        title: "你还没有收藏职位",
        description: "看到感兴趣的岗位先点收藏，这里会自动帮你集中起来。"
      };
    }
    if (activeTab === "已投递") {
      return {
        title: "你还没有已投递职位",
        description: "给岗位标记为已投递后，这里会帮你集中管理后续跟进。"
      };
    }
    if (hasActiveJobFilters()) {
      return {
        title: "暂时没有符合筛选条件的职位",
        description: "你可以放宽城市、学历或经验条件，系统会重新帮你匹配。"
      };
    }
    return {
      title: "当前还没有可展示的推荐职位",
      description: "先上传或更新简历、补充求职意向，系统会更快生成适合你的职位推荐。"
    };
  }

  function getFilterSummaryItems() {
    const summary = [];
    if (jobFilters.city) {
      summary.push({ key: "city", label: `城市 ${jobFilters.city}` });
    }
    if (jobFilters.experienceLevel) {
      summary.push({ key: "experienceLevel", label: `经验 ${getExperienceLevelLabel(jobFilters.experienceLevel)}` });
    }
    if (jobFilters.employmentType) {
      summary.push({ key: "employmentType", label: jobFilters.employmentType });
    }
    if (jobFilters.industryName) {
      summary.push({ key: "industryName", label: jobFilters.industryName });
    }
    if (jobFilters.educationRequirement) {
      summary.push({ key: "educationRequirement", label: jobFilters.educationRequirement });
    }
    if (jobFilters.datePosted) {
      summary.push({
        key: "datePosted",
        label: jobFilters.datePosted === "24h"
          ? "24 小时内"
          : jobFilters.datePosted === "3d"
            ? "3 天内"
            : "7 天内"
      });
    }
    return summary;
  }

  function clearJobFilter(field) {
    updateJobFilter(field, "");
  }

  function getTabBadgeCount(tab) {
    if (tab === "收藏职位") {
      return favoriteCount;
    }
    if (tab === "已投递") {
      return appliedCount;
    }
    return 0;
  }

  async function handleLogin(event) {
    event.preventDefault();
    setLoading(true);
    setMessage({ type: "", text: "" });
    try {
      const data = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(loginForm)
      });
      localStorage.setItem(TOKEN_KEY, data.accessToken);
      setAuth({ token: data.accessToken, user: null });
      await loadCurrentUser();
      setMessage({ type: "success", text: "登录成功，正在进入首页。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
      await refreshLoginCaptcha(true);
    } finally {
      setLoading(false);
    }
  }

  async function refreshLoginCaptcha(silent = false) {
    if (!silent) {
      setLoginCaptchaLoading(true);
    }
    try {
      const data = await request("/api/auth/captcha", { method: "GET" });
      setLoginCaptcha(data);
      setLoginForm((current) => ({
        ...current,
        captchaKey: data?.captchaKey || "",
        captchaCode: ""
      }));
    } catch (error) {
      if (!silent) {
        setMessage({ type: "error", text: error.message || "获取图形验证码失败" });
      }
    } finally {
      setLoginCaptchaLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setLoading(true);
    setMessage({ type: "", text: "" });
    try {
      const { verificationCode: _verificationCode, ...registerPayload } = registerForm;
      await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(registerPayload)
      });
      setRegisterForm(registerInitialState);
      setSendCodeCooldown(0);
      setAuthView("login");
      setMessage({ type: "success", text: "注册成功，请使用新账号登录。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function handleSendVerificationCode() {
    const email = String(registerForm.email || "").trim();
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      setMessage({ type: "error", text: "请先填写邮箱后再发送验证码。" });
      return;
    }
    if (!emailPattern.test(email)) {
      setMessage({ type: "error", text: "邮箱格式不正确，请检查后再试。" });
      return;
    }
    if (sendCodeCooldown > 0) {
      return;
    }

    setSendCodeLoading(true);
    setMessage({ type: "", text: "" });
    try {
      await request("/api/auth/send", {
        method: "POST",
        body: JSON.stringify({ email })
      });
      setSendCodeCooldown(60);
      setMessage({ type: "success", text: "验证码已发送，请留意邮箱。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "验证码发送失败" });
    } finally {
      setSendCodeLoading(false);
    }
  }

  function logout(clearMessage = true) {
    localStorage.removeItem(TOKEN_KEY);
    setAuth({ token: null, user: null });
    setActiveSection("jobs");
    setActiveTab("推荐职位");
    setResumeInfo(null);
    setSelectedJobId(null);
    setUserDashboard(userDashboardFallback);
    setUserProfileDraft(userProfileDraftInitialState);
    setJobActionOverrides({});
    setJobFilters(jobFilterInitialState);
    setRecommendedJobsData(createJobListState([], mockRecommendedJobs.length, mockRecommendedJobs.length > 0));
    setFavoriteJobsData(createJobListState());
    setAppliedJobsData(createJobListState());
    setResumeFile(null);
    setAuthView("login");
    setSessionReady(true);
    if (clearMessage) {
      setMessage({ type: "", text: "" });
    }
  }

  function updateForm(setter, field, value) {
    setter((current) => ({ ...current, [field]: value }));
  }

  function updateJobFilter(field, value) {
    setJobFilters((current) => ({ ...current, [field]: value, current: 1 }));
  }

  function saveJobActionOverrides(nextValue, user = auth.user) {
    setJobActionOverrides(nextValue);
    writeLocalJson(getUserScopedStorageKey(user, "job_actions"), nextValue);
  }

  async function refreshAllJobLists(filters = jobFilters) {
    await Promise.all([
      loadRecommendedJobs(filters, false, true),
      loadFavoriteJobs(filters, false, true),
      loadAppliedJobs(filters, false, true)
    ]);
  }

  async function handleToggleJobLike(job) {
    const currentLiked = normalizeBoolean(jobActionOverrides[job.jobId]?.liked ?? job.liked);
    try {
      setJobLoading(true);
      await request(`/api/jobs/${job.jobId}/favorite`, {
        method: currentLiked ? "DELETE" : "POST"
      });
      const nextOverrides = {
        ...jobActionOverrides,
        [job.jobId]: {
          liked: !currentLiked,
          applied: jobActionOverrides[job.jobId]?.applied ?? job.applied
        }
      };
      saveJobActionOverrides(nextOverrides);
      await refreshAllJobLists(jobFilters);
      setMessage({
        type: "success",
        text: currentLiked ? "已从收藏职位移除。" : "已加入收藏职位，后续可以集中查看。"
      });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "收藏职位失败" });
    } finally {
      setJobLoading(false);
    }
  }

  async function handleToggleJobApplied(job) {
    const currentApplied = normalizeBoolean(jobActionOverrides[job.jobId]?.applied ?? job.applied);
    try {
      setJobLoading(true);
      await request(`/api/jobs/${job.jobId}/apply`, {
        method: currentApplied ? "DELETE" : "POST"
      });
      const nextOverrides = {
        ...jobActionOverrides,
        [job.jobId]: {
          liked: jobActionOverrides[job.jobId]?.liked ?? job.liked,
          applied: !currentApplied
        }
      };
      saveJobActionOverrides(nextOverrides);
      await refreshAllJobLists(jobFilters);
      setMessage({
        type: "success",
        text: currentApplied ? "已取消已投递标记。" : "已标记为已投递，后续会出现在已投递列表。"
      });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "标记投递失败" });
    } finally {
      setJobLoading(false);
    }
  }

  async function handleApplyNow(job) {
    if (!job?.applyUrl) {
      setMessage({ type: "error", text: "这个职位暂时没有原始投递链接，后续可以等后端补齐 applyUrl 字段。" });
      return;
    }

    const openedWindow = window.open("about:blank", "_blank");
    try {
      if (openedWindow) {
        openedWindow.opener = null;
        openedWindow.location.replace(job.applyUrl);
      } else {
        window.open(job.applyUrl, "_blank", "noopener,noreferrer");
      }

      setPendingApplyFollowUpJobId(job.jobId);
      setApplyFollowUpLeftPage(false);
      setShowApplyFollowUpModal(false);
      setMessage({
        type: "success",
        text: "已为你打开职位原始链接。完成投递后，回到页面我们会继续帮你确认。"
      });
    } catch (error) {
      if (openedWindow && !openedWindow.closed) {
        openedWindow.close();
      }
      setMessage({ type: "error", text: error.message || "跳转职位原始链接失败" });
    }
  }

  async function confirmAppliedAfterReturn() {
    if (!pendingApplyFollowUpJob) {
      setShowApplyFollowUpModal(false);
      setPendingApplyFollowUpJobId(null);
      setApplyFollowUpLeftPage(false);
      return;
    }

    const currentApplied = normalizeBoolean(
      jobActionOverrides[pendingApplyFollowUpJob.jobId]?.applied ?? pendingApplyFollowUpJob.applied
    );
    try {
      setJobLoading(true);
      if (!currentApplied) {
        await request(`/api/jobs/${pendingApplyFollowUpJob.jobId}/apply`, { method: "POST" });
        const nextOverrides = {
          ...jobActionOverrides,
          [pendingApplyFollowUpJob.jobId]: {
            liked:
              jobActionOverrides[pendingApplyFollowUpJob.jobId]?.liked ?? pendingApplyFollowUpJob.liked,
            applied: true
          }
        };
        saveJobActionOverrides(nextOverrides);
        await refreshAllJobLists(jobFilters);
      }
      setMessage({ type: "success", text: "已帮你标记为已投递，后续可以在已投递列表中继续跟进。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "标记投递失败" });
    } finally {
      setJobLoading(false);
      setShowApplyFollowUpModal(false);
      setPendingApplyFollowUpJobId(null);
      setApplyFollowUpLeftPage(false);
    }
  }

  function dismissApplyFollowUp() {
    setShowApplyFollowUpModal(false);
    setPendingApplyFollowUpJobId(null);
    setApplyFollowUpLeftPage(false);
    setMessage({ type: "success", text: "好的，当前不会把这个职位标记为已投递。" });
  }

  function openJobDetail(job) {
    setSelectedJobId(job.jobId);
  }

  function closeJobDetail() {
    setSelectedJobId(null);
  }

  async function handleJobSearch(event) {
    event.preventDefault();
    await loadJobsByActiveTab(jobFilters);
  }

  async function handleResetFilters() {
    setJobFilters(jobFilterInitialState);
    await loadJobList(activeTab, jobFilterInitialState);
  }

  async function handleResumeUpload(event) {
    event.preventDefault();
    if (!resumeFile) {
      setMessage({ type: "error", text: "请先选择一份简历文件。" });
      return;
    }
    setLoading(true);
    setMessage({ type: "", text: "" });
    try {
      const formData = new FormData();
      formData.append("file", resumeFile);
      await request("/api/user/resume/upload", {
        method: "POST",
        body: formData
      });
      await loadCurrentResume();
      await loadUserDashboard();
      await refreshAllJobLists(jobFilterInitialState);
      setResumeFile(null);
      setMessage({ type: "success", text: "简历上传成功，已进入首页。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function handleResumeFilePick(event) {
    setResumeFile(event.target.files?.[0] || null);
  }

  async function openResumePreview() {
    const fallbackPreview = buildResumePreviewContent(resumeInfo, userProfileDraft, auth.user);
    setResumePreviewOpen(true);
    setResumePreviewLoading(true);
    setResumePreviewData(fallbackPreview);

    if (!resumeInfo?.resumeId) {
      setResumePreviewLoading(false);
      return;
    }

    try {
      const remotePreview = await request(`/api/user/resume/${resumeInfo.resumeId}/preview`, { method: "GET" });
      setResumePreviewData({
        ...fallbackPreview,
        ...remotePreview
      });
    } catch {
      setResumePreviewData(fallbackPreview);
    } finally {
      setResumePreviewLoading(false);
    }
  }

  function closeResumePreview() {
    setResumePreviewOpen(false);
  }

  function handleDownloadResume() {
    if (!resumePreviewData?.downloadUrl) {
      setMessage({ type: "error", text: "当前简历还没有可下载地址。" });
      return;
    }
    window.open(`${API_BASE_URL}${resumePreviewData.downloadUrl}`, "_blank", "noopener,noreferrer");
  }

  function handleSidebarSwitch(sectionKey) {
    setActiveSection(sectionKey);
  }

  function handleProfileTabClick(tabKey) {
    setActiveProfileTab(tabKey);
    profileSectionRefs[tabKey]?.current?.scrollIntoView({
      behavior: "smooth",
      block: "start"
    });
  }

  function openProfileEditor(type) {
    if (!auth.user) {
      return;
    }
    if (type === "personal") {
      setProfilePersonalEditorDraft((current) => current || readLocalJson(
        getUserScopedStorageKey(auth.user, "profile_personal_editor"),
        buildProfilePersonalEditorDraft(auth.user, userProfileDraft)
      ));
    }
    if (type === "work") {
      setProfileWorkEditorDraft((current) => current || readLocalJson(
        getUserScopedStorageKey(auth.user, "profile_work_editor"),
        buildProfileWorkEditorDraft(userProfileDraft)
      ));
    }
    if (type === "skills") {
      setProfileSkillsEditorDraft((current) => current || readLocalJson(
        getUserScopedStorageKey(auth.user, "profile_skills_editor"),
        buildProfileSkillsEditorDraft(userProfileDraft)
      ));
    }
    if (type === "education") {
      setProfileEducationEditorDraft((current) => current || readLocalJson(
        getUserScopedStorageKey(auth.user, "profile_education_editor"),
        buildProfileEducationEditorDraft()
      ));
    }
    if (type === "preferences") {
      setProfilePreferencesEditorDraft((current) => current || readLocalJson(
        getUserScopedStorageKey(auth.user, "profile_preferences_editor"),
        buildProfilePreferencesEditorDraft(userProfileDraft)
      ));
    }
    setProfileEditorType(type);
  }

  function closeProfileEditor() {
    setProfileEditorType("");
  }

  function handleProfilePersonalEditorChange(field, value) {
    setProfilePersonalEditorDraft((current) => ({ ...(current || profilePersonalView), [field]: value }));
  }

  function handleProfileWorkEditorChange(field, value) {
    setProfileWorkEditorDraft((current) => ({ ...(current || profileWorkView), [field]: value }));
  }

  function handleProfileSkillsEditorChange(field, value) {
    setProfileSkillsEditorDraft((current) => ({ ...(current || profileSkillsView), [field]: value }));
  }

  function handleProfileEducationEditorChange(field, value) {
    setProfileEducationEditorDraft((current) => ({ ...(current || profileEducationView), [field]: value }));
  }

  function handleProfilePreferencesEditorChange(field, value) {
    setProfilePreferencesEditorDraft((current) => ({ ...(current || profilePreferencesView), [field]: value }));
  }

  function handleAddSkillItem() {
    const nextSkill = String(profileSkillsView.pendingSkill || "").trim();
    if (!nextSkill || profileSkillsView.items.includes(nextSkill)) {
      return;
    }
    setProfileSkillsEditorDraft((current) => ({
      ...(current || profileSkillsView),
      items: [...(current?.items || profileSkillsView.items), nextSkill],
      pendingSkill: ""
    }));
  }

  function handleRemoveSkillItem(skill) {
    setProfileSkillsEditorDraft((current) => ({
      ...(current || profileSkillsView),
      items: (current?.items || profileSkillsView.items).filter((item) => item !== skill)
    }));
  }

  function handleProfileWorkBulletChange(index, value) {
    setProfileWorkEditorDraft((current) => ({
      ...(current || profileWorkView),
      jobDescriptionBullets: (current?.jobDescriptionBullets || profileWorkView.jobDescriptionBullets).map((item, itemIndex) => (
        itemIndex === index ? value : item
      ))
    }));
  }

  function handleRemoveProfileWorkBullet(index) {
    setProfileWorkEditorDraft((current) => ({
      ...(current || profileWorkView),
      jobDescriptionBullets: (current?.jobDescriptionBullets || profileWorkView.jobDescriptionBullets).filter((_, itemIndex) => itemIndex !== index)
    }));
  }

  function handleAddProfileWorkBullet() {
    setProfileWorkEditorDraft((current) => ({
      ...(current || profileWorkView),
      jobDescriptionBullets: [...(current?.jobDescriptionBullets || profileWorkView.jobDescriptionBullets), ""]
    }));
  }

  function handleSavePersonalProfileEditor() {
    if (!auth.user || !profilePersonalView.firstName.trim()) {
      setMessage({ type: "error", text: "请先填写名字。" });
      return;
    }
    const nextDisplayName = [profilePersonalView.firstName.trim(), profilePersonalView.lastName.trim()]
      .filter(Boolean)
      .join(" ");
    const nextProfileDraft = {
      ...userProfileDraft,
      expectedCity: profilePersonalView.city.trim() || userProfileDraft.expectedCity
    };
    setAuth((current) => ({
      ...current,
      user: current.user
        ? { ...current.user, displayName: nextDisplayName, email: profilePersonalView.email.trim() || current.user.email }
        : current.user
    }));
    setUserProfileDraft(nextProfileDraft);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_personal_editor"), profilePersonalView);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), nextProfileDraft);
    setMessage({ type: "success", text: "个人信息已更新。" });
    closeProfileEditor();
  }

  function handleSaveWorkProfileEditor() {
    if (!auth.user || !profileWorkView.jobTitle.trim()) {
      setMessage({ type: "error", text: "请先填写职位名称。" });
      return;
    }
    const nextProfileDraft = {
      ...userProfileDraft,
      targetRole: profileWorkView.jobTitle.trim(),
      expectedCity: profileWorkView.location.trim() || userProfileDraft.expectedCity,
      personalSummary: (profileWorkView.jobDescriptionBullets || []).map((item) => item.trim()).filter(Boolean).join("\n")
    };
    setUserProfileDraft(nextProfileDraft);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_work_editor"), profileWorkView);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), nextProfileDraft);
    setMessage({ type: "success", text: "工作经历已更新。" });
    closeProfileEditor();
  }

  function handleSaveSkillsProfileEditor() {
    if (!auth.user || !Array.isArray(profileSkillsView.items) || profileSkillsView.items.length === 0) {
      setMessage({ type: "error", text: "请至少保留一个技能标签。" });
      return;
    }
    const nextProfileDraft = {
      ...userProfileDraft,
      keywordTags: profileSkillsView.items.join(", ")
    };
    setUserProfileDraft(nextProfileDraft);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_skills_editor"), profileSkillsView);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), nextProfileDraft);
    setMessage({ type: "success", text: "技能标签已更新。" });
    closeProfileEditor();
  }

  function handleSaveEducationProfileEditor() {
    if (!auth.user || !profileEducationView.school.trim()) {
      setMessage({ type: "error", text: "请先填写学校名称。" });
      return;
    }
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_education_editor"), profileEducationView);
    setMessage({ type: "success", text: "教育经历已更新。" });
    closeProfileEditor();
  }

  function handleSavePreferencesProfileEditor() {
    if (!auth.user) {
      return;
    }
    const nextProfileDraft = {
      ...userProfileDraft,
      targetRole: profilePreferencesView.targetRole.trim(),
      expectedCity: profilePreferencesView.expectedCity.trim(),
      jobTypes: profilePreferencesView.jobTypes,
      openToRemote: profilePreferencesView.openToRemote,
      requireVisaSupport: profilePreferencesView.requireVisaSupport
    };
    setUserProfileDraft(nextProfileDraft);
    setProfilePreferencesEditorDraft(profilePreferencesView);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_preferences_editor"), profilePreferencesView);
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), nextProfileDraft);
    setMessage({ type: "success", text: "求职偏好已更新。" });
    closeProfileEditor();
  }

  async function handleJobTabChange(tab) {
    const nextFilters = { ...jobFilters, current: 1 };
    setActiveTab(tab);
    setJobFilters(nextFilters);
    await loadJobList(tab, nextFilters);
  }

  function handleProfileDraftChange(field, value) {
    setUserProfileDraft((current) => ({ ...current, [field]: value }));
  }

  function handleProfileJobTypeToggle(jobType) {
    setUserProfileDraft((current) => {
      const currentTypes = Array.isArray(current.jobTypes) ? current.jobTypes : [];
      const nextTypes = currentTypes.includes(jobType)
        ? currentTypes.filter((item) => item !== jobType)
        : [...currentTypes, jobType];
      return { ...current, jobTypes: nextTypes };
    });
  }

  function handleProfilePreferencesJobTypeToggle(jobType) {
    setProfilePreferencesEditorDraft((current) => {
      const base = current || profilePreferencesView;
      const currentTypes = Array.isArray(base.jobTypes) ? base.jobTypes : [];
      const nextTypes = currentTypes.includes(jobType)
        ? currentTypes.filter((item) => item !== jobType)
        : [...currentTypes, jobType];
      return { ...base, jobTypes: nextTypes };
    });
  }

  function handleSaveUserProfileDraft() {
    return saveProfilePreferences("个人资料已保存，首页推荐和 onboarding 偏好会保持一致。");
  }

  async function saveProfilePreferences(successText) {
    if (!userProfileDraft.targetRole.trim()) {
      setMessage({ type: "error", text: "请先填写目标岗位方向。" });
      return false;
    }
    if (!Array.isArray(userProfileDraft.jobTypes) || userProfileDraft.jobTypes.length === 0) {
      setMessage({ type: "error", text: "请至少选择一种求职类型。" });
      return false;
    }
    if (!userProfileDraft.expectedCity.trim() && !userProfileDraft.openToRemote) {
      setMessage({ type: "error", text: "请填写期望地点，或开启接受远程办公。" });
      return false;
    }
    setLoading(true);
    try {
      await request("/api/user/profile/preferences", {
        method: "PUT",
        body: JSON.stringify(userProfileDraft)
      });
      writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), userProfileDraft);
      setMessage({ type: "success", text: successText || "个人资料已保存。" });
      return true;
    } catch (error) {
      setMessage({ type: "error", text: error.message || "保存求职偏好失败" });
      return false;
    } finally {
      setLoading(false);
    }
  }

  async function handleCompleteProfileOnboarding(event) {
    event.preventDefault();
    await saveProfilePreferences("求职偏好已保存。");
  }

  function handleSettingsDraftChange(field, value) {
    setSettingsDraft((current) => ({ ...current, [field]: value }));
  }

  function handleSaveSettingsDraft() {
    writeLocalJson(getUserScopedStorageKey(auth.user, "settings_draft"), settingsDraft);
    setMessage({ type: "success", text: "系统设置已保存，新的体验偏好已在本地生效。" });
  }

  function handleUpgradePlan(plan) {
    localStorage.setItem(getUserScopedStorageKey(auth.user, "plan_name"), plan.name);
    setUserDashboard((current) => ({ ...current, planName: plan.name }));
    setMessage({
      type: "success",
      text: `${plan.name} 已在前端演示环境中启用，后续接入支付和后端套餐接口后即可持久化。`
    });
  }

  function renderUserWorkspaceSection() {
    if (activeSection === "agent" || activeSection === "coach") {
      return (
        <section className="user-home-shell">
          <article className="user-home-hero">
            <div>
              <span className="eyebrow">求职工作区</span>
              <h1>{activeSection === "agent" ? "求职助手能力正在准备接入。" : "求职辅导专区即将开放。"}</h1>
              <p>
                这一块我先给你留了工作区入口，当前版本重点先落在个人主页、简历完善和套餐升级。
              </p>
            </div>
          </article>
        </section>
      );
    }

    if (activeSection === "resume") {
      return (
        <section className="user-home-shell">
          <article className="resume-page-shell">
            <input
              ref={resumeUploadInputRef}
              type="file"
              accept=".pdf,.doc,.docx"
              onChange={handleResumeFilePick}
              hidden
            />
            <header className="resume-page-header">
              <div className="resume-title-block">
                <span className="eyebrow">简历中心</span>
              </div>
            </header>

            <div className="resume-toolbar">
              <div className="resume-status-bar">
                <span className="resume-status-dot" />
                <strong>你当前已保存 1 份简历，最多可维护 5 个简历槽位。</strong>
              </div>
              <div className="resume-toolbar-actions">
                <span className="resume-toolbar-tip">上传新版本后会自动刷新职位推荐、匹配分和工作台提示。</span>
                <button
                  className="resume-add-button"
                  type="button"
                  onClick={() => resumeUploadInputRef.current?.click()}
                >
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M10 4.5v11" />
                    <path d="M4.5 10h11" />
                  </svg>
                  <span>{resumeInfo ? "替换简历" : "Add Resume"}</span>
                </button>
              </div>
            </div>

            <section className="resume-main-card">
              <section className="resume-table-card">
                <div className="resume-table-head">
                  <span>简历</span>
                  <span>目标岗位</span>
                  <span>最近更新</span>
                  <span>创建时间</span>
                  <span />
                </div>

                <div className="resume-table-body">
                  <article
                    className={resumeInfo ? "resume-table-row clickable" : "resume-table-row"}
                    onClick={resumeInfo ? openResumePreview : undefined}
                    role={resumeInfo ? "button" : undefined}
                    tabIndex={resumeInfo ? 0 : undefined}
                    onKeyDown={(event) => {
                      if (!resumeInfo) {
                        return;
                      }
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        void openResumePreview();
                      }
                    }}
                  >
                    <div className="resume-cell resume-primary-cell">
                      <span className="resume-avatar">{getAuthDisplayName(auth.user).slice(0, 1).toUpperCase()}</span>
                      <div className="resume-primary-copy">
                        <strong>{resumeInfo?.fileName || "当前还没有简历"}</strong>
                        <div className="resume-primary-tags">
                          <span className="resume-chip primary">
                            {resumeInfo ? "当前版本" : "未上传"}
                          </span>
                          <span className="resume-chip">{resumeInfo?.status || "ACTIVE"}</span>
                          <span className="resume-chip">评分 {resumeInfo?.score || dashboardView.resumeScore}</span>
                        </div>
                      </div>
                    </div>
                    <div className="resume-cell">
                      <span className="resume-cell-text">{userProfileDraft.targetRole || "后端开发"}</span>
                    </div>
                    <div className="resume-cell">
                      <span className="resume-cell-text">{formatResumeRelativeTime(resumeInfo?.uploadTime)}</span>
                    </div>
                    <div className="resume-cell">
                      <span className="resume-cell-text">{formatResumeAbsoluteTime(resumeInfo?.uploadTime)}</span>
                    </div>
                    <div className="resume-cell resume-row-actions">
                      <button
                        className="resume-row-icon"
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();
                          resumeUploadInputRef.current?.click();
                        }}
                        aria-label="替换简历"
                      >
                        <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                          <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                          <path d="M11.93 5.99 14.01 8.07" />
                        </svg>
                      </button>
                    </div>
                  </article>
                </div>
              </section>

              {resumeFile ? (
              <form className="resume-upload-toolbar" onSubmit={handleResumeUpload}>
                <div className={resumeFile ? "selected-file-card resume-picker-card" : "selected-file-card empty resume-picker-card"}>
                  <strong>{resumeFile ? resumeFile.name : "还没有选择新文件"}</strong>
                  <span>
                    {resumeFile
                      ? `${Math.max(1, Math.round(resumeFile.size / 1024))} KB`
                      : "支持 PDF、DOC、DOCX。上传后会直接替换当前生效简历。"}
                  </span>
                </div>
                <div className="resume-upload-actions">
                  <button
                    className="ghost-button"
                    type="button"
                    onClick={() => setActiveSection("profile")}
                  >
                    去个人资料
                  </button>
                  <button className="primary-button" disabled={loading || !resumeFile} type="submit">
                    {loading ? "上传中..." : "上传并刷新简历"}
                  </button>
                </div>
              </form>
              ) : null}
            </section>

            {resumePreviewOpen ? (
              <div className="resume-preview-overlay" onClick={closeResumePreview} role="presentation">
                <aside
                  className="resume-preview-panel"
                  onClick={(event) => event.stopPropagation()}
                >
                  <div className="resume-workbench-toolbar">
                    <div className="resume-workbench-left">
                      <button className="resume-preview-close flat" onClick={closeResumePreview} type="button">
                        ×
                      </button>
                      <div className="resume-file-pill">
                        <span className="resume-file-pill-star">★</span>
                        <strong>{resumePreviewData?.fileName || "当前简历"}</strong>
                      </div>
                    </div>
                    <div className="resume-workbench-actions">
                      <button className="resume-action-button success" type="button">反馈</button>
                      <button className="resume-action-button" type="button">编辑简历信息</button>
                      <button className="resume-action-button" onClick={handleDownloadResume} type="button">导出</button>
                      <button className="resume-action-button subtle" type="button">删除</button>
                    </div>
                  </div>

                  <div className="resume-preview-body">
                    {resumePreviewLoading ? (
                      <div className="resume-preview-loading">正在加载简历内容...</div>
                    ) : null}

                    <section className="resume-workbench-hero">
                      <div className="resume-workbench-grade">
                        <div className="resume-grade-badge hex">{resumePreviewData?.score?.grade || "A"}</div>
                        <div className="resume-workbench-grade-copy">
                          <div className="resume-workbench-grade-row">
                            <strong>{resumePreviewData?.score?.label || "EXCELLENT"}</strong>
                            <button className="resume-report-button" type="button">查看完整报告</button>
                          </div>
                          <span>评分 {resumePreviewData?.score?.scoreValue || 88}</span>
                        </div>
                      </div>

                      <div className="resume-workbench-scoreboard">
                        <article className="fix-card urgent">
                          <strong>{resumePreviewData?.score?.urgentFixCount ?? 0}</strong>
                          <span>紧急修复</span>
                        </article>
                        <article className="fix-card critical">
                          <strong>{resumePreviewData?.score?.criticalFixCount ?? 0}</strong>
                          <span>重要修复</span>
                        </article>
                        <article className="fix-card optional">
                          <strong>{resumePreviewData?.score?.optionalFixCount ?? 0}</strong>
                          <span>可选优化</span>
                        </article>
                        <div className="resume-reanalyze-card">
                          <strong>重新分析</strong>
                          <span>最近更新 {formatResumeRelativeTime(resumePreviewData?.updatedAt)}</span>
                        </div>
                        <div className="resume-credit-pill">还可用 1 次分析机会</div>
                      </div>
                    </section>

                    <section className="resume-editor-surface">
                      <div className="resume-editor-name-row">
                        <div>
                          <h2>{resumePreviewData?.profile?.name || getAuthDisplayName(auth.user)}</h2>
                          <p>{resumePreviewData?.profile?.title || "后端开发"}</p>
                        </div>
                        <div className="resume-urgent-pill">
                          <span>{resumePreviewData?.score?.urgentFixCount ?? 0} 项紧急问题</span>
                          <button type="button">修复建议</button>
                        </div>
                      </div>

                      <div className="resume-contact-grid">
                        {getResumeContactCards(resumePreviewData, userProfileDraft, auth.user).map((item) => (
                          <article key={item.key} className="resume-contact-card">
                            <div className="resume-contact-head">
                              <span className="resume-contact-icon">
                                {item.icon === "mail" ? "✉" : item.icon === "phone" ? "✆" : item.icon === "location" ? "⌖" : item.icon === "github" ? "⌘" : item.icon === "link" ? "in" : "◎"}
                              </span>
                              <strong>{item.value}</strong>
                            </div>
                            <span>{item.hint}</span>
                          </article>
                        ))}
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>技术技能</h3>
                        <button className="resume-icon-action" type="button" aria-label="删除模块">🗑</button>
                      </div>
                      <div className="resume-skill-editor-list">
                        {(resumePreviewData?.skillGroups || []).map((group) => (
                          <article key={group.title} className="resume-skill-editor-group">
                            <strong>{group.title}</strong>
                            <div className="resume-skill-editor-row">
                              {(group.items || []).map((item) => (
                                <span key={`${group.title}-${item}`} className="resume-editor-chip">{item} <em>×</em></span>
                              ))}
                              <span className="resume-editor-chip ghost">添加技能...</span>
                              <button className="resume-chip-help" type="button">?</button>
                            </div>
                          </article>
                        ))}
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>工作经历</h3>
                        <button className="resume-outline-add" type="button">+ 工作经历</button>
                      </div>
                      <div className="resume-experience-list">
                        {(resumePreviewData?.workExperiences || []).map((item) => (
                          <article key={`${item.company}-${item.role}`} className="resume-experience-card">
                            <strong>{item.company}</strong>
                            <span>{item.role}</span>
                            <ul>
                              {(item.bullets || []).map((bullet) => (
                                <li key={`${item.company}-${bullet}`}>{bullet}</li>
                              ))}
                            </ul>
                            <button className="resume-outline-add small" type="button">+ 要点描述</button>
                          </article>
                        ))}
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>项目经历</h3>
                        <button className="resume-outline-add" type="button">+ 项目经历</button>
                      </div>
                      <div className="resume-project-editor-list">
                        {(resumePreviewData?.projects || []).map((project) => (
                          <article key={project.name} className="resume-project-editor-card">
                            <strong>{project.name}</strong>
                            <div className="resume-project-tech">
                              {(project.technologies || []).map((tech) => (
                                <span key={`${project.name}-${tech}`}>{tech}</span>
                              ))}
                            </div>
                            <ul>
                              {(project.bullets || []).map((bullet) => (
                                <li key={`${project.name}-${bullet}`}>{bullet}</li>
                              ))}
                            </ul>
                            <button className="resume-outline-add small" type="button">+ 要点描述</button>
                          </article>
                        ))}
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>证书与能力说明</h3>
                        <button className="resume-outline-add" type="button">+ 证书信息</button>
                      </div>
                      <div className="resume-certification-list">
                        {(resumePreviewData?.certifications || []).map((item) => (
                          <article key={item.name} className="resume-certification-card">
                            <strong>{item.name}</strong>
                            <p>{item.description}</p>
                          </article>
                        ))}
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>分析摘要</h3>
                      </div>
                      <div className="resume-preview-lines">
                        <p>{resumePreviewData?.score?.summary}</p>
                        <p>{resumePreviewData?.analysisSummary}</p>
                      </div>
                    </section>

                    <section className="resume-preview-section">
                      <div className="resume-section-title-row">
                        <h3>优化建议</h3>
                        <button className="resume-fab" type="button" aria-label="更多操作">
                          ≣
                        </button>
                      </div>
                      <div className="resume-issue-list">
                        {(resumePreviewData?.urgentIssues || []).map((issue) => (
                          <article key={issue.title} className="resume-issue-card">
                            <strong>{issue.title}</strong>
                            <p>{issue.description}</p>
                          </article>
                        ))}
                      </div>
                    </section>
                  </div>
                </aside>
              </div>
            ) : null}
          </article>
        </section>
      );
    }

    if (activeSection === "settings") {
      return (
        <section className="user-home-shell">
          <article className="user-home-hero">
            <div>
              <span className="eyebrow">系统设置</span>
              <h1>把通知、职位浏览体验和隐私偏好放在一个地方统一管理。</h1>
              <p>当前版本先把常用设置做成前端可保存，后续可以继续接入服务端同步。</p>
            </div>
            <div className="user-home-kpis">
              <article>
                <strong>{settingsDraft.emailNotifications ? "已开启" : "已关闭"}</strong>
                <span>邮件通知</span>
              </article>
              <article>
                <strong>{settingsDraft.pushNewMatches ? "实时提醒" : "静默模式"}</strong>
                <span>岗位推送</span>
              </article>
              <article>
                <strong>{settingsDraft.privateProfile ? "仅自己可见" : "基础可见"}</strong>
                <span>隐私设置</span>
              </article>
            </div>
          </article>

          <div className="user-home-grid">
            <section className="user-home-card settings-card">
              <div className="rail-title">
                <strong>通知偏好</strong>
                <button type="button" onClick={handleSaveSettingsDraft}>立即保存</button>
              </div>
              <div className="settings-list">
                <label className="setting-item">
                  <div>
                    <strong>邮件通知</strong>
                    <span>接收推荐职位、简历处理结果和账号提醒。</span>
                  </div>
                  <input
                    checked={settingsDraft.emailNotifications}
                    onChange={(event) => handleSettingsDraftChange("emailNotifications", event.target.checked)}
                    type="checkbox"
                  />
                </label>
                <label className="setting-item">
                  <div>
                    <strong>每周求职周报</strong>
                    <span>汇总本周匹配岗位、投递节奏和资料完善建议。</span>
                  </div>
                  <input
                    checked={settingsDraft.weeklyReport}
                    onChange={(event) => handleSettingsDraftChange("weeklyReport", event.target.checked)}
                    type="checkbox"
                  />
                </label>
                <label className="setting-item">
                  <div>
                    <strong>新匹配职位提醒</strong>
                    <span>当出现高匹配职位时，优先在工作台中提醒你。</span>
                  </div>
                  <input
                    checked={settingsDraft.pushNewMatches}
                    onChange={(event) => handleSettingsDraftChange("pushNewMatches", event.target.checked)}
                    type="checkbox"
                  />
                </label>
              </div>
            </section>

            <section className="user-home-card settings-card">
              <div className="rail-title">
                <strong>浏览体验</strong>
                <button type="button" onClick={() => setActiveSection("jobs")}>返回职位页</button>
              </div>
              <div className="settings-list">
                <label className="setting-item">
                  <div>
                    <strong>自动加载更多职位</strong>
                    <span>滚动到底部时自动请求下一页职位。</span>
                  </div>
                  <input
                    checked={settingsDraft.autoLoadMoreJobs}
                    onChange={(event) => handleSettingsDraftChange("autoLoadMoreJobs", event.target.checked)}
                    type="checkbox"
                  />
                </label>
                <label className="setting-item">
                  <div>
                    <strong>紧凑职位卡片</strong>
                    <span>减少职位卡片留白，更适合连续浏览大量岗位。</span>
                  </div>
                  <input
                    checked={settingsDraft.compactCards}
                    onChange={(event) => handleSettingsDraftChange("compactCards", event.target.checked)}
                    type="checkbox"
                  />
                </label>
                <label className="setting-item">
                  <div>
                    <strong>显示匹配原因</strong>
                    <span>在职位卡片中展示画像匹配解释和技能命中信息。</span>
                  </div>
                  <input
                    checked={settingsDraft.showMatchReason}
                    onChange={(event) => handleSettingsDraftChange("showMatchReason", event.target.checked)}
                    type="checkbox"
                  />
                </label>
              </div>
            </section>
          </div>

          <div className="user-home-grid">
            <section className="user-home-card settings-card">
              <div className="rail-title">
                <strong>隐私与账号</strong>
                <button type="button" onClick={handleSaveSettingsDraft}>保存隐私设置</button>
              </div>
              <div className="settings-list">
                <label className="setting-item">
                  <div>
                    <strong>隐藏公开画像</strong>
                    <span>关闭后，个人画像只用于系统内部推荐，不在公开区域展示。</span>
                  </div>
                  <input
                    checked={settingsDraft.privateProfile}
                    onChange={(event) => handleSettingsDraftChange("privateProfile", event.target.checked)}
                    type="checkbox"
                  />
                </label>
                <label className="setting-item">
                  <div>
                    <strong>展示简历评分</strong>
                    <span>在工作台和简历中心中保留评分结果与趋势提示。</span>
                  </div>
                  <input
                    checked={settingsDraft.shareResumeScore}
                    onChange={(event) => handleSettingsDraftChange("shareResumeScore", event.target.checked)}
                    type="checkbox"
                  />
                </label>
              </div>
            </section>

            <section className="user-home-card settings-card settings-card-highlight">
              <strong>建议先开启哪些设置？</strong>
              <ul className="settings-tips">
                <li>开启“新匹配职位提醒”，及时发现高匹配岗位。</li>
                <li>保留“显示匹配原因”，更容易判断推荐质量。</li>
                <li>如果你经常集中刷岗位，可以开启“自动加载更多职位”。</li>
              </ul>
            </section>
          </div>
        </section>
      );
    }

    const profileKeywordTags = profileSkillsView.items;
    const profileSummaryBullets = (profileWorkView.jobDescriptionBullets || []).filter((item) => String(item || "").trim());
    const profileDisplayName = [profilePersonalView.firstName, profilePersonalView.lastName].filter(Boolean).join(" ") || getAuthDisplayName(auth.user);
    const profileDisplayLocation = profilePersonalView.city?.trim() || getProfileLocationText(userProfileDraft);

    return (
      <section className="profile-page-shell">
        <header className="profile-page-topbar">
          <h1>个人资料</h1>
        </header>

        <div className="profile-page-grid">
          <section className="profile-main-card">
            <div className="profile-tab-row">
              {profileTabItems.map((item) => (
                <button
                  key={item.key}
                  className={`profile-tab${activeProfileTab === item.key ? " active" : ""}`}
                  onClick={() => handleProfileTabClick(item.key)}
                  type="button"
                >
                  {item.label}
                </button>
              ))}
            </div>

            <section className="profile-section-card hero" ref={profileBasicRef}>
              <div className="profile-section-head">
                <div>
                  <h2>{profileDisplayName}</h2>
                  <span className="profile-location-pill">
                    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                      <path d="M10 17s4.7-3.76 4.7-7.82A4.7 4.7 0 1 0 5.3 9.18C5.3 13.24 10 17 10 17Z" />
                      <path d="M10 10.9a1.9 1.9 0 1 0 0-3.8 1.9 1.9 0 0 0 0 3.8Z" />
                    </svg>
                    <span>{profileDisplayLocation}</span>
                  </span>
                </div>
                <button className="profile-edit-button" type="button" onClick={() => openProfileEditor("personal")}>
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                    <path d="M11.93 5.99 14.01 8.07" />
                  </svg>
                </button>
              </div>

              <div className="profile-display-grid">
                <article className="profile-display-item">
                  <span>目标岗位</span>
                  <div>{userProfileDraft.targetRole || "待补充目标岗位"}</div>
                </article>
                <article className="profile-display-item">
                  <span>期望城市</span>
                  <div>{userProfileDraft.expectedCity || "待补充期望城市"}</div>
                </article>
                <article className="profile-display-item full-width summary">
                  <span>个人亮点摘要</span>
                  <div>{userProfileDraft.personalSummary || "待补充个人亮点摘要"}</div>
                </article>
              </div>
            </section>

            <section className="profile-section-card" ref={profileEducationRef}>
              <div className="profile-section-head inline">
                <div>
                  <h3>教育经历</h3>
                  <p>{profileEducationView.school ? `${profileEducationView.school} · ${profileEducationView.degree} · ${profileEducationView.fieldOfStudy}` : "补充教育经历后，系统更容易识别校招、实习和当前阶段的匹配逻辑。"}</p>
                </div>
                <button className="profile-edit-button" type="button" onClick={() => openProfileEditor("education")}>
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                    <path d="M11.93 5.99 14.01 8.07" />
                  </svg>
                </button>
              </div>
              <button className="profile-outline-action" type="button" onClick={() => openProfileEditor("education")}>
                + 添加教育经历
              </button>
            </section>

            <section className="profile-section-card" ref={profileWorkRef}>
              <div className="profile-section-head inline">
                <div>
                  <h3>工作经历</h3>
                  <p>项目经历越具体，推荐和匹配原因越稳定，也更方便后续做自动填充。</p>
                </div>
                <button className="profile-edit-button" type="button" onClick={() => openProfileEditor("work")}>
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                    <path d="M11.93 5.99 14.01 8.07" />
                  </svg>
                </button>
              </div>

              <article className="profile-timeline-card">
                <div className="profile-timeline-marker" />
                <div className="profile-timeline-content">
                  <strong>{profileWorkView.jobTitle || "后端开发实习生"}</strong>
                  <span>{profileWorkView.location || profileDisplayLocation}</span>
                  <ul className="profile-bullet-list">
                    {(profileSummaryBullets.length ? profileSummaryBullets : [
                      "补充你的核心项目和业务场景，让招聘侧更快看懂你的经验边界。",
                      "尽量写清楚你使用的技术栈，以及你负责过的模块和结果。",
                      "如果有性能优化、稳定性提升或交付提效，建议直接写出量化结果。"
                    ]).map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                </div>
              </article>
            </section>

            <section className="profile-section-card" ref={profileSkillsRef}>
              <div className="profile-section-head inline">
                <div>
                  <h3>技能标签</h3>
                  <p>关键词拆成技能标签后，职位命中原因和推荐展示会更直观。</p>
                </div>
                <button className="profile-edit-button" type="button" onClick={() => openProfileEditor("skills")}>
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                    <path d="M11.93 5.99 14.01 8.07" />
                  </svg>
                </button>
              </div>
              <label className="profile-inline-input">
                <span>岗位关键词</span>
                <input
                  value={profileSkillsView.items.join(", ")}
                  placeholder="例如 Java, Spring Boot, MySQL"
                  readOnly
                />
              </label>
              <div className="profile-skill-tags">
                {(profileKeywordTags.length ? profileKeywordTags : ["Java", "Spring Boot", "MySQL"]).map((tag) => (
                  <span key={tag}>{tag}</span>
                ))}
              </div>
            </section>
          </section>

          <aside className="profile-side-column">
            <section className="profile-alert-card">
              <div className="profile-alert-icon">!</div>
              <strong>完善资料能提高职位匹配准确度，也方便后续自动填充投递信息。</strong>
              <button className="profile-alert-button" onClick={handleSaveUserProfileDraft} type="button">
                保存个人资料
              </button>
            </section>

            <section className="profile-side-links">
              <button type="button" onClick={() => setActiveSection("resume")}>
                <span>管理我的简历</span>
                <span>›</span>
              </button>
              <button type="button" onClick={() => setActiveSection("jobs")}>
                <span>返回职位推荐</span>
                <span>›</span>
              </button>
            </section>

            <section className="profile-side-links soft">
              <button type="button" onClick={() => handleUpgradePlan(planOptions[1])}>
                <span>当前套餐 · {dashboardView.planName}</span>
                <span>›</span>
              </button>
              <button type="button" onClick={handleSaveUserProfileDraft}>
                <span>完善度 · {dashboardView.profileCompletionRate}%</span>
                <span>›</span>
              </button>
            </section>
          </aside>
        </div>

        {profileEditorType ? (
          <div className="profile-editor-overlay" onClick={closeProfileEditor} role="presentation">
            <div
              className="profile-editor-modal"
              onClick={(event) => event.stopPropagation()}
              role="dialog"
              aria-modal="true"
            >
              <div className="profile-editor-header">
                <button className="profile-editor-back" onClick={closeProfileEditor} type="button" aria-label="关闭编辑页">
                  ›
                </button>
                <strong>
                  {profileEditorType === "personal"
                    ? "个人信息"
                    : profileEditorType === "education"
                      ? "教育经历"
                    : profileEditorType === "work"
                      ? "工作经历"
                      : profileEditorType === "skills"
                        ? "技能标签"
                        : "求职偏好"}
                </strong>
                <button
                  className="profile-editor-update"
                  onClick={
                    profileEditorType === "personal"
                      ? handleSavePersonalProfileEditor
                      : profileEditorType === "education"
                        ? handleSaveEducationProfileEditor
                      : profileEditorType === "work"
                        ? handleSaveWorkProfileEditor
                        : profileEditorType === "skills"
                          ? handleSaveSkillsProfileEditor
                          : handleSavePreferencesProfileEditor
                  }
                  type="button"
                >
                  更新
                </button>
              </div>

              <div className="profile-editor-body">
                {profileEditorType === "personal" ? (
                  <div className="profile-editor-form-grid">
                    <label>
                      <span>* 名</span>
                      <input
                        value={profilePersonalView.firstName}
                        onChange={(event) => handleProfilePersonalEditorChange("firstName", event.target.value)}
                        placeholder="请输入名字"
                      />
                    </label>
                    <label>
                      <span>* 姓</span>
                      <input
                        value={profilePersonalView.lastName}
                        onChange={(event) => handleProfilePersonalEditorChange("lastName", event.target.value)}
                        placeholder="请输入姓氏"
                      />
                    </label>
                    <label>
                      <span>* 邮箱</span>
                      <input
                        value={profilePersonalView.email}
                        onChange={(event) => handleProfilePersonalEditorChange("email", event.target.value)}
                        placeholder="请输入邮箱"
                      />
                    </label>
                    <label>
                      <span>手机号</span>
                      <input
                        value={profilePersonalView.phone}
                        onChange={(event) => handleProfilePersonalEditorChange("phone", event.target.value)}
                        placeholder="请输入手机号"
                      />
                    </label>
                    <label>
                      <span>国家/地区</span>
                      <input
                        value={profilePersonalView.countryRegion}
                        onChange={(event) => handleProfilePersonalEditorChange("countryRegion", event.target.value)}
                        placeholder="请输入国家/地区"
                      />
                    </label>
                    <label>
                      <span>城市</span>
                      <input
                        value={profilePersonalView.city}
                        onChange={(event) => handleProfilePersonalEditorChange("city", event.target.value)}
                        placeholder="请输入城市"
                      />
                    </label>
                    <label>
                      <span>区县</span>
                      <input
                        value={profilePersonalView.county}
                        onChange={(event) => handleProfilePersonalEditorChange("county", event.target.value)}
                        placeholder="请输入区县"
                      />
                    </label>
                    <label>
                      <span>邮编</span>
                      <input
                        value={profilePersonalView.postalCode}
                        onChange={(event) => handleProfilePersonalEditorChange("postalCode", event.target.value)}
                        placeholder="请输入邮编"
                      />
                    </label>
                    <label className="full-width">
                      <span>详细地址</span>
                      <input
                        value={profilePersonalView.addressLine}
                        onChange={(event) => handleProfilePersonalEditorChange("addressLine", event.target.value)}
                        placeholder="请输入详细地址"
                      />
                    </label>
                    <label className="full-width">
                      <span>* LinkedIn 地址</span>
                      <input
                        value={profilePersonalView.linkedInUrl}
                        onChange={(event) => handleProfilePersonalEditorChange("linkedInUrl", event.target.value)}
                        placeholder="请输入 LinkedIn 地址"
                      />
                    </label>
                    <label className="full-width">
                      <span>GitHub 地址</span>
                      <input
                        value={profilePersonalView.githubUrl}
                        onChange={(event) => handleProfilePersonalEditorChange("githubUrl", event.target.value)}
                        placeholder="请输入 GitHub 地址"
                      />
                    </label>
                  </div>
                ) : null}

                {profileEditorType === "work" ? (
                  <div className="profile-editor-work-stack">
                    <div className="profile-editor-block">
                      <div className="profile-editor-block-head">
                        <strong>工作经历 1</strong>
                      </div>
                      <div className="profile-editor-form-grid">
                        <label className="full-width">
                          <span>* 职位名称</span>
                          <input
                            value={profileWorkView.jobTitle}
                            onChange={(event) => handleProfileWorkEditorChange("jobTitle", event.target.value)}
                            placeholder="请输入职位名称"
                          />
                        </label>
                        <label className="full-width">
                          <span>* 公司名称</span>
                          <input
                            value={profileWorkView.company}
                            onChange={(event) => handleProfileWorkEditorChange("company", event.target.value)}
                            placeholder="请输入公司名称"
                          />
                        </label>
                        <label>
                          <span>* 工作类型</span>
                          <input
                            value={profileWorkView.jobType}
                            onChange={(event) => handleProfileWorkEditorChange("jobType", event.target.value)}
                            placeholder="请输入工作类型"
                          />
                        </label>
                        <label>
                          <span>工作地点</span>
                          <input
                            value={profileWorkView.location}
                            onChange={(event) => handleProfileWorkEditorChange("location", event.target.value)}
                            placeholder="请输入工作地点"
                          />
                        </label>
                        <label>
                          <span>开始时间</span>
                          <input
                            value={profileWorkView.startDate}
                            onChange={(event) => handleProfileWorkEditorChange("startDate", event.target.value)}
                            placeholder="请选择开始时间"
                          />
                        </label>
                        <label>
                          <span>结束时间</span>
                          <input
                            value={profileWorkView.endDate}
                            onChange={(event) => handleProfileWorkEditorChange("endDate", event.target.value)}
                            placeholder="请选择结束时间"
                          />
                        </label>
                        <label className="full-width">
                          <span>经历概述</span>
                          <textarea
                            rows={3}
                            value={profileWorkView.experienceSummary}
                            onChange={(event) => handleProfileWorkEditorChange("experienceSummary", event.target.value)}
                            placeholder="补充 1-2 句岗位职责概述"
                          />
                        </label>
                      </div>
                    </div>

                    <div className="profile-editor-bullets">
                      <strong>工作描述</strong>
                      {(profileWorkView.jobDescriptionBullets || []).map((bullet, index) => (
                        <div key={`work-bullet-${index}`} className="profile-editor-bullet-row">
                          <textarea
                            rows={2}
                            value={bullet}
                            onChange={(event) => handleProfileWorkBulletChange(index, event.target.value)}
                            placeholder="请输入一条工作描述"
                          />
                          <button type="button" onClick={() => handleRemoveProfileWorkBullet(index)}>×</button>
                        </div>
                      ))}
                      <button className="profile-editor-outline" onClick={handleAddProfileWorkBullet} type="button">
                        + 添加描述要点
                      </button>
                    </div>
                  </div>
                ) : null}

                {profileEditorType === "skills" ? (
                  <div className="profile-editor-skills-stack">
                    <div className="profile-editor-chip-grid">
                      {profileSkillsView.items.map((skill) => (
                        <span key={skill} className="profile-editor-skill-chip">
                          {skill}
                          <button type="button" onClick={() => handleRemoveSkillItem(skill)}>×</button>
                        </span>
                      ))}
                      <label className="profile-editor-skill-input">
                        <input
                          value={profileSkillsView.pendingSkill}
                          onChange={(event) => handleProfileSkillsEditorChange("pendingSkill", event.target.value)}
                          onKeyDown={(event) => {
                            if (event.key === "Enter") {
                              event.preventDefault();
                              handleAddSkillItem();
                            }
                          }}
                          placeholder="添加技能..."
                        />
                      </label>
                    </div>
                  </div>
                ) : null}

                {profileEditorType === "education" ? (
                  <div className="profile-editor-form-grid">
                    <label className="full-width">
                      <span>* 学校名称</span>
                      <input
                        value={profileEducationView.school}
                        onChange={(event) => handleProfileEducationEditorChange("school", event.target.value)}
                        placeholder="请输入学校名称"
                      />
                    </label>
                    <label>
                      <span>学历</span>
                      <input
                        value={profileEducationView.degree}
                        onChange={(event) => handleProfileEducationEditorChange("degree", event.target.value)}
                        placeholder="请输入学历"
                      />
                    </label>
                    <label>
                      <span>专业</span>
                      <input
                        value={profileEducationView.fieldOfStudy}
                        onChange={(event) => handleProfileEducationEditorChange("fieldOfStudy", event.target.value)}
                        placeholder="请输入专业"
                      />
                    </label>
                    <label>
                      <span>所在地</span>
                      <input
                        value={profileEducationView.location}
                        onChange={(event) => handleProfileEducationEditorChange("location", event.target.value)}
                        placeholder="请输入所在地"
                      />
                    </label>
                    <label>
                      <span>开始时间</span>
                      <input
                        value={profileEducationView.startDate}
                        onChange={(event) => handleProfileEducationEditorChange("startDate", event.target.value)}
                        placeholder="请选择开始时间"
                      />
                    </label>
                    <label>
                      <span>结束时间</span>
                      <input
                        value={profileEducationView.endDate}
                        onChange={(event) => handleProfileEducationEditorChange("endDate", event.target.value)}
                        placeholder="请选择结束时间"
                      />
                    </label>
                    <label className="full-width">
                      <span>补充说明</span>
                      <textarea
                        rows={4}
                        value={profileEducationView.highlights}
                        onChange={(event) => handleProfileEducationEditorChange("highlights", event.target.value)}
                        placeholder="可补充 GPA、竞赛、科研或奖项经历"
                      />
                    </label>
                  </div>
                ) : null}

                {profileEditorType === "preferences" ? (
                  <div className="profile-editor-work-stack">
                    <div className="profile-editor-form-grid">
                      <label>
                        <span>目标岗位</span>
                        <input
                          value={profilePreferencesView.targetRole}
                          onChange={(event) => handleProfilePreferencesEditorChange("targetRole", event.target.value)}
                          placeholder="请输入目标岗位"
                        />
                      </label>
                      <label>
                        <span>期望城市</span>
                        <input
                          value={profilePreferencesView.expectedCity}
                          onChange={(event) => handleProfilePreferencesEditorChange("expectedCity", event.target.value)}
                          placeholder="请输入期望城市"
                        />
                      </label>
                    </div>

                    <div className="profile-editor-inline-group">
                      <strong>求职类型</strong>
                      <div className="profile-choice-row">
                        {onboardingJobTypeOptions.map((jobType) => (
                          <button
                            key={`editor-${jobType}`}
                            className={`profile-choice-chip${profilePreferencesView.jobTypes.includes(jobType) ? " active" : ""}`}
                            onClick={() => handleProfilePreferencesJobTypeToggle(jobType)}
                            type="button"
                          >
                            {jobType}
                          </button>
                        ))}
                      </div>
                    </div>

                    <div className="profile-editor-inline-group">
                      <strong>投递偏好</strong>
                      <div className="profile-toggle-list">
                        <label className="profile-toggle-item">
                          <input
                            checked={profilePreferencesView.openToRemote}
                            onChange={(event) => handleProfilePreferencesEditorChange("openToRemote", event.target.checked)}
                            type="checkbox"
                          />
                          <span>接受远程办公</span>
                        </label>
                        <label className="profile-toggle-item">
                          <input
                            checked={profilePreferencesView.requireVisaSupport}
                            onChange={(event) => handleProfilePreferencesEditorChange("requireVisaSupport", event.target.checked)}
                            type="checkbox"
                          />
                          <span>需要签证支持</span>
                        </label>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        ) : null}
      </section>
    );
  }

  if (auth.token && !sessionReady) {
    return (
      <div className="auth-shell">
        <div className="auth-backdrop auth-left" />
        <div className="auth-backdrop auth-right" />
        <main className="auth-layout">
          <section className="auth-hero">
            <div className="auth-brand-row">
              <div className="auth-brand-mark">J</div>
              <div>
                <span className="eyebrow">JobBright 求职工作台</span>
                <strong className="auth-brand-name">正在同步你的偏好、简历和工作台状态</strong>
              </div>
            </div>
          </section>
          <section className="auth-panel">
            <div className="auth-form">
              <h2>加载中</h2>
              <p>正在连接账号信息并恢复 onboarding 进度，请稍候。</p>
            </div>
          </section>
        </main>
      </div>
    );
  }

  if (!auth.token || !auth.user) {
    return (
      <div className="auth-shell">
        <div className="auth-backdrop auth-left" />
        <div className="auth-backdrop auth-right" />

        <main className="auth-layout">
          <section className="auth-hero">
            <div className="auth-brand-row">
              <div className="auth-brand-mark">J</div>
              <div>
                <span className="eyebrow">JobBright 求职工作台</span>
                <strong className="auth-brand-name">把求职过程收进一个清爽的工作区</strong>
              </div>
            </div>

            <div className="auth-hero-copy">
              <h1>先看匹配机会，再决定投什么。</h1>
              <p className="auth-copy">
                登录后会先收集你的岗位方向和地点偏好，再上传当前简历，让首页推荐、筛选条件和投递跟进自然串起来。
              </p>
            </div>

            <div className="auth-hero-highlights">
              <article className="auth-highlight-card primary">
                <small>推荐优先</small>
                <strong>职位、简历、投递放在同一条链路里。</strong>
                <span>不用在多个 demo 页面之间来回切换。</span>
              </article>
              <div className="auth-highlight-grid">
                <article className="auth-highlight-card">
                  <strong>画像参与匹配</strong>
                  <span>岗位方向、城市和关键词会直接影响推荐。</span>
                </article>
                <article className="auth-highlight-card">
                  <strong>投递状态可跟踪</strong>
                  <span>收藏、已投递和后续跟进放在一个首页里管理。</span>
                </article>
              </div>
            </div>

            <div className="auth-mini-metrics">
              <article>
                <strong>3 步</strong>
                <span>完成偏好采集和简历接入</span>
              </article>
              <article>
                <strong>关键字段优先</strong>
                <span>首页先展示地点、类型、薪资和匹配度</span>
              </article>
            </div>
          </section>

          <section className="auth-panel">
              <div className="auth-panel-head">
              <div>
                <span className="eyebrow">欢迎回来</span>
                <strong>进入你的求职工作台</strong>
              </div>
              <span className="auth-panel-pill">推荐优先</span>
              </div>

              <div className="auth-tabs">
                <button
                  className={authView === "login" ? "auth-tab active" : "auth-tab"}
                  onClick={() => setAuthView("login")}
                  type="button"
                >
                  登录
                </button>
                <button
                  className={authView === "register" ? "auth-tab active" : "auth-tab"}
                  onClick={() => setAuthView("register")}
                  type="button"
                >
                  注册
                </button>
              </div>

              {message.text ? (
                <div className={message.type === "error" ? "notice error" : "notice success"}>
                  {message.text}
                </div>
              ) : null}

              {authView === "login" ? (
                <form className="auth-form" onSubmit={handleLogin}>
                  <h2>登录</h2>
                  <p>登录后直接进入推荐职位流，继续你的筛选和投递节奏。</p>

                  <label>
                    用户名或邮箱
                    <input
                      value={loginForm.account}
                      onChange={(event) => updateForm(setLoginForm, "account", event.target.value)}
                      placeholder="请输入用户名或邮箱"
                    />
                  </label>

                  <label>
                    密码
                    <input
                      type="password"
                      value={loginForm.password}
                      onChange={(event) => updateForm(setLoginForm, "password", event.target.value)}
                      placeholder="请输入密码"
                    />
                  </label>

                  <label>
                    图形验证码
                    <div className="auth-inline-row">
                      <input
                        value={loginForm.captchaCode}
                        onChange={(event) => updateForm(setLoginForm, "captchaCode", event.target.value)}
                        placeholder="请输入图形验证码"
                      />
                      <button
                        className="auth-captcha-button"
                        onClick={() => refreshLoginCaptcha()}
                        type="button"
                        disabled={loginCaptchaLoading || loading}
                      >
                        {loginCaptchaLoading ? (
                          <span>加载中...</span>
                        ) : loginCaptcha?.imageData ? (
                          <img src={loginCaptcha.imageData} alt="图形验证码" />
                        ) : (
                          <span>获取验证码</span>
                        )}
                      </button>
                    </div>
                  </label>

                  <button
                    className="auth-refresh-link"
                    onClick={() => refreshLoginCaptcha()}
                    type="button"
                    disabled={loginCaptchaLoading || loading}
                  >
                    看不清？换一张
                  </button>

                  <button className="primary-button" disabled={loading} type="submit">
                    {loading ? "登录中..." : "立即登录"}
                  </button>

                  <div className="helper-text">演示账号：`demo / JobBacked123`</div>
                </form>
              ) : (
                <form className="auth-form" onSubmit={handleRegister}>
                  <h2>创建账号</h2>
                  <p>注册后就能保存简历、生成画像并建立自己的推荐与投递轨迹。</p>

                  <label>
                    用户名
                    <input
                    value={registerForm.username}
                    onChange={(event) =>
                        updateForm(setRegisterForm, "username", event.target.value)
                      }
                      placeholder="请输入用户名"
                    />
                  </label>

                  <label>
                    邮箱
                    <div className="auth-inline-row">
                      <input
                        value={registerForm.email}
                        onChange={(event) => updateForm(setRegisterForm, "email", event.target.value)}
                        placeholder="请输入邮箱"
                      />
                      <button
                        className="auth-code-button"
                        disabled={sendCodeLoading || sendCodeCooldown > 0 || loading}
                        onClick={handleSendVerificationCode}
                        type="button"
                      >
                        {sendCodeLoading
                          ? "发送中..."
                          : sendCodeCooldown > 0
                            ? `${sendCodeCooldown}s`
                            : "发送验证码"}
                      </button>
                    </div>
                  </label>

                  <label>
                    邮箱验证码
                    <input
                      value={registerForm.verificationCode}
                      onChange={(event) => updateForm(setRegisterForm, "verificationCode", event.target.value)}
                      placeholder="请输入收到的邮箱验证码"
                    />
                  </label>

                  <label>
                    显示名称
                    <input
                      value={registerForm.displayName}
                      onChange={(event) =>
                        updateForm(setRegisterForm, "displayName", event.target.value)
                      }
                      placeholder="请输入显示名称"
                    />
                  </label>

                  <label>
                    密码
                    <input
                      type="password"
                      value={registerForm.password}
                      onChange={(event) =>
                        updateForm(setRegisterForm, "password", event.target.value)
                      }
                      placeholder="请输入密码"
                    />
                  </label>

                  <button className="primary-button" disabled={loading} type="submit">
                    {loading ? "注册中..." : "创建账号"}
                  </button>

                  <div className="helper-text">
                    当前注册页已接入邮箱验证码发送接口，注册提交本身暂未校验验证码。
                  </div>
                </form>
              )}
          </section>
        </main>
      </div>
    );
  }

  if (!resumeInfo) {
    return (
      <div className="auth-shell">
        <div className="auth-backdrop auth-left" />
        <div className="auth-backdrop auth-right" />

        <main className="auth-layout onboarding-layout resume-gate-layout">
          <section className="auth-hero onboarding-hero resume-gate-hero">
            <div className="auth-brand-row">
              <div className="auth-brand-mark">O</div>
              <div>
                <span className="eyebrow">JobBright 简历上传</span>
                <strong className="auth-brand-name">上传当前简历后，就可以开始浏览首页推荐</strong>
              </div>
            </div>

            <div className="auth-hero-copy onboarding-copy">
              <h1>先上传简历，再进入职位首页。</h1>
              <p className="auth-copy">
                当前版本登录后不再强制收集偏好信息，上传一份最新简历后就能直接进入推荐职位流。
              </p>
            </div>

            <div className="resume-gate-benefits">
              <article>
                <strong>上传后会发生什么</strong>
                <span>系统会用简历参与职位匹配、筛选默认值和推荐解释生成。</span>
              </article>
              <article>
                <strong>建议上传最新版本</strong>
                <span>优先使用最近投递的简历版本，首页结果会更贴近当前求职方向。</span>
              </article>
            </div>
          </section>

          <section className="auth-panel resume-upload-panel polished">
            <div className="onboarding-topbar">
              <span className="auth-panel-pill">上传后开始匹配</span>
              <button className="ghost-button compact-button" onClick={() => logout()} type="button">
                退出登录
              </button>
            </div>

            <h2>上传当前简历</h2>
            <p>支持 PDF、DOC、DOCX，建议上传最新版本并控制在 10MB 以内。</p>

            {message.text ? (
              <div className={message.type === "error" ? "notice error" : "notice success"}>
                {message.text}
              </div>
            ) : null}

            <form className="auth-form resume-upload-form" onSubmit={handleResumeUpload}>
              <input
                ref={onboardingResumeInputRef}
                type="file"
                accept=".pdf,.doc,.docx"
                onChange={handleResumeFilePick}
                hidden
              />

              <button
                className="resume-dropzone"
                onClick={() => onboardingResumeInputRef.current?.click()}
                type="button"
              >
                <div className="resume-dropzone-icon">
                  <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M12 16V7.5" />
                    <path d="m8.5 11 3.5-3.5 3.5 3.5" />
                    <path d="M6 18.5h12" />
                    <path d="M7 20h10a2.5 2.5 0 0 0 2.5-2.5V8.7a2.5 2.5 0 0 0-.84-1.87l-4.4-3.95A2.5 2.5 0 0 0 12.6 2H7A2.5 2.5 0 0 0 4.5 4.5v13A2.5 2.5 0 0 0 7 20Z" />
                  </svg>
                </div>
                <strong>{resumeFile ? "重新选择简历文件" : "上传你的简历"}</strong>
                <span>支持 PDF、DOC、DOCX，拖放体验后续也可以继续补。</span>
                <em>点击选择文件</em>
              </button>

              <div className="resume-upload-specs">
                <span>支持格式：PDF / DOC / DOCX</span>
                <span>文件大小：建议不超过 10MB</span>
              </div>

              {resumeFile ? (
                <div className="selected-file-card">
                  <div className="selected-file-head">
                    <strong>{resumeFile.name}</strong>
                    <span>{Math.max(1, Math.round(resumeFile.size / 1024))} KB</span>
                  </div>
                  <small>已选中，将作为当前生效简历参与职位匹配。</small>
                </div>
              ) : (
                <div className="selected-file-card empty">
                  <strong>还没有选择文件</strong>
                  <span>请上传最新简历后继续。</span>
                </div>
              )}

              <div className="resume-upload-privacy">
                简历仅用于职位匹配、简历解析和推荐解释，不会在未授权情况下用于公开展示。
              </div>

              <button className="primary-button onboarding-next-button" disabled={loading} type="submit">
                {loading ? "上传中..." : "开始匹配职位"}
              </button>
            </form>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="dashboard-shell">
      {activeSection === "jobs" ? (
        <div className="promo-bar">
          <span>本周重点机会仍在持续刷新。</span>
          <strong>优化简历和筛选条件，可以显著提升岗位匹配度。</strong>
        </div>
      ) : null}

      <div className={`dashboard-layout${activeSection === "resume" ? " resume-layout" : ""}${activeSection === "profile" ? " profile-layout" : ""}${activeSection !== "jobs" ? " no-promo" : ""}`}>
        <aside className="sidebar">
          <div className="brand-block">
            <div className="brand-mark">J</div>
            <div>
              <strong>JobBright</strong>
              <span>求职工作台</span>
            </div>
          </div>

          <nav className="sidebar-nav">
            {sidebarItems.map((item) => (
              <button
                key={item.key}
                className={activeSection === item.key ? "sidebar-link active" : "sidebar-link"}
                onClick={() => handleSidebarSwitch(item.key)}
                type="button"
              >
                <span className="sidebar-link-main">
                  <span className="sidebar-link-icon">
                    <SidebarIcon type={item.icon} />
                  </span>
                  <span>{item.label}</span>
                </span>
                {item.badge ? <em>{item.badge}</em> : null}
              </button>
            ))}
          </nav>

          <div className="sidebar-card sidebar-score-card">
            <div className="sidebar-score-main">
              <small>当前简历评分</small>
              <strong>{dashboardView.resumeScore} / 100</strong>
            </div>
            <button className="ghost-button" onClick={() => setActiveSection("resume")} type="button">
              优化简历
            </button>
          </div>

          <div className="sidebar-footer">
            <button type="button">消息中心</button>
            <button type="button">意见反馈</button>
            <button onClick={() => handleSidebarSwitch("settings")} type="button">系统设置</button>
            <button onClick={() => logout()} type="button">退出登录</button>
          </div>
        </aside>

        <main className="main-panel" ref={mainPanelRef}>
          {!isUserHomeSection ? (
            <header className="topbar">
              <div className="topbar-title">
                <span className="section-label">职位</span>
                <div className="tab-strip">
                  {jobTabs.map((tab) => (
                    <button
                      key={tab}
                      className={activeTab === tab ? "top-tab active" : "top-tab"}
                      onClick={() => handleJobTabChange(tab)}
                      type="button"
                    >
                      <span>{tab}</span>
                      {getTabBadgeCount(tab) > 0 ? (
                        <em className="top-tab-badge">{getTabBadgeCount(tab)}</em>
                      ) : null}
                    </button>
                  ))}
                </div>
              </div>

              <div className="topbar-actions">
                <label className="search-box">
                  <span>搜索</span>
                  <input
                    placeholder="搜索职位、公司或关键词"
                    value={jobFilters.keyword}
                    onChange={(event) => updateJobFilter("keyword", event.target.value)}
                  />
                </label>
                <button className="turbo-button" type="button">
                  加速求职模式
                </button>
              </div>
            </header>
          ) : null}

          {message.text ? (
            <div className={message.type === "error" ? "notice error" : "notice success"}>
              {message.text}
            </div>
          ) : null}

          {!isUserHomeSection ? (
            <>
          <section className="filter-panel">
            <div className="filter-toolbar">
              <div>
                <strong>智能筛选</strong>
                <span>先看核心条件，需要时再展开更多筛选。</span>
              </div>
              <button
                className={filterExpanded ? "filter-toggle active" : "filter-toggle"}
                onClick={() => setFilterExpanded((current) => !current)}
                type="button"
              >
                <span className={filterExpanded ? "filter-toggle-arrow active" : "filter-toggle-arrow"}>
                  <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    <path d="m5 7.5 5 5 5-5" />
                  </svg>
                </span>
                {filterExpanded ? "收起筛选" : "更多筛选"}
              </button>
            </div>

            {getFilterSummaryItems().length ? (
              <div className="filter-summary-row">
                {getFilterSummaryItems().map((item) => (
                  <button
                    key={item.key}
                    className="filter-summary-chip"
                    onClick={() => clearJobFilter(item.key)}
                    type="button"
                  >
                    <span>{item.label}</span>
                    <strong>×</strong>
                  </button>
                ))}
              </div>
            ) : null}

            <form className="filter-grid" onSubmit={handleJobSearch}>
              <label className="filter-field">
                国家
                <FilterSelect
                  value={jobFilters.country}
                  options={countryOptions}
                  onChange={(nextValue) => updateJobFilter("country", nextValue)}
                  placeholder="全部国家"
                />
              </label>
              <label className="filter-field">
                职位名称
                <input
                  value={jobFilters.title}
                  onChange={(event) => updateJobFilter("title", event.target.value)}
                  placeholder="如 后端开发"
                />
              </label>
              <label className="filter-field">
                城市
                <FilterSelect
                  value={jobFilters.city}
                  options={cityOptions}
                  onChange={(nextValue) => updateJobFilter("city", nextValue)}
                  placeholder="全部城市"
                />
              </label>
              <label className="filter-field">
                经验等级
                <FilterSelect
                  value={jobFilters.experienceLevel}
                  options={[{ value: "", label: "全部等级" }, ...experienceLevelOptions]}
                  onChange={(nextValue) => updateJobFilter("experienceLevel", nextValue)}
                  placeholder="全部等级"
                />
              </label>

              <div className={filterExpanded ? "filter-grid-advanced expanded" : "filter-grid-advanced"}>
              <label className="filter-field">
                工作类型
                <FilterSelect
                  value={jobFilters.employmentType}
                  options={employmentTypeOptions}
                  onChange={(nextValue) => updateJobFilter("employmentType", nextValue)}
                  placeholder="全部类型"
                />
              </label>
              <label className="filter-field">
                工作模式
                <FilterSelect
                  value={jobFilters.workMode}
                  options={workModeOptions}
                  onChange={(nextValue) => updateJobFilter("workMode", nextValue)}
                  placeholder="全部模式"
                />
              </label>
              <label className="filter-field">
                发布时间
                <FilterSelect
                  value={jobFilters.datePosted}
                  options={datePostedOptions}
                  onChange={(nextValue) => updateJobFilter("datePosted", nextValue)}
                  placeholder="全部时间"
                />
              </label>
              <label className="filter-field">
                行业方向
                <FilterSelect
                  value={jobFilters.industryName}
                  options={industryOptions}
                  onChange={(nextValue) => updateJobFilter("industryName", nextValue)}
                  placeholder="全部行业"
                />
              </label>
              <label className="filter-field">
                学历要求
                <FilterSelect
                  value={jobFilters.educationRequirement}
                  options={educationOptions}
                  onChange={(nextValue) => updateJobFilter("educationRequirement", nextValue)}
                  placeholder="全部学历"
                />
              </label>
              <label className="filter-field">
                每次加载
                <FilterSelect
                  value={jobFilters.size}
                  options={sizeOptions}
                  onChange={(nextValue) => updateJobFilter("size", nextValue)}
                  placeholder="10"
                />
              </label>
              </div>

              <div className="filter-actions filter-actions-inline">
                <button className="filter-secondary-button" onClick={handleResetFilters} type="button">
                  重置筛选
                </button>
                <button className="filter-primary-button" disabled={jobLoading} type="submit">
                  {jobLoading ? "筛选中..." : "应用筛选"}
                </button>
              </div>
            </form>
          </section>

          <section className="results-bar">
            <strong>{activeTab}</strong>
            <span>
              已显示 {visibleJobRecords.length} / {visibleJobTotal} 条
            </span>
          </section>

          <section className="job-feed">
            {visibleJobRecords.length ? visibleJobRecords.map((job) => (
              <article
                key={job.jobId}
                className="job-card"
                onClick={() => openJobDetail(job)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    openJobDetail(job);
                  }
                }}
                role="button"
                tabIndex={0}
              >
                <div className="job-card-main">
                  <div className="job-card-header">
                    <CompanyBadge job={job} />

                    <div className="job-copy">
                      <div className="job-card-topline">
                        <span className="posted-chip">{job.postedAt || "刚刚发布"}</span>
                        {job.highlightTags?.[0] ? (
                          <span className="topline-chip">{job.highlightTags[0]}</span>
                        ) : null}
                      </div>
                      <h2>{job.title}</h2>
                      {getJobMetaLine(job) ? <p className="job-meta">{getJobMetaLine(job)}</p> : null}
                    </div>

                    <button
                      className="job-card-menu"
                      type="button"
                      aria-label="查看职位详情"
                      onClick={(event) => {
                        event.stopPropagation();
                        openJobDetail(job);
                      }}
                    >
                      <span />
                      <span />
                      <span />
                    </button>
                  </div>

                  <div className="job-detail-grid">
                    {getJobDetailItems(job).slice(0, 6).map((item) => (
                      <div key={`${job.jobId}-${item.icon}-${item.label}`} className="job-detail-item">
                        <span className="job-detail-icon">
                          <JobDetailIcon type={item.icon} />
                        </span>
                        <span>{item.label}</span>
                      </div>
                    ))}
                  </div>

                  <div className="job-card-divider" />

                  <div className="job-footer">
                    <div className="job-footnote">
                      {job.applicantCount > 0 ? `${job.applicantCount} 人已投递` : "少于 25 人已投递"}
                    </div>

                    <div className="job-footer-actions">
                      <button
                        className="job-action-icon-button"
                        onClick={(event) => {
                          event.stopPropagation();
                          void handleToggleJobApplied(job);
                        }}
                        type="button"
                        aria-label={job.applied ? "取消投递" : "标记投递"}
                      >
                        <JobActionIcon type="skip" />
                      </button>
                      <button
                        className="job-action-icon-button"
                        onClick={(event) => {
                          event.stopPropagation();
                          void handleToggleJobLike(job);
                        }}
                        type="button"
                        aria-label={job.liked ? "取消收藏" : "收藏"}
                      >
                        <JobActionIcon type="favorite" />
                      </button>
                      <button
                        className="job-assistant-button"
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();
                        }}
                      >
                        <JobActionIcon type="spark" />
                        <span>问求职助手</span>
                      </button>
                      <button
                        className="job-apply-button"
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();
                          void handleApplyNow(job);
                        }}
                      >
                        立即申请
                      </button>
                    </div>
                  </div>
                </div>

                <aside className="job-match-panel">
                  <div className="match-ring">
                    <span>{job.matchScore}%</span>
                  </div>
                  <strong>{job.matchLabel}</strong>
                  <div className="job-match-divider" />
                  <p>{job.matchReason}</p>
                </aside>
              </article>
            )) : (
              <div className="empty-state-card">
                <strong>{getEmptyStateCopy().title}</strong>
                <span>{getEmptyStateCopy().description}</span>
              </div>
            )}
          </section>

          {activeTab === "推荐职位" && visibleJobRecords.length ? (
            <button
              className={hasMoreJobs ? "load-more-anchor interactive" : "load-more-anchor"}
              ref={loadMoreAnchorRef}
              onClick={() => handleLoadMoreRecommended()}
              type="button"
              disabled={jobLoading || !hasMoreJobs}
            >
              {jobLoading ? "正在加载更多职位..." : hasMoreJobs ? "继续下滑加载更多" : "已经到底了"}
            </button>
          ) : null}
            </>
          ) : (
            renderUserWorkspaceSection()
          )}
        </main>

        {activeSection !== "resume" && activeSection !== "profile" ? (
        <aside className="right-rail">
          <section className="profile-panel">
            <div className="profile-header profile-header-compact">
              <div className="profile-identity">
                <div className="avatar">
                  {getAuthDisplayName(auth.user).slice(0, 1)}
                </div>
                <div className="profile-copy">
                  <small className="profile-copy-eyebrow">当前账号</small>
                  <strong>{dashboardView.displayName}</strong>
                </div>
              </div>
              <div className="profile-plan-group">
                <span className="plan-icon" aria-hidden="true">
                  <svg viewBox="0 0 20 20" fill="none">
                    <path d="M10 2.4c3.78 0 6.84 3.06 6.84 6.84 0 3.49-2.61 6.38-5.98 6.8l-3.98 1.56 1.14-3.4A6.85 6.85 0 0 1 3.16 9.24C3.16 5.46 6.22 2.4 10 2.4Z" />
                    <path d="m6.8 11.7 6.28-6.27" />
                  </svg>
                </span>
                <span className="plan-pill">{dashboardView.planName}</span>
              </div>
            </div>
          </section>

          <section className="rail-card">
            <div className="rail-title">
              <strong>已保存筛选</strong>
              <button className="rail-round-button" type="button" aria-label="新增筛选方案">
                <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                  <path d="M10 4.5v11" />
                  <path d="M4.5 10h11" />
                </svg>
              </button>
            </div>
            <div className="saved-filter-list">
              {savedFilters.map((item, index) => (
                <div key={item} className="saved-filter-item">
                  <div className="saved-filter-copy">
                    <small>方案 {index + 1}</small>
                    <span className="saved-filter-name">{item}</span>
                  </div>
                  <button className="saved-filter-edit" type="button" aria-label={`编辑方案 ${index + 1}`}>
                    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                      <path d="M4.17 13.75 13.1 4.82a1.67 1.67 0 1 1 2.36 2.36l-8.93 8.93-2.78.42.42-2.78Z" />
                      <path d="M11.93 5.99 14.01 8.07" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          </section>

          <section className="rail-card progress-card">
            <div className="progress-card-header">
              <span className="progress-card-icon" aria-hidden="true">
                <svg viewBox="0 0 20 20" fill="none">
                  <path d="M10 2.8c2.5 0 4.53 2.03 4.53 4.53 0 1.72-.96 3.21-2.37 3.97v1.12a1.6 1.6 0 0 1-1.6 1.6H9.44a1.6 1.6 0 0 1-1.6-1.6v-1.12A4.53 4.53 0 0 1 5.47 7.33C5.47 4.83 7.5 2.8 10 2.8Z" />
                  <path d="M8.45 16.2h3.1" />
                  <path d="M8.85 13.95h2.3" />
                </svg>
              </span>
              <strong>完善资料以获得更高匹配职位</strong>
            </div>
            <div className="progress-shell">
              <div className="progress-bar">
                <div style={{ width: `${dashboardView.profileCompletionRate}%` }} />
              </div>
              <span className="progress-percent">{dashboardView.profileCompletionRate}%</span>
            </div>
            <ul className="progress-checklist">
              {dashboardView.tips.map((tip) => (
                <li key={tip}>
                  <span className="progress-step-index" aria-hidden="true" />
                  <span>{tip}</span>
                  <span className="progress-step-chevron" aria-hidden="true">
                    <svg viewBox="0 0 20 20" fill="none">
                      <path d="m6 8 4 4 4-4" />
                    </svg>
                  </span>
                </li>
              ))}
            </ul>
          </section>

        </aside>
        ) : null}
      </div>

      {selectedJob ? (
        <div className="job-detail-overlay" onClick={closeJobDetail} role="presentation">
          <section
            className="job-detail-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-label="职位详情"
          >
            <header className="job-detail-modal-header">
              <div className="job-detail-modal-identity">
                <CompanyBadge job={selectedJob} />
                <div className="job-detail-modal-copy">
                  <span className="job-detail-modal-company">{selectedJob.companyName || "企业"}</span>
                  <h2>{selectedJob.title}</h2>
                  {getJobMetaLine(selectedJob) ? <p>{getJobMetaLine(selectedJob)}</p> : null}
                </div>
              </div>
              <button
                className="job-detail-close-button"
                onClick={closeJobDetail}
                type="button"
                aria-label="关闭职位详情"
              >
                ×
              </button>
            </header>

            <div className="job-detail-modal-body">
              <section className="job-detail-primary">
                <div className="job-detail-hero">
                  <div className="job-detail-hero-main">
                    <div className="job-detail-chip-row">
                      {getJobKeyItems(selectedJob).map((item) => (
                        <span key={`${selectedJob.jobId}-${item}`} className="job-detail-chip">
                          {item}
                        </span>
                      ))}
                    </div>
                    <p className="job-detail-summary">
                      {selectedJob.jobSummary || "后端职位详情接口补齐后，这里会展示完整职位描述、任职要求和公司介绍。"}
                    </p>
                  </div>

                  <aside className="job-detail-score-card">
                    <div className="match-ring">
                      <span>{selectedJob.matchScore}%</span>
                    </div>
                    <strong>{selectedJob.matchLabel}</strong>
                    <p>{selectedJob.matchReason}</p>
                  </aside>
                </div>

                <div className="job-detail-section">
                  <h3>岗位信息</h3>
                  <div className="job-detail-info-grid">
                    {getJobDetailItems(selectedJob).map((item) => (
                      <div key={`${selectedJob.jobId}-${item.icon}-${item.label}`} className="job-detail-info-item">
                        <span className="job-detail-icon">
                          <JobDetailIcon type={item.icon} />
                        </span>
                        <span>{item.label}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {getJobTagGroups(selectedJob).map((group) => (
                  <div key={`${selectedJob.jobId}-${group.title}`} className="job-detail-section">
                    <h3>{group.title}</h3>
                    <div className="job-detail-chip-row">
                      {group.values.map((value) => (
                        <span key={`${group.title}-${value}`} className="job-detail-chip muted">
                          {value}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </section>

              <aside className="job-detail-sidebar">
                <div className="job-detail-side-card">
                  <strong>投递动作</strong>
                  <button
                    className="job-apply-button detail"
                    onClick={() => void handleApplyNow(selectedJob)}
                    type="button"
                  >
                    申请并跳转原始链接
                  </button>
                  <button
                    className="job-detail-secondary-button"
                    onClick={() => void handleToggleJobLike(selectedJob)}
                    type="button"
                  >
                    {selectedJob.liked ? "取消收藏职位" : "收藏职位"}
                  </button>
                  <button
                    className="job-detail-secondary-button"
                    onClick={() => {
                      if (selectedJob.applyUrl) {
                        window.open(selectedJob.applyUrl, "_blank", "noopener,noreferrer");
                      } else {
                        setMessage({ type: "error", text: "这个职位暂时没有原始链接。" });
                      }
                    }}
                    type="button"
                  >
                    查看原始职位链接
                  </button>
                </div>
              </aside>
            </div>
          </section>
        </div>
      ) : null}

      {showApplyFollowUpModal && pendingApplyFollowUpJob ? (
        <div className="apply-followup-overlay" role="presentation">
          <section
            className="apply-followup-modal"
            role="dialog"
            aria-modal="true"
            aria-label="确认是否已投递"
          >
            <button
              className="apply-followup-close"
              onClick={dismissApplyFollowUp}
              type="button"
              aria-label="关闭"
            >
              ×
            </button>
            <div className="apply-followup-icon" aria-hidden="true">
              <svg viewBox="0 0 64 64" fill="none">
                <path d="M18 23h28a6 6 0 0 1 6 6v18a8 8 0 0 1-8 8H20a8 8 0 0 1-8-8V29a6 6 0 0 1 6-6Z" />
                <path d="M24 23v-3a8 8 0 0 1 16 0v3" />
                <path d="M14 31c7 8 29 8 36 0" />
              </svg>
            </div>
            <h2>已经完成投递了吗？</h2>
            <p>
              告诉我们这次投递是否完成，我们会帮你更新投递状态，并让后续推荐更准确。
            </p>
            <div className="apply-followup-job-title">{pendingApplyFollowUpJob.title}</div>
            <div className="apply-followup-actions">
              <button
                className="apply-followup-primary"
                onClick={() => void confirmAppliedAfterReturn()}
                type="button"
              >
                是的，已投递
              </button>
              <button
                className="apply-followup-secondary"
                onClick={dismissApplyFollowUp}
                type="button"
              >
                还没有投递
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}

export default App;
