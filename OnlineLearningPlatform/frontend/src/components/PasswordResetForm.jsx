import { useState } from "react";
import { request } from "../api.js";

export default function PasswordResetForm({ mode, token, onModeChange }) {
  const isReset = mode === "reset-password";
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const passwordsDoNotMatch = confirmPassword.length > 0 && password !== confirmPassword;
  const missingResetToken = isReset && !token;

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    if (isReset && password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setBusy(true);
    try {
      if (isReset) {
        await request("/api/auth/password/reset", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ token, password }),
        });
        setMessage("Your password has been reset. You can now sign in with your new password.");
      } else {
        const response = await request("/api/auth/password/forgot", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email }),
        });
        setMessage(response.message);
      }
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-page password-reset-page">
      <aside className="auth-aside">
        <p className="eyebrow"><span className="eyebrow-line" />ACCOUNT SECURITY</p>
        <h1>{isReset ? "Choose a new password." : "Get back to learning."}</h1>
        <p>
          {isReset
            ? "Set a new password for your LearnSpace account. The reset link can only be used once."
            : "Enter the email address on your account and we’ll send you a secure password reset link."}
        </p>
        <div className="auth-aside-note">
          <span aria-hidden="true">✳</span>
          <span>Reset links expire after 30 minutes.</span>
        </div>
      </aside>
      <section className="auth-panel" aria-labelledby="password-reset-heading">
        <div className="auth-heading">
          <div>
            <p className="eyebrow">{isReset ? "RESET PASSWORD" : "ACCOUNT RECOVERY"}</p>
            <h2 id="password-reset-heading">
              {isReset ? "Set a new password" : "Forgot your password?"}
            </h2>
          </div>
          <button className="text-button" onClick={() => onModeChange("login")} type="button">
            Back to sign in
          </button>
        </div>
        {missingResetToken ? (
          <div className="password-reset-result">
            <p className="form-message form-error" role="alert">
              This reset link is incomplete. Request a new link to reset your password.
            </p>
            <button
              className="secondary-button"
              onClick={() => onModeChange("forgot-password")}
              type="button"
            >
              Request another link
            </button>
          </div>
        ) : message ? (
          <div className="password-reset-result">
            <p className="form-message" role="status">{message}</p>
            {!isReset && (
              <p className="field-hint">
                If an account exists for that address, check its inbox and spam folder.
              </p>
            )}
            {isReset && (
              <button
                className="primary-button"
                onClick={() => onModeChange("login")}
                type="button"
              >
                Continue to sign in
              </button>
            )}
          </div>
        ) : (
          <form className="auth-form" onSubmit={submit}>
            {isReset ? (
              <>
                <label htmlFor="reset-password">
                  New password
                  <span className="password-input-wrap">
                    <input
                      autoComplete="new-password"
                      id="reset-password"
                      maxLength={72}
                      minLength={8}
                      onChange={(event) => setPassword(event.target.value)}
                      required
                      type={showPassword ? "text" : "password"}
                      value={password}
                    />
                    <button
                      aria-label={showPassword ? "Hide password" : "Show password"}
                      className="password-visibility"
                      onClick={() => setShowPassword((current) => !current)}
                      type="button"
                    >
                      {showPassword ? "Hide" : "Show"}
                    </button>
                  </span>
                </label>
                <span className="field-hint">Use 8–72 characters.</span>
                <label htmlFor="reset-password-confirm">
                  Confirm new password
                  <input
                    autoComplete="new-password"
                    aria-invalid={passwordsDoNotMatch || undefined}
                    id="reset-password-confirm"
                    maxLength={72}
                    minLength={8}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    required
                    type={showPassword ? "text" : "password"}
                    value={confirmPassword}
                  />
                </label>
                {passwordsDoNotMatch && (
                  <p className="form-message form-error" role="status">Passwords do not match.</p>
                )}
              </>
            ) : (
              <label htmlFor="reset-email">
                Email
                <input
                  autoComplete="email"
                  id="reset-email"
                  onChange={(event) => setEmail(event.target.value)}
                  required
                  type="email"
                  value={email}
                />
              </label>
            )}
            {error && <p className="form-message form-error" role="alert">{error}</p>}
            <button
              className="primary-button"
              disabled={busy || (isReset && passwordsDoNotMatch)}
              type="submit"
            >
              {busy ? "Please wait…" : isReset ? "Reset password" : "Send reset link"}
            </button>
          </form>
        )}
        {!isReset && !message && (
          <p className="auth-switch">
            Remember your password?{" "}
            <button className="text-button" onClick={() => onModeChange("login")} type="button">
              Sign in
            </button>
          </p>
        )}
      </section>
    </div>
  );
}
