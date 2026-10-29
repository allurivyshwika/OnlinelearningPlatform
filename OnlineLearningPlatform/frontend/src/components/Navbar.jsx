export default function Navbar({
  user,
  view,
  authMode,
  busy,
  onNavigate,
  onLogin,
  onAdminLogin,
  onRegister,
  onLogout,
}) {
  const dashboardLabel = user?.role === "ADMIN"
    ? "Admin"
    : user?.role === "INSTRUCTOR"
      ? "Teaching"
      : "My learning";
  const roleLabel = user?.role === "ADMIN"
    ? "Administrator"
    : user?.role === "INSTRUCTOR"
      ? "Instructor"
      : "Student";

  return (
    <header className="topbar">
      <a
        className="brand"
        href="/"
        aria-label="LearnSpace home"
        onClick={(event) => {
          event.preventDefault();
          onNavigate("catalog");
        }}
      >
        <span className="brand-mark" aria-hidden="true">L</span>
        <span>learnspace</span>
      </a>
      <div className="topbar-actions">
        <nav className="main-navigation" aria-label="Main navigation">
          <button
            aria-current={view === "catalog" ? "page" : undefined}
            className={`nav-link${view === "catalog" ? " nav-link-active" : ""}`}
            onClick={() => onNavigate("catalog")}
            type="button"
          >
            Courses
          </button>
          {user && (
            <button
              aria-current={view === "dashboard" ? "page" : undefined}
              className={`nav-link${view === "dashboard" ? " nav-link-active" : ""}`}
              onClick={() => onNavigate("dashboard")}
              type="button"
            >
              {dashboardLabel}
            </button>
          )}
        </nav>
        {user ? (
          <div className="account-actions">
            <div className="account-chip" title={`${user.name} · ${roleLabel}`}>
              <span className="account-avatar" aria-hidden="true">
                {user.name?.trim().charAt(0).toUpperCase() || "U"}
              </span>
              <span className="account-details">
                <strong>{user.name}</strong>
                <span>{roleLabel}</span>
              </span>
            </div>
            <button
              className="nav-button nav-button-muted"
              disabled={busy}
              onClick={onLogout}
              type="button"
            >
              Sign out
            </button>
          </div>
        ) : authMode ? null : (
          <div className="account-actions">
            <button className="nav-link" onClick={onLogin} type="button">Sign in</button>
            <button className="nav-link admin-login-link" onClick={onAdminLogin} type="button">
              Admin sign in
            </button>
            <button className="nav-button" onClick={onRegister} type="button">
              Create account
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
