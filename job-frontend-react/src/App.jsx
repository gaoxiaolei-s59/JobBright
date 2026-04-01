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
  { key: "jobs", label: "职位推荐", badge: null, active: true },
  { key: "resume", label: "简历中心", badge: null, active: false },
  { key: "profile", label: "个人资料", badge: null, active: false },
  { key: "agent", label: "求职助手", badge: null, active: false },
  { key: "coach", label: "求职辅导", badge: "新", active: false }
];

const savedFilters = ["后端开发 · 上海", "校招岗位 · Java / Go", "AI 平台 · 全职"];

const homeOverviewFallback = {
  freshJobCount: 1286,
  averageMatchRate: 82,
  averageShortlistMinutes: 9,
  resumeScore: 82,
  profileCompletionRate: 42
};

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
  const [authView, setAuthView] = useState("login");
  const [loginForm, setLoginForm] = useState(loginInitialState);
  const [registerForm, setRegisterForm] = useState(registerInitialState);
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeInfo, setResumeInfo] = useState(null);
  const [homeOverview, setHomeOverview] = useState(homeOverviewFallback);
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
    setResumeInfo(null);
    setHomeOverview(homeOverviewFallback);
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
                className={item.active ? "sidebar-link active" : "sidebar-link"}
                type="button"
              >
                <span>{item.label}</span>
                {item.badge ? <em>{item.badge}</em> : null}
              </button>
            ))}
          </nav>

          <div className="sidebar-card">
            <small>当前简历评分</small>
            <strong>{homeOverview.resumeScore} / 100</strong>
            <p>上传最新简历并补全偏好设置后，可以获得更高质量的岗位推荐。</p>
            <button className="ghost-button" type="button">
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

          {message.text ? (
            <div className={message.type === "error" ? "notice error" : "notice success"}>
              {message.text}
            </div>
          ) : null}

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
        </main>

        <aside className="right-rail">
          <section className="profile-panel">
            <div className="profile-header">
              <div className="avatar">
                {getAuthDisplayName(auth.user).slice(0, 1)}
              </div>
              <div>
                <strong>{getAuthDisplayName(auth.user)}</strong>
                <span>免费版</span>
              </div>
            </div>
            <button className="ghost-button rail-logout" onClick={() => logout()} type="button">
              退出登录
            </button>
          </section>

          <section className="rail-card">
            <div className="rail-title">
              <strong>已保存筛选</strong>
              <button type="button">+</button>
            </div>
            <div className="saved-filter-list">
              {savedFilters.map((item) => (
                <div key={item} className="saved-filter-item">
                  <span>{item}</span>
                  <button type="button">编辑</button>
                </div>
              ))}
            </div>
          </section>

          <section className="rail-card progress-card">
            <strong>完善你的求职资料以获得更高匹配职位</strong>
            <div className="progress-bar">
              <div style={{ width: `${homeOverview.profileCompletionRate}%` }} />
            </div>
            <ul>
              <li>上传最新简历</li>
              <li>设置期望城市和岗位方向</li>
              <li>补充工作偏好与经历标签</li>
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
