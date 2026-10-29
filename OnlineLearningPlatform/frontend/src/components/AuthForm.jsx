import { useState } from "react";

function PasswordField({ id, label, value, onChange, autoComplete, minLength, error }) {
  const [visible, setVisible] = useState(false);

  return (
    <label htmlFor={id}>
      {label}
      <span className="password-input-wrap">
        <input
          autoComplete={autoComplete}
          aria-invalid={error ? "true" : undefined}
          aria-describedby={error ? `${id}-error` : undefined}
          id={id}
          maxLength={72}
          minLength={minLength}
          onChange={onChange}
          required
          type={visible ? "text" : "password"}
          value={value}
        />
        <button
          aria-label={`${visible ? "Hide" : "Show"} ${label.toLowerCase()}`}
          aria-pressed={visible}
          className="password-visibility"
          onClick={() => setVisible((current) => !current)}
          type="button"
        >
          {visible ? "Hide" : "Show"}
        </button>
      </span>
    </label>
  );
}

export default function AuthForm({ mode, onModeChange, onSubmit, busy, error, message }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [validationError, setValidationError] = useState("");
  const passwordsDoNotMatch = mode === "register"
    && confirmPassword.length > 0
    && password !== confirmPassword;

  function submit(event) {
    event.preventDefault();
    if (mode === "register" && password !== confirmPassword) {
      setValidationError("Passwords do not match.");
      return;
    }
    setValidationError("");
    onSubmit({ name, email, password });
  }

  const isRegister = mode === "register";
  const isAdminLogin = mode === "admin";

  return (
    <div className={`auth-page${isRegister ? " auth-page-register" : ""}`}>
      <aside className={`auth-aside${isAdminLogin ? " auth-aside-admin" : ""}`}>
        <p className="eyebrow"><span className="eyebrow-line" />{isAdminLogin ? "PLATFORM MANAGEMENT" : "LEARNSPACE COMMUNITY"}</p>
        <h1>
          {isAdminLogin
            ? "Manage the learning community."
            : isRegister
              ? "Make room for what’s next."
              : "Your next chapter starts here."}
        </h1>
        <p>
          {isAdminLogin
            ? "Sign in with an administrator account to manage users, courses, and catalog settings."
            : isRegister
              ? "Create your free account to enroll in practical courses, track your progress, and keep building skills."
              : "Sign in to pick up where you left off, continue your courses, and keep your learning moving forward."}
        </p>
        <div className="auth-aside-note">
          <span aria-hidden="true">✳</span>
          <span>{isAdminLogin ? "Administrator access is assigned to authorized accounts." : "Practical learning, at your own pace."}</span>
        </div>
      </aside>
      <section className="auth-panel" aria-labelledby="auth-heading">
        <div className="auth-heading">
          <div>
            <p className="eyebrow">
              {isAdminLogin ? "ADMINISTRATOR ACCESS" : isRegister ? "GET STARTED" : "WELCOME BACK"}
            </p>
            <h2 id="auth-heading">
              {isAdminLogin ? "Admin sign in" : isRegister ? "Create your account" : "Sign in to learn"}
            </h2>
          </div>
          <button className="text-button" onClick={() => onModeChange(null)} type="button">
            Back to courses
          </button>
        </div>
        <form className="auth-form" onSubmit={submit}>
          {isRegister && (
            <label>
              Name
              <input
                autoComplete="name"
                maxLength={100}
                onChange={(event) => setName(event.target.value)}
                required
                value={name}
              />
            </label>
          )}
          <label>
            Email
            <input
              autoComplete="email"
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </label>
          <PasswordField
            id="account-password"
            label="Password"
            autoComplete={isRegister ? "new-password" : "current-password"}
            minLength={isRegister ? 8 : undefined}
            onChange={(event) => {
              setPassword(event.target.value);
              setValidationError("");
            }}
            value={password}
            error={passwordsDoNotMatch}
          />
          {isRegister && (
            <>
              <span className="field-hint">Use 8–72 characters.</span>
              <PasswordField
                id="confirm-password"
                label="Confirm password"
                autoComplete="new-password"
                minLength={8}
                onChange={(event) => {
                  setConfirmPassword(event.target.value);
                  setValidationError("");
                }}
                value={confirmPassword}
                error={passwordsDoNotMatch}
              />
              {passwordsDoNotMatch && (
                <span className="field-hint form-error" id="confirm-password-error" role="status">
                  Passwords do not match.
                </span>
              )}
            </>
          )}
          {validationError && <p className="form-message form-error" role="alert">{validationError}</p>}
          {error && <p className="form-message form-error" role="alert">{error}</p>}
          {message && <p className="form-message" role="status">{message}</p>}
          <button className="primary-button" disabled={busy} type="submit">
            {busy ? "Please wait…" : isRegister ? "Create account" : "Sign in"}
          </button>
        </form>
        <p className="auth-switch">
          {isAdminLogin ? (
            <>
              Need a learner account?{" "}
              <button className="text-button" onClick={() => onModeChange("login")} type="button">
                Regular sign in
              </button>
            </>
          ) : (
            <>
              {isRegister ? "Already have an account?" : "New to LearnSpace?"}{" "}
              <button
                className="text-button"
                onClick={() => onModeChange(isRegister ? "login" : "register")}
                type="button"
              >
                {isRegister ? "Sign in" : "Create an account"}
              </button>
              {!isRegister && (
                <>
                  {" · "}
                  <button className="text-button" onClick={() => onModeChange("admin")} type="button">
                    Admin sign in
                  </button>
                  {" · "}
                  <button className="text-button" onClick={() => onModeChange("forgot-password")} type="button">
                    Forgot password?
                  </button>
                </>
              )}
            </>
          )}
        </p>
      </section>
    </div>
  );
}
