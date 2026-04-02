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

const homeOverviewFallback = {
  freshJobCount: 1286,
  averageMatchRate: 82,
  averageShortlistMinutes: 9,
  resumeScore: 82,
  profileCompletionRate: 42
};

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
  experienceLevel: "",
  employmentType: "",
  workMode: "",
  datePosted: "",
  industryName: "",
  page: 1,
  pageSize: 10
};

const experienceLevelOptions = [
  { value: "STUDENT", label: "在校生" },
  { value: "NEW_GRAD", label: "应届生" },
  { value: "JUNIOR", label: "初级岗位" }
];

const experienceLevelLabelMap = Object.fromEntries(
  experienceLevelOptions.map((item) => [item.value, item.label])
);

const recommendRequestFieldNames = [
  "keyword",
  "country",
  "title",
  "experienceLevel",
  "employmentType",
  "workMode",
  "datePosted",
  "industryName",
  "page",
  "pageSize"
];

const jobItems = [
  {
    id: "job-1",
    company: "三星",
    companyLogo: "",
    title: "后端开发实习生",
    meta: "三星电子 / 制造业 / 成长期",
    postedAt: "6 小时前",
    location: "上海",
    workMode: "线下办公",
    employmentType: "实习",
    experienceLevel: "STUDENT",
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
    workMode: "混合办公",
    employmentType: "全职",
    experienceLevel: "NEW_GRAD",
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
    workMode: "混合办公",
    employmentType: "全职",
    experienceLevel: "JUNIOR",
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
  location: job.location,
  workMode: job.workMode,
  employmentType: job.employmentType,
  experienceLevel: job.experienceLevel,
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

function getCompanyBadgeText(job) {
  if (job.brand) {
    return job.brand;
  }
  return (job.companyName || "U").slice(0, 2).toUpperCase();
}

function getExperienceLevelLabel(level) {
  return experienceLevelLabelMap[level] || level || "";
}

function getAuthDisplayName(user) {
  return user?.displayName || user?.username || "U";
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
  const [authView, setAuthView] = useState("login");
  const [loginForm, setLoginForm] = useState(loginInitialState);
  const [registerForm, setRegisterForm] = useState(registerInitialState);
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeInfo, setResumeInfo] = useState(null);
  const [homeOverview, setHomeOverview] = useState(homeOverviewFallback);
  const [userDashboard, setUserDashboard] = useState(userDashboardFallback);
  const [userProfileDraft, setUserProfileDraft] = useState(userProfileDraftInitialState);
  const [jobFilters, setJobFilters] = useState(jobFilterInitialState);
  const [jobsData, setJobsData] = useState({
    total: mockRecommendedJobs.length,
    hasMore: mockRecommendedJobs.length > 0,
    records: []
  });
  const [jobLoading, setJobLoading] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(false);
  const loadMoreRef = useRef(null);
  const [auth, setAuth] = useState(() => ({
    token: localStorage.getItem(TOKEN_KEY),
    user: null
  }));
  const hasMoreJobs = Boolean(jobsData.hasMore);
  const dashboardView = buildUserDashboardView(
    userDashboard,
    userProfileDraft,
    resumeInfo,
    auth.user
  );
  const isUserHomeSection = activeSection !== "jobs";

  useEffect(() => {
    if (auth.token) {
      loadCurrentUser();
    }
  }, []);

  useEffect(() => {
    if (!auth.token || !resumeInfo || !loadMoreRef.current || jobLoading || !hasMoreJobs) {
      return undefined;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry?.isIntersecting) {
          const nextFilters = {
            ...jobFilters,
            page: Number(jobFilters.page || 1) + 1
          };
          setJobFilters(nextFilters);
          loadRecommendedJobs(nextFilters, true);
        }
      },
      { rootMargin: "240px 0px" }
    );
    observer.observe(loadMoreRef.current);
    return () => observer.disconnect();
  }, [auth.token, resumeInfo, jobLoading, hasMoreJobs, jobFilters]);

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
      await loadUserDashboard(user);
      await loadCurrentResume();
      await loadHomeOverview();
      await loadRecommendedJobs(jobFilterInitialState);
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

  async function loadHomeOverview() {
    try {
      const overview = await request("/api/home/overview", { method: "GET" });
      setHomeOverview({ ...homeOverviewFallback, ...overview });
    } catch (error) {
      setHomeOverview(homeOverviewFallback);
      if (!resumeInfo) {
        return;
      }
      setMessage({ type: "error", text: `${error.message || "首页概览加载失败"}，已回退默认展示` });
    }
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
        [job.title, job.companyName, job.meta, job.location].some((text) =>
          String(text).toLowerCase().includes(normalizedKeyword)
        );
      const countryMatched = !filters.country || filters.country === "中国大陆";
      const titleMatched =
        !normalizedTitle || job.title.toLowerCase().includes(normalizedTitle);
      const experienceMatched =
        !filters.experienceLevel || job.experienceLevel === filters.experienceLevel;
      const employmentTypeMatched =
        !filters.employmentType || job.employmentType === filters.employmentType;
      const workModeMatched = !filters.workMode || job.workMode === filters.workMode;
      const industryNameMatched =
        !filters.industryName || job.meta.includes(filters.industryName);
      return (
        keywordMatched &&
        countryMatched &&
        titleMatched &&
        experienceMatched &&
        employmentTypeMatched &&
        workModeMatched &&
        industryNameMatched
      );
    });

    const page = Number(filters.page) || 1;
    const pageSize = Number(filters.pageSize) || jobFilterInitialState.pageSize;
    const fromIndex = Math.max((page - 1) * pageSize, 0);
    const toIndex = fromIndex + pageSize;
    return {
      total: filtered.length,
      hasMore: toIndex < filtered.length,
      records: filtered.slice(fromIndex, toIndex)
    };
  }

  async function loadRecommendedJobs(nextFilters = jobFilters, append = false) {
    setJobLoading(true);
    try {
      const queryString = buildRecommendQueryString(nextFilters);
      const data = await request(`/api/jobs/recommended?${queryString}`, { method: "GET" });
      const nextRecords = Array.isArray(data?.records) ? data.records : [];
      setJobsData((current) => ({
        total: data?.total ?? 0,
        hasMore: Boolean(data?.hasMore),
        records: append ? [...current.records, ...nextRecords] : nextRecords
      }));
    } catch (error) {
      const fallback = filterMockJobs(nextFilters);
      setJobsData((current) => ({
        total: fallback.total,
        hasMore: fallback.hasMore,
        records: append ? [...current.records, ...fallback.records] : fallback.records
      }));
      setMessage({
        type: "error",
        text: `${error.message || "推荐职位加载失败"}，当前已切换到本地演示数据`
      });
    } finally {
      setJobLoading(false);
    }
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
    setResumeInfo(null);
    setHomeOverview(homeOverviewFallback);
    setUserDashboard(userDashboardFallback);
    setUserProfileDraft(userProfileDraftInitialState);
    setJobFilters(jobFilterInitialState);
    setJobsData({
      total: mockRecommendedJobs.length,
      hasMore: mockRecommendedJobs.length > 0,
      records: []
    });
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
    setJobFilters((current) => ({ ...current, [field]: value, page: 1 }));
  }

  async function handleJobSearch(event) {
    event.preventDefault();
    await loadRecommendedJobs(jobFilters);
  }

  async function handleResetFilters() {
    setJobFilters(jobFilterInitialState);
    await loadRecommendedJobs(jobFilterInitialState);
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
      await loadHomeOverview();
      await loadRecommendedJobs(jobFilterInitialState);
      setResumeFile(null);
      setMessage({ type: "success", text: "简历上传成功，已进入首页。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function handleSidebarSwitch(sectionKey) {
    setActiveSection(sectionKey);
  }

  function handleProfileDraftChange(field, value) {
    setUserProfileDraft((current) => ({ ...current, [field]: value }));
  }

  function handleSaveUserProfileDraft() {
    writeLocalJson(getUserScopedStorageKey(auth.user, "profile_draft"), userProfileDraft);
    setMessage({ type: "success", text: "个人主页设置已保存，后续可直接在这里继续完善资料。" });
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
          <article className="user-home-hero">
            <div>
              <span className="eyebrow">简历中心</span>
              <h1>把当前简历版本、评分和上传动作集中管理，保持推荐结果始终基于最新材料。</h1>
              <p>
                这里专门处理简历本身，不再混入个人偏好字段，方便你持续替换和优化版本。
              </p>
            </div>
            <div className="user-home-kpis">
              <article>
                <strong>{dashboardView.resumeScore}</strong>
                <span>当前简历评分</span>
              </article>
              <article>
                <strong>{resumeInfo?.status || "ACTIVE"}</strong>
                <span>当前简历状态</span>
              </article>
              <article>
                <strong>{resumeInfo?.fileName || "未上传"}</strong>
                <span>当前生效版本</span>
              </article>
            </div>
          </article>

          <div className="user-home-grid user-home-grid-single">
            <section className="user-home-card resume-workbench">
              <div className="rail-title">
                <strong>上传与替换简历</strong>
                <button type="button" onClick={() => setActiveSection("profile")}>
                  去个人资料
                </button>
              </div>

              <div className="resume-current-card">
                <strong>{resumeInfo?.fileName || "当前还没有简历"}</strong>
                <span>
                  {resumeInfo
                    ? `评分 ${resumeInfo.score || dashboardView.resumeScore} · 状态 ${resumeInfo.status || "ACTIVE"}`
                    : "上传当前简历后，这里会展示最新版本和分数。"}
                </span>
              </div>

              <form className="user-home-form" onSubmit={handleResumeUpload}>
                <label className="file-upload-box compact">
                  <span>选择新的简历文件</span>
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
                    <strong>还没有选择新文件</strong>
                    <span>支持 PDF、DOC、DOCX，上传后会替换当前生效简历。</span>
                  </div>
                )}

                <div className="tips-grid">
                  <article className="tip-card">
                    <strong>建议保持一份最新校招版本</strong>
                    <span>针对目标岗位更新关键词和项目描述，再上传替换当前版本。</span>
                  </article>
                  <article className="tip-card">
                    <strong>上传后会刷新匹配结果</strong>
                    <span>岗位推荐、匹配分和工作台提示会根据当前简历重新计算。</span>
                  </article>
                </div>

                <div className="user-home-actions">
                  <button className="primary-button" disabled={loading} type="submit">
                    {loading ? "上传中..." : "上传并刷新简历"}
                  </button>
                </div>
              </form>
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

      <div className="dashboard-layout">
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

          <div className="sidebar-card">
            <small>当前简历评分</small>
            <strong>{dashboardView.resumeScore} / 100</strong>
            <p>上传最新简历并补全偏好设置后，可以获得更高质量的岗位推荐。</p>
            <button className="ghost-button" onClick={() => setActiveSection("resume")} type="button">
              优化简历
            </button>
          </div>

          <div className="sidebar-footer">
            <button type="button">消息中心</button>
            <button type="button">意见反馈</button>
            <button type="button">系统设置</button>
          </div>
        </aside>

        <main className="main-panel">
          {!isUserHomeSection ? (
            <header className="topbar">
              <div className="topbar-title">
                <span className="section-label">职位</span>
                <div className="tab-strip">
                  {["推荐职位", "收藏职位", "已投递", "站外岗位"].map((tab) => (
                    <button
                      key={tab}
                      className={activeTab === tab ? "top-tab active" : "top-tab"}
                      onClick={() => setActiveTab(tab)}
                      type="button"
                    >
                      {tab}
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
          <section className="hero-panel">
            <div>
              <span className="eyebrow">求职首页</span>
              <h1>集中管理简历、职位推荐和投递动作，让求职节奏更清晰。</h1>
              <p>
                首页采用推荐优先的职位流设计，适合快速筛选目标岗位、对照简历匹配度、
                并把高价值职位沉淀到后续投递流程里。
              </p>
            </div>
            <div className="hero-stats">
              <article>
                <strong>{homeOverview.freshJobCount.toLocaleString("zh-CN")}</strong>
                <span>本周新增的后端相关岗位</span>
              </article>
              <article>
                <strong>{homeOverview.averageMatchRate}%</strong>
                <span>当前简历平均岗位匹配度</span>
              </article>
              <article>
                <strong>{homeOverview.averageShortlistMinutes} 分钟</strong>
                <span>完成首轮职位筛选的典型用时</span>
              </article>
            </div>
          </section>

          <section className="filter-panel">
            <form className="filter-grid" onSubmit={handleJobSearch}>
              <label>
                国家
                <select
                  value={jobFilters.country}
                  onChange={(event) => updateJobFilter("country", event.target.value)}
                >
                  <option value="">全部国家</option>
                  <option value="中国大陆">中国大陆</option>
                </select>
              </label>
              <label>
                职位名称
                <input
                  value={jobFilters.title}
                  onChange={(event) => updateJobFilter("title", event.target.value)}
                  placeholder="如 后端开发"
                />
              </label>
              <label>
                经验等级
                <select
                  value={jobFilters.experienceLevel}
                  onChange={(event) => updateJobFilter("experienceLevel", event.target.value)}
                >
                  <option value="">全部等级</option>
                  {experienceLevelOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                工作类型
                <select
                  value={jobFilters.employmentType}
                  onChange={(event) => updateJobFilter("employmentType", event.target.value)}
                >
                  <option value="">全部类型</option>
                  <option value="全职">全职</option>
                  <option value="实习">实习</option>
                </select>
              </label>
              <label>
                工作模式
                <select
                  value={jobFilters.workMode}
                  onChange={(event) => updateJobFilter("workMode", event.target.value)}
                >
                  <option value="">全部模式</option>
                  <option value="线下办公">线下办公</option>
                  <option value="混合办公">混合办公</option>
                  <option value="远程">远程</option>
                </select>
              </label>
              <label>
                发布时间
                <select
                  value={jobFilters.datePosted}
                  onChange={(event) => updateJobFilter("datePosted", event.target.value)}
                >
                  <option value="">全部时间</option>
                  <option value="24h">24 小时内</option>
                  <option value="3d">3 天内</option>
                  <option value="7d">7 天内</option>
                </select>
              </label>
              <label>
                行业方向
                <select
                  value={jobFilters.industryName}
                  onChange={(event) => updateJobFilter("industryName", event.target.value)}
                >
                  <option value="">全部行业</option>
                  <option value="互联网">互联网</option>
                  <option value="制造业">制造业</option>
                  <option value="企业 SaaS">企业 SaaS</option>
                  <option value="云计算">云计算</option>
                </select>
              </label>
              <label>
                每次加载
                <select
                  value={jobFilters.pageSize}
                  onChange={(event) => updateJobFilter("pageSize", Number(event.target.value))}
                >
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                </select>
              </label>

              <div className="filter-actions">
                <button className="ghost-button" onClick={handleResetFilters} type="button">
                  重置筛选
                </button>
                <button className="primary-button" disabled={jobLoading} type="submit">
                  {jobLoading ? "筛选中..." : "应用筛选"}
                </button>
              </div>
            </form>
          </section>

          <section className="results-bar">
            <strong>推荐职位</strong>
            <span>
              已加载 {jobsData.records.length} / {jobsData.total} 条
            </span>
          </section>

          <section className="job-feed">
            {jobsData.records.length ? jobsData.records.map((job) => (
              <article key={job.jobId} className="job-card">
                <div className="job-card-main">
                  <CompanyBadge job={job} />

                  <div className="job-copy">
                    <span className="posted-chip">{job.postedAt}</span>
                    <h2>{job.title}</h2>
                    <p className="job-meta">{job.meta}</p>

                    <div className="job-tags">
                      <span>{job.location}</span>
                      <span>{job.workMode}</span>
                      <span>{job.employmentType}</span>
                      <span>{getExperienceLevelLabel(job.experienceLevel)}</span>
                    </div>

                    <div className="job-actions">
                      <button className="icon-button" type="button">
                        {job.applied ? "已投递" : "跳过"}
                      </button>
                      <button className="icon-button" type="button">
                        {job.liked ? "已收藏" : "收藏"}
                      </button>
                      <button className="secondary-action" type="button">
                        问求职助手
                      </button>
                      <button className="primary-action" type="button">
                        {job.applyUrl ? "立即申请" : "查看岗位"}
                      </button>
                    </div>

                    <small>{job.applicantCount} 人已投递</small>
                  </div>
                </div>

                <aside className="job-match-panel">
                  <div className="match-ring">
                    <span>{job.matchScore}%</span>
                  </div>
                  <strong>{job.matchLabel}</strong>
                  <p>{job.matchReason}</p>
                </aside>
              </article>
            )) : (
              <div className="empty-state-card">
                <strong>没有找到符合条件的职位</strong>
                <span>换一个关键词，或者放宽筛选条件后再试一次。</span>
              </div>
            )}
          </section>

          {jobsData.records.length ? (
            <div className="load-more-anchor" ref={loadMoreRef}>
              {jobLoading ? "正在加载更多职位..." : hasMoreJobs ? "继续下滑加载更多" : "已经到底了"}
            </div>
          ) : null}
            </>
          ) : (
            renderUserWorkspaceSection()
          )}
        </main>

        <aside className="right-rail">
          <section className="profile-panel">
            <div className="profile-header">
              <div className="avatar">
                {getAuthDisplayName(auth.user).slice(0, 1)}
              </div>
              <div className="profile-copy">
                <strong>{dashboardView.displayName}</strong>
                <span>{dashboardView.planName}</span>
              </div>
            </div>
            <div className="profile-actions">
              <button
                className="ghost-button"
                onClick={() => setActiveSection("profile")}
                type="button"
              >
                用户主页
              </button>
              <button className="ghost-button rail-logout" onClick={() => logout()} type="button">
                退出登录
              </button>
            </div>
          </section>

          <section className="rail-card">
            <div className="rail-title">
              <strong>已保存筛选</strong>
              <button type="button">+</button>
            </div>
            <div className="saved-filter-list">
              {savedFilters.map((item) => (
                <div key={item} className="saved-filter-item">
                  <span className="saved-filter-name">{item}</span>
                  <button type="button">编辑</button>
                </div>
              ))}
            </div>
          </section>

          <section className="rail-card progress-card">
            <strong>完善你的求职资料以获得更高匹配职位</strong>
            <div className="progress-bar">
              <div style={{ width: `${dashboardView.profileCompletionRate}%` }} />
            </div>
            <ul>
              {dashboardView.tips.map((tip) => (
                <li key={tip}>{tip}</li>
              ))}
            </ul>
          </section>

          <section className="rail-card insight-card">
            <small>本周建议</small>
            <strong>后端校招岗位在一线城市的匹配推荐更活跃，建议先补全城市偏好和简历关键词。</strong>
            <p>后续这里可以接入简历评分、职位趋势和个性化推荐解释。</p>
          </section>
        </aside>
      </div>
    </div>
  );
}

export default App;
