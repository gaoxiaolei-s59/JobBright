import { useMemo, useState } from "react";

const sidebarItems = [
  { key: "jobs", label: "Jobs", badge: null, active: true },
  { key: "resume", label: "Resume", badge: null, active: false },
  { key: "profile", label: "Profile", badge: null, active: false },
  { key: "agent", label: "Agent", badge: null, active: false },
  { key: "coaching", label: "Coaching", badge: "NEW", active: false }
];

const filters = [
  "United States",
  "Backend Engineer",
  "Intern/New Grad",
  "Full-time +1",
  "Onsite +2",
  "Date Posted",
  "Years of Experience",
  "Industry",
  "Hidden Jobs",
  "All Filters"
];

const savedFilters = [
  "Backend Engineer, US",
  "New Grad, Remote Friendly",
  "AI Platform, Full-time"
];

const jobItems = [
  {
    id: "job-1",
    company: "Samsung",
    title: "Software Engineer, Backend Internship",
    meta: "Samsung Electronics America / Manufacturing / Late Stage",
    postedAt: "6 hours ago",
    location: "Mountain View, CA",
    workMode: "Onsite",
    employmentType: "Internship",
    level: "Intern",
    applicants: 54,
    match: 82,
    sponsor: "H1B Sponsor Likely",
    cta: "Apply with Autofill",
    brand: "SAMSUNG",
    theme: "dark"
  },
  {
    id: "job-2",
    company: "Informatica",
    title: "Software Engineering AMTS (College Grad)",
    meta: "Informatica / Big Data / Cloud Computing / Public Company",
    postedAt: "10 hours ago",
    location: "CA",
    workMode: "Onsite",
    employmentType: "Full-time",
    level: "New Grad, Entry Level",
    applicants: 72,
    match: 98,
    sponsor: "H1B Sponsor Likely",
    cta: "Apply Now",
    brand: "I",
    theme: "orange"
  },
  {
    id: "job-3",
    company: "Salesforce",
    title: "Software Engineering AMTS (College Grad)",
    meta: "Salesforce / AI Cloud / Enterprise SaaS / Public Company",
    postedAt: "Reposted 14 hours ago",
    location: "San Francisco, CA",
    workMode: "Hybrid",
    employmentType: "Full-time",
    level: "Early Career",
    applicants: 108,
    match: 93,
    sponsor: "Strong alumni network",
    cta: "Open Role",
    brand: "salesforce",
    theme: "blue"
  }
];

function App() {
  const [query, setQuery] = useState("");
  const [activeTab, setActiveTab] = useState("Recommended");

  const visibleJobs = useMemo(() => {
    const keyword = query.trim().toLowerCase();
    if (!keyword) {
      return jobItems;
    }
    return jobItems.filter((item) =>
      [item.title, item.company, item.meta].some((text) => text.toLowerCase().includes(keyword))
    );
  }, [query]);

  return (
    <div className="dashboard-shell">
      <div className="promo-bar">
        <span>Offer window closes in 59m 38s.</span>
        <strong>Last chance to unlock more matched jobs this week.</strong>
      </div>

      <div className="dashboard-layout">
        <aside className="sidebar">
          <div className="brand-block">
            <div className="brand-mark">J</div>
            <div>
              <strong>JobBright</strong>
              <span>Career OS</span>
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
            <small>Resume Score</small>
            <strong>82 / 100</strong>
            <p>Upload your latest resume and unlock a better match score for backend roles.</p>
            <button className="ghost-button" type="button">
              Improve Resume
            </button>
          </div>

          <div className="sidebar-footer">
            <button type="button">Messages</button>
            <button type="button">Feedback</button>
            <button type="button">Settings</button>
          </div>
        </aside>

        <main className="main-panel">
          <header className="topbar">
            <div className="topbar-title">
              <span className="section-label">Jobs</span>
              <div className="tab-strip">
                {["Recommended", "Liked", "Applied", "External"].map((tab) => (
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
                <span>Search</span>
                <input
                  placeholder="Search by title or company"
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                />
              </label>
              <button className="turbo-button" type="button">
                Turbo for Students
              </button>
            </div>
          </header>

          <section className="hero-panel">
            <div>
              <span className="eyebrow">Career Command Center</span>
              <h1>Track the best-fit jobs, sharpen your resume, and move faster on every application.</h1>
              <p>
                This homepage is designed as a Jobright-inspired operating system for job seekers:
                recommendation-first, filter-heavy, and centered on match quality instead of plain lists.
              </p>
            </div>
            <div className="hero-stats">
              <article>
                <strong>1,286</strong>
                <span>Fresh backend roles this week</span>
              </article>
              <article>
                <strong>82%</strong>
                <span>Average match after resume optimization</span>
              </article>
              <article>
                <strong>9 min</strong>
                <span>Typical time to shortlist a target set</span>
              </article>
            </div>
          </section>

          <section className="filter-row">
            {filters.map((filter) => (
              <button key={filter} className={filter === "All Filters" ? "filter-pill accent" : "filter-pill"} type="button">
                {filter}
              </button>
            ))}
          </section>

          <section className="job-feed">
            {visibleJobs.map((job) => (
              <article key={job.id} className="job-card">
                <div className="job-card-main">
                  <div className={job.theme ? `company-badge ${job.theme}` : "company-badge"}>
                    {job.brand}
                  </div>

                  <div className="job-copy">
                    <span className="posted-chip">{job.postedAt}</span>
                    <h2>{job.title}</h2>
                    <p className="job-meta">{job.meta}</p>

                    <div className="job-tags">
                      <span>{job.location}</span>
                      <span>{job.workMode}</span>
                      <span>{job.employmentType}</span>
                      <span>{job.level}</span>
                    </div>

                    <div className="job-actions">
                      <button className="icon-button" type="button">
                        Skip
                      </button>
                      <button className="icon-button" type="button">
                        Save
                      </button>
                      <button className="secondary-action" type="button">
                        Ask Copilot
                      </button>
                      <button className="primary-action" type="button">
                        {job.cta}
                      </button>
                    </div>

                    <small>{job.applicants} applicants</small>
                  </div>
                </div>

                <aside className="job-match-panel">
                  <div className="match-ring">
                    <span>{job.match}%</span>
                  </div>
                  <strong>{job.match >= 95 ? "Strong Match" : "Good Match"}</strong>
                  <p>{job.sponsor}</p>
                </aside>
              </article>
            ))}
          </section>
        </main>

        <aside className="right-rail">
          <section className="profile-panel">
            <div className="profile-header">
              <div className="avatar">Y</div>
              <div>
                <strong>Your Workspace</strong>
                <span>Free Plan</span>
              </div>
            </div>
          </section>

          <section className="rail-card">
            <div className="rail-title">
              <strong>Saved Filters</strong>
              <button type="button">+</button>
            </div>
            <div className="saved-filter-list">
              {savedFilters.map((item) => (
                <div key={item} className="saved-filter-item">
                  <span>{item}</span>
                  <button type="button">Edit</button>
                </div>
              ))}
            </div>
          </section>

          <section className="rail-card progress-card">
            <strong>Complete your profile to unlock more high-match jobs</strong>
            <div className="progress-bar">
              <div style={{ width: "42%" }} />
            </div>
            <ul>
              <li>Upload your resume</li>
              <li>Set location and visa preference</li>
              <li>Add preferred role filters</li>
            </ul>
          </section>

          <section className="rail-card insight-card">
            <small>Weekly Insight</small>
            <strong>Backend internships with onsite preference are converting better this week.</strong>
            <p>Try narrowing your filters to California, New Grad, and Distributed Systems.</p>
          </section>
        </aside>
      </div>
    </div>
  );
}

export default App;
