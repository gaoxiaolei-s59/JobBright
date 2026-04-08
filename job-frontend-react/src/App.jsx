import { useEffect, useRef, useState } from "react";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") || "http://localhost:10010";
const TOKEN_KEY = "jobbright_access_token";
const TOKEN_HEADER = "X-Access-Token";

const loginInitialState = {
  account: "demo",
  password: "JobBacked123"
};

const registerInitialState = {
  username: "",
  email: "",
  displayName: "",
  password: ""
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
  targetRole: "后端开发",
  keywordTags: "",
  personalSummary: ""
};

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

function getAuthDisplayName(user) {
  return user?.displayName || user?.username || "U";
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
  const [filterExpanded, setFilterExpanded] = useState(false);
  const [authView, setAuthView] = useState("login");
  const [loginForm, setLoginForm] = useState(loginInitialState);
  const [registerForm, setRegisterForm] = useState(registerInitialState);
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeInfo, setResumeInfo] = useState(null);
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
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(false);
  const [jobActionOverrides, setJobActionOverrides] = useState({});
  const mainPanelRef = useRef(null);
  const resumeUploadInputRef = useRef(null);
  const loadMoreLockRef = useRef(false);
  const [auth, setAuth] = useState(() => ({
    token: localStorage.getItem(TOKEN_KEY),
    user: null
  }));
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
  const visibleJobTotal = currentJobsData.total;
  const favoriteCount = favoriteJobsData.total;
  const appliedCount = appliedJobsData.total;

  useEffect(() => {
    if (auth.token) {
      loadCurrentUser();
    }
  }, []);

  useEffect(() => {
    const panel = mainPanelRef.current;
    if (!auth.token || !resumeInfo || !panel || jobLoading || !hasMoreJobs || activeTab !== "推荐职位" || isUserHomeSection) {
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
      loadMoreLockRef.current = true;
      setJobFilters((current) => {
        const nextFilters = {
          ...current,
          current: Number(current.current || 1) + 1
        };
        void loadRecommendedJobs(nextFilters, true);
        return nextFilters;
      });
    };

    tryLoadMore();
    panel.addEventListener("scroll", tryLoadMore, { passive: true });
    return () => panel.removeEventListener("scroll", tryLoadMore);
  }, [auth.token, resumeInfo, jobLoading, hasMoreJobs, activeTab, isUserHomeSection]);

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
    try {
      const user = await request("/api/auth/me", { method: "GET" });
      setAuth((current) => ({ ...current, user }));
      loadLocalUserProfileDraft(user);
      loadLocalSettingsDraft(user);
      loadLocalJobActions(user);
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
    setUserProfileDraft({
      ...userProfileDraftInitialState,
      ...storedDraft
    });
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
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setLoading(true);
    setMessage({ type: "", text: "" });
    try {
      await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(registerForm)
      });
      setRegisterForm(registerInitialState);
      setAuthView("login");
      setMessage({ type: "success", text: "注册成功，请使用新账号登录。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function logout(clearMessage = true) {
    localStorage.removeItem(TOKEN_KEY);
    setAuth({ token: null, user: null });
    setActiveSection("jobs");
    setActiveTab("推荐职位");
    setResumeInfo(null);
    setUserDashboard(userDashboardFallback);
    setUserProfileDraft(userProfileDraftInitialState);
    setJobActionOverrides({});
    setJobFilters(jobFilterInitialState);
    setRecommendedJobsData(createJobListState([], mockRecommendedJobs.length, mockRecommendedJobs.length > 0));
    setFavoriteJobsData(createJobListState());
    setAppliedJobsData(createJobListState());
    setResumeFile(null);
    setAuthView("login");
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

  function handleSidebarSwitch(sectionKey) {
    setActiveSection(sectionKey);
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

  function handleSaveUserProfileDraft() {
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), userProfileDraft);
    setMessage({ type: "success", text: "个人主页设置已保存，后续可直接在这里继续完善资料。" });
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
                  <article className="resume-table-row">
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
                        onClick={() => resumeUploadInputRef.current?.click()}
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

    return (
      <section className="user-home-shell">
        <article className="user-home-hero">
          <div>
            <span className="eyebrow">个人资料</span>
            <h1>把求职偏好、关键词和套餐放在一个地方维护，方便你持续迭代求职策略。</h1>
            <p>
              这里专门维护个人画像和会员能力，简历上传则单独放到简历中心处理。
            </p>
          </div>
          <div className="user-home-kpis">
            <article>
              <strong>{dashboardView.resumeScore}</strong>
              <span>当前简历评分</span>
            </article>
            <article>
              <strong>{dashboardView.profileCompletionRate}%</strong>
              <span>资料完善度</span>
            </article>
            <article>
              <strong>{dashboardView.planName}</strong>
              <span>当前套餐</span>
            </article>
          </div>
        </article>

        <div className="user-home-grid">
          <section className="user-home-card">
            <div className="rail-title">
              <strong>完善个人资料</strong>
              <button type="button" onClick={() => setActiveSection("resume")}>
                去简历中心
              </button>
            </div>

            <form className="user-home-form">
              <label>
                期望城市
                <input
                  value={userProfileDraft.expectedCity}
                  onChange={(event) => handleProfileDraftChange("expectedCity", event.target.value)}
                  placeholder="例如 上海 / 深圳"
                />
              </label>

              <label>
                目标岗位
                <input
                  value={userProfileDraft.targetRole}
                  onChange={(event) => handleProfileDraftChange("targetRole", event.target.value)}
                  placeholder="例如 后端开发 / Java 工程师"
                />
              </label>

              <label>
                岗位关键词
                <input
                  value={userProfileDraft.keywordTags}
                  onChange={(event) => handleProfileDraftChange("keywordTags", event.target.value)}
                  placeholder="例如 Java, Spring Boot, MySQL"
                />
              </label>

              <label>
                个人亮点摘要
                <textarea
                  value={userProfileDraft.personalSummary}
                  onChange={(event) =>
                    handleProfileDraftChange("personalSummary", event.target.value)
                  }
                  placeholder="写下你最想让招聘方快速看到的项目经历、技术栈或求职方向。"
                  rows={5}
                />
              </label>

              <div className="user-home-actions">
                <button className="primary-button" onClick={handleSaveUserProfileDraft} type="button">
                  保存资料
                </button>
              </div>
            </form>
          </section>

          <section className="user-home-card plan-workbench">
            <div className="rail-title">
              <strong>升级套餐</strong>
              <button type="button">权益说明</button>
            </div>

            <div className="current-plan-banner">
              <small>当前已启用</small>
              <strong>{dashboardView.planName}</strong>
              <span>你可以先在前端演示环境中切换套餐方案，验证交互和展示逻辑。</span>
            </div>

            <div className="plan-option-list">
              {planOptions.map((plan) => {
                const active = dashboardView.planName === plan.name;
                return (
                  <article key={plan.code} className={active ? "plan-option active" : "plan-option"}>
                    <div className="plan-option-head">
                      <div>
                        <strong>{plan.name}</strong>
                        <span>{plan.price}</span>
                      </div>
                      <button
                        className={active ? "ghost-button" : "primary-button"}
                        onClick={() => handleUpgradePlan(plan)}
                        type="button"
                      >
                        {active ? "当前套餐" : "切换到此套餐"}
                      </button>
                    </div>
                    <p>{plan.description}</p>
                    <ul>
                      {plan.features.map((feature) => (
                        <li key={feature}>{feature}</li>
                      ))}
                    </ul>
                  </article>
                );
              })}
            </div>
          </section>
        </div>

        <div className="user-home-grid">
          <section className="user-home-card">
            <div className="rail-title">
              <strong>求职提醒</strong>
              <button type="button" onClick={() => setActiveSection("jobs")}>
                返回职位流
              </button>
            </div>
            <div className="tips-grid">
              {dashboardView.tips.map((tip) => (
                <article key={tip} className="tip-card">
                  <strong>{tip}</strong>
                  <span>完成这一项后，你的主页完成度和岗位匹配解释会更完整。</span>
                </article>
              ))}
            </div>
          </section>
        </div>
      </section>
    );
  }

  if (!auth.token || !auth.user) {
    return (
      <div className="auth-shell">
        <div className="auth-backdrop auth-left" />
        <div className="auth-backdrop auth-right" />

        <main className="auth-layout">
          <section className="auth-hero">
            <span className="eyebrow">JobBright 求职工作台</span>
            <h1>登录后进入首页，围绕简历、职位和投递建立你的求职节奏。</h1>
            <p className="auth-copy">
              这是一个中文化的职位平台首页原型，风格参考 Jobright，但会更贴近中文求职场景。
              你可以在这里集中管理简历、筛选岗位、跟踪投递、查看匹配分析。
            </p>

            <div className="auth-feature-grid">
              <article>
                <strong>推荐职位</strong>
                <span>从首页直接进入推荐流，快速筛掉不合适的岗位。</span>
              </article>
              <article>
                <strong>简历中心</strong>
                <span>上传当前简历，持续优化匹配分数与投递效率。</span>
              </article>
              <article>
                <strong>求职助手</strong>
                <span>后续可以扩展成投递建议、问答辅助和个性化提醒。</span>
              </article>
            </div>
          </section>

          <section className="auth-panel">
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
                <h2>欢迎回来</h2>
                <p>登录后进入职位首页，查看推荐岗位与简历匹配信息。</p>

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

                <button className="primary-button" disabled={loading} type="submit">
                  {loading ? "登录中..." : "立即登录"}
                </button>

                <div className="helper-text">演示账号：demo / JobBacked123</div>
              </form>
            ) : (
              <form className="auth-form" onSubmit={handleRegister}>
                <h2>创建账号</h2>
                <p>先完成注册，后续你可以继续接简历上传、职位推荐和投递记录。</p>

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
                  <input
                    value={registerForm.email}
                    onChange={(event) => updateForm(setRegisterForm, "email", event.target.value)}
                    placeholder="请输入邮箱"
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

        <main className="auth-layout resume-gate-layout">
          <section className="auth-hero">
            <span className="eyebrow">先上传一份简历</span>
            <h1>登录成功后，需要先上传当前简历，才能进入职位首页。</h1>
            <p className="auth-copy">
              首页的匹配分数、推荐职位、筛选建议都会基于当前简历生成。建议先上传 PDF、
              DOC 或 DOCX 格式的最新简历，再进入首页开始筛选岗位。
            </p>

            <div className="auth-feature-grid">
              <article>
                <strong>匹配推荐</strong>
                <span>后续职位列表将围绕你的简历关键词和方向生成推荐结果。</span>
              </article>
              <article>
                <strong>简历中心</strong>
                <span>上传后可以持续替换当前版本，用最新简历保持推荐质量。</span>
              </article>
              <article>
                <strong>投递提效</strong>
                <span>简历会成为后续一键投递、简历分析和求职助手的基础输入。</span>
              </article>
            </div>
          </section>

          <section className="auth-panel resume-upload-panel">
            <h2>上传当前简历</h2>
            <p>支持 PDF、DOC、DOCX。上传成功后自动进入首页。</p>

            {message.text ? (
              <div className={message.type === "error" ? "notice error" : "notice success"}>
                {message.text}
              </div>
            ) : null}

            <form className="auth-form" onSubmit={handleResumeUpload}>
              <label className="file-upload-box">
                <span>选择简历文件</span>
                <input
                  type="file"
                  accept=".pdf,.doc,.docx"
                  onChange={(event) => setResumeFile(event.target.files?.[0] || null)}
                />
              </label>

              {resumeFile ? (
                <div className="selected-file-card">
                  <strong>{resumeFile.name}</strong>
                  <span>{Math.max(1, Math.round(resumeFile.size / 1024))} KB</span>
                </div>
              ) : (
                <div className="selected-file-card empty">
                  <strong>还没有选择文件</strong>
                  <span>请上传最新简历后继续。</span>
                </div>
              )}

              <button className="primary-button" disabled={loading} type="submit">
                {loading ? "上传中..." : "上传简历并进入首页"}
              </button>

              <button className="ghost-button" onClick={() => logout()} type="button">
                退出登录
              </button>
            </form>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="dashboard-shell">
      <div className="promo-bar">
        <span>本周重点机会仍在持续刷新。</span>
        <strong>优化简历和筛选条件，可以显著提升岗位匹配度。</strong>
      </div>

      <div className={activeSection === "resume" ? "dashboard-layout resume-layout" : "dashboard-layout"}>
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
              <article key={job.jobId} className="job-card">
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

                    <button className="job-card-menu" type="button" aria-label="更多操作">
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
                        onClick={() => handleToggleJobApplied(job)}
                        type="button"
                        aria-label={job.applied ? "取消投递" : "标记投递"}
                      >
                        <JobActionIcon type="skip" />
                      </button>
                      <button
                        className="job-action-icon-button"
                        onClick={() => handleToggleJobLike(job)}
                        type="button"
                        aria-label={job.liked ? "取消收藏" : "收藏"}
                      >
                        <JobActionIcon type="favorite" />
                      </button>
                      <button className="job-assistant-button" type="button">
                        <JobActionIcon type="spark" />
                        <span>问求职助手</span>
                      </button>
                      <button className="job-apply-button" type="button">
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
            <div className="load-more-anchor">
              {jobLoading ? "正在加载更多职位..." : hasMoreJobs ? "继续下滑加载更多" : "已经到底了"}
            </div>
          ) : null}
            </>
          ) : (
            renderUserWorkspaceSection()
          )}
        </main>

        {activeSection !== "resume" ? (
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
    </div>
  );
}

export default App;
