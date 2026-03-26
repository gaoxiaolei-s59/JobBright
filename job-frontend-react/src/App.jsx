import { useEffect, useState } from "react";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") || "http://localhost:8080";
const TOKEN_KEY = "jobbright_access_token";

const initialLoginForm = {
  account: "demo",
  password: "JobBacked123"
};

const initialRegisterForm = {
  username: "",
  email: "",
  displayName: "",
  password: ""
};

function App() {
  const [activeTab, setActiveTab] = useState("login");
  const [loginForm, setLoginForm] = useState(initialLoginForm);
  const [registerForm, setRegisterForm] = useState(initialRegisterForm);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(false);
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    return {
      token,
      user: null
    };
  });

  useEffect(() => {
    if (auth.token) {
      loadCurrentUser(auth.token);
    }
  }, []);

  async function request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      }
    });

    const result = await response.json();
    if (!response.ok || !result.success) {
      throw new Error(result.message || "请求失败");
    }
    return result.data;
  }

  async function loadCurrentUser(token = auth.token) {
    if (!token) {
      return;
    }

    try {
      const user = await request("/api/auth/me", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setAuth((current) => ({ ...current, user }));
    } catch (error) {
      logout();
      setMessage({ type: "error", text: error.message });
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
      setAuth({ token: data.accessToken, user: data.user });
      setMessage({ type: "success", text: "登录成功，欢迎回来。" });
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
      const data = await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(registerForm)
      });
      localStorage.setItem(TOKEN_KEY, data.accessToken);
      setAuth({ token: data.accessToken, user: data.user });
      setRegisterForm(initialRegisterForm);
      setMessage({ type: "success", text: "注册成功，已自动进入平台。" });
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setAuth({ token: null, user: null });
    setActiveTab("login");
  }

  function updateForm(setter, field, value) {
    setter((current) => ({
      ...current,
      [field]: value
    }));
  }

  return (
    <div className="page-shell">
      <div className="ambient ambient-left" />
      <div className="ambient ambient-right" />

      <main className="layout">
        <section className="hero-card">
          <span className="eyebrow">JobBright Platform</span>
          <h1>把登录页做成真正能承接转化的求职平台首页入口。</h1>
          <p className="hero-copy">
            这套 React 前端已经对接当前 Spring Boot 登录接口，可直接继续扩展成职位搜索、
            候选人中心、企业端控制台。视觉方向参考现代招聘平台官网，强调品牌、速度和信任感。
          </p>

          <div className="feature-grid">
            <article className="feature-card">
              <strong>React + Vite</strong>
              <span>轻量前端工程，适合快速迭代官网和业务页。</span>
            </article>
            <article className="feature-card">
              <strong>JWT Auth</strong>
              <span>已接通登录、注册、当前用户接口。</span>
            </article>
            <article className="feature-card">
              <strong>Docs Ready</strong>
              <span>前后端说明文档已补齐，方便继续开发。</span>
            </article>
          </div>
        </section>

        <section className="panel-card">
          {auth.user ? (
            <div className="dashboard">
              <div className="dashboard-header">
                <div>
                  <span className="eyebrow">Authenticated</span>
                  <h2>{auth.user.displayName}</h2>
                  <p>你已经完成登录，当前可以继续扩展职位推荐、简历投递和个人工作台。</p>
                </div>
                <button className="secondary-button" onClick={logout}>
                  退出登录
                </button>
              </div>

              <div className="profile-grid">
                <div className="profile-card">
                  <span>用户名</span>
                  <strong>{auth.user.username}</strong>
                </div>
                <div className="profile-card">
                  <span>邮箱</span>
                  <strong>{auth.user.email}</strong>
                </div>
                <div className="profile-card">
                  <span>角色</span>
                  <strong>{auth.user.role}</strong>
                </div>
                <div className="profile-card">
                  <span>接口地址</span>
                  <strong>/api/auth/me</strong>
                </div>
              </div>

              <div className="tips-card">
                <strong>下一步建议</strong>
                <p>
                  继续补职位列表页、搜索筛选、公司详情页和投递流程，就能把这个登录壳扩展成完整的职位平台前台。
                </p>
              </div>
            </div>
          ) : (
            <>
              <div className="tab-row">
                <button
                  className={activeTab === "login" ? "tab active" : "tab"}
                  onClick={() => setActiveTab("login")}
                >
                  登录
                </button>
                <button
                  className={activeTab === "register" ? "tab active" : "tab"}
                  onClick={() => setActiveTab("register")}
                >
                  注册
                </button>
              </div>

              {message.text ? (
                <div className={message.type === "error" ? "notice error" : "notice success"}>
                  {message.text}
                </div>
              ) : null}

              {activeTab === "login" ? (
                <form className="auth-form" onSubmit={handleLogin}>
                  <h2>欢迎回来</h2>
                  <p>使用演示账号或你刚注册的账户直接登录。</p>

                  <label>
                    用户名或邮箱
                    <input
                      value={loginForm.account}
                      onChange={(event) => updateForm(setLoginForm, "account", event.target.value)}
                      placeholder="demo 或 demo@jobbacked.com"
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

                  <button className="primary-button" type="submit" disabled={loading}>
                    {loading ? "登录中..." : "进入平台"}
                  </button>

                  <div className="helper-text">演示账号：demo / JobBacked123</div>
                </form>
              ) : (
                <form className="auth-form" onSubmit={handleRegister}>
                  <h2>创建账号</h2>
                  <p>先完成一个简单的注册闭环，后面可以继续补邮箱验证和企业身份。</p>

                  <label>
                    用户名
                    <input
                      value={registerForm.username}
                      onChange={(event) =>
                        updateForm(setRegisterForm, "username", event.target.value)
                      }
                      placeholder="3-24 位用户名"
                    />
                  </label>

                  <label>
                    邮箱
                    <input
                      type="email"
                      value={registerForm.email}
                      onChange={(event) =>
                        updateForm(setRegisterForm, "email", event.target.value)
                      }
                      placeholder="you@example.com"
                    />
                  </label>

                  <label>
                    昵称
                    <input
                      value={registerForm.displayName}
                      onChange={(event) =>
                        updateForm(setRegisterForm, "displayName", event.target.value)
                      }
                      placeholder="展示给平台的名称"
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
                      placeholder="至少 6 位"
                    />
                  </label>

                  <button className="primary-button" type="submit" disabled={loading}>
                    {loading ? "注册中..." : "创建账户"}
                  </button>
                </form>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
