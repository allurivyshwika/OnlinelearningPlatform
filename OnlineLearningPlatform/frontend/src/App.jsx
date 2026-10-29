import { useEffect, useState } from "react";
import { API_BASE_URL, request } from "./api.js";
import AuthForm from "./components/AuthForm.jsx";
import AdminDashboard from "./components/AdminDashboard.jsx";
import CourseCard from "./components/CourseCard.jsx";
import CourseDetails from "./components/CourseDetails.jsx";
import Dashboard from "./components/Dashboard.jsx";
import InstructorDashboard from "./components/InstructorDashboard.jsx";
import Navbar from "./components/Navbar.jsx";
import PasswordResetForm from "./components/PasswordResetForm.jsx";
import QuizPanel from "./components/QuizPanel.jsx";

function Hero() {
  return (
    <section className="hero">
      <div className="hero-copy">
        <p className="eyebrow"><span className="eyebrow-line" />LEARN SOMETHING NEW</p>
        <h1>Find your<br /><span>next skill.</span></h1>
        <p className="hero-description">
          Practical courses to help you grow your skills and move forward with confidence.
        </p>
      </div>
      <div className="hero-stamp" aria-label="Learn at your own pace">
        <span>LEARN</span><span>AT YOUR</span><span>OWN PACE</span>
        <span className="stamp-star" aria-hidden="true">✳</span>
      </div>
    </section>
  );
}

function authModeFromPath(pathname) {
  const path = pathname.replace(/\/+$/, "");
  if (path === "/admin/login") return "admin";
  if (path === "/forgot-password" || path === "/reset-password") return path.slice(1);
  if (path === "/login" || path === "/register") return path.slice(1);
  return null;
}

function App() {
  const [courses, setCourses] = useState([]);
  const [user, setUser] = useState(null);
  const [enrolled, setEnrolled] = useState([]);
  const [attempts, setAttempts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [quizLoading, setQuizLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [catalogError, setCatalogError] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [view, setView] = useState("catalog");
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [authMode, setAuthMode] = useState(() => authModeFromPath(window.location.pathname));
  const [authError, setAuthError] = useState("");
  const [authMessage, setAuthMessage] = useState("");
  const [quizCourse, setQuizCourse] = useState(null);
  const [quizzes, setQuizzes] = useState([]);
  const [answers, setAnswers] = useState({});
  const [quizResult, setQuizResult] = useState(null);

  function navigateAuth(mode, { replace = false } = {}) {
    const path = mode === "admin"
      ? "/admin/login"
      : mode === "forgot-password"
        ? "/forgot-password"
        : mode === "reset-password"
          ? `/reset-password${window.location.search}`
          : mode
            ? `/${mode}`
            : "/";
    if (window.location.pathname !== path) {
      window.history[replace ? "replaceState" : "pushState"]({}, "", path);
    }
    setAuthMode(mode);
    setAuthError("");
    setAuthMessage("");
  }

  useEffect(() => {
    function syncAuthRoute() {
      setAuthMode(authModeFromPath(window.location.pathname));
      setAuthError("");
      setAuthMessage("");
      setView("catalog");
      setSelectedCourse(null);
      setQuizCourse(null);
    }

    window.addEventListener("popstate", syncAuthRoute);
    return () => window.removeEventListener("popstate", syncAuthRoute);
  }, []);

  async function loadLearningData(signal) {
    const [dashboard, history] = await Promise.all([
      request("/api/dashboard", { signal }),
      request("/api/quiz-attempts", { signal }),
    ]);
    setEnrolled(dashboard);
    setAttempts(history);
  }

  useEffect(() => {
    const controller = new AbortController();
    async function initialize() {
      try {
        const catalog = await request("/api/courses", { signal: controller.signal });
        if (controller.signal.aborted) return;
        setCourses(catalog);

        try {
          const currentUser = await request("/api/auth/me", { signal: controller.signal });
          if (controller.signal.aborted) return;
          setUser(currentUser);
          await loadLearningData(controller.signal);
        } catch (requestError) {
          if (requestError.status !== 401 && requestError.name !== "AbortError") {
            throw requestError;
          }
        }
      } catch (requestError) {
        if (requestError.name !== "AbortError" && !controller.signal.aborted) {
          setCatalogError(
            `${requestError.message} Check that the backend is running at ${API_BASE_URL}.`,
          );
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    initialize();
    return () => controller.abort();
  }, []);

  async function refreshLearningData() {
    await loadLearningData();
  }

  async function submitAuth(formData) {
    setBusy(true);
    setAuthError("");
    setAuthMessage("");
    try {
      if (authMode === "register") {
        await request("/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        });
      }
      const signedIn = await request("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: formData.email, password: formData.password }),
      });
      if (authMode === "admin" && signedIn.role !== "ADMIN") {
        await request("/api/auth/logout", { method: "POST" });
        setUser(null);
        setEnrolled([]);
        setAttempts([]);
        throw new Error("This account does not have administrator access. Use the regular sign-in page.");
      }
      setUser(signedIn);
      await refreshLearningData();
      navigateAuth(null, { replace: true });
      setView("dashboard");
      setNotice(`Welcome, ${signedIn.name}.`);
    } catch (requestError) {
      setAuthError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function enroll(course) {
    if (!user) {
      navigateAuth("login");
      setAuthMessage("Sign in or create an account to enroll in a course.");
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await request(`/api/enrollments/${course.courseId}`, { method: "POST" });
      await refreshLearningData();
      setNotice(`You’re enrolled in ${course.title}.`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function markComplete(courseId) {
    setBusy(true);
    setError("");
    try {
      await request(`/api/enrollments/${courseId}/progress`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ progressPercent: 100 }),
      });
      await refreshLearningData();
      setNotice("Course marked complete.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function openQuiz(course) {
    if (!user) {
      navigateAuth("login");
      setAuthMessage("Sign in to take course quizzes.");
      return;
    }
    setQuizCourse(course);
    setSelectedCourse(null);
    setQuizzes([]);
    setAnswers({});
    setQuizResult(null);
    setError("");
    setQuizLoading(true);
    try {
      setQuizzes(await request(`/api/courses/${course.courseId}/quizzes`));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setQuizLoading(false);
    }
  }

  async function submitQuiz(event) {
    event.preventDefault();
    if (!quizzes[0]) return;
    setBusy(true);
    setError("");
    try {
      const result = await request(`/api/quizzes/${quizzes[0].quizId}/attempts`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          answers: Object.fromEntries(
            Object.entries(answers).map(([questionId, optionId]) => [
              Number(questionId),
              optionId,
            ]),
          ),
        }),
      });
      setQuizResult(result);
      await refreshLearningData();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function logout() {
    setBusy(true);
    setError("");
    try {
      await request("/api/auth/logout", { method: "POST" });
      setUser(null);
      setEnrolled([]);
      setAttempts([]);
      setView("catalog");
      setSelectedCourse(null);
      setNotice("You’ve signed out.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const enrolledIds = new Set(enrolled.map((course) => course.courseId));
  const selectedCourseIndex = courses.findIndex(
    (course) => course.courseId === selectedCourse?.courseId,
  );

  return (
    <main className="page-shell">
      <Navbar
        user={user}
        view={view}
        authMode={authMode}
        busy={busy}
        onNavigate={(nextView) => {
          navigateAuth(null);
          setView(nextView);
          setSelectedCourse(null);
          setQuizCourse(null);
          setError("");
        }}
        onLogin={() => navigateAuth("login")}
        onAdminLogin={() => navigateAuth("admin")}
        onRegister={() => navigateAuth("register")}
        onLogout={logout}
      />
      {authMode ? (
        authMode === "forgot-password" || authMode === "reset-password" ? (
          <PasswordResetForm
            mode={authMode}
            token={new URLSearchParams(window.location.search).get("token") || ""}
            onModeChange={navigateAuth}
          />
        ) : (
          <AuthForm
            mode={authMode}
            onModeChange={navigateAuth}
            onSubmit={submitAuth}
            busy={busy}
            error={authError}
            message={authMessage}
          />
        )
      ) : (
        <>
          <Hero />

          {notice && <p className="notice-message" role="status">{notice}</p>}
          {error && !quizCourse && (
            <div className="status-panel error-panel" role="alert">
              <p>{error}</p>
              <button className="text-button" onClick={() => setError("")}>Dismiss</button>
            </div>
          )}

          {quizCourse ? (
            <QuizPanel
              course={quizCourse}
              quizzes={quizzes}
              loading={quizLoading}
              answers={answers}
              result={quizResult}
              busy={busy}
              error={error}
              onAnswer={(questionId, optionId) =>
                setAnswers((current) => ({ ...current, [questionId]: optionId }))
              }
              onSubmit={submitQuiz}
              onClose={() => {
                setQuizCourse(null);
                setError("");
              }}
            />
          ) : view === "dashboard" && user?.role === "ADMIN" ? (
            <AdminDashboard />
          ) : view === "dashboard" && user?.role === "INSTRUCTOR" ? (
            <InstructorDashboard />
          ) : view === "dashboard" && user ? (
            <Dashboard
              user={user}
              courses={enrolled}
              attempts={attempts}
              onMarkComplete={markComplete}
              onOpenQuiz={openQuiz}
              busy={busy}
            />
          ) : selectedCourse ? (
            <CourseDetails
              course={selectedCourse}
              index={selectedCourseIndex}
              enrolled={enrolledIds.has(selectedCourse.courseId)}
              busy={busy}
              onBack={() => setSelectedCourse(null)}
              onEnroll={() => enroll(selectedCourse)}
              onOpenQuiz={() => openQuiz(selectedCourse)}
            />
          ) : (
            <section className="catalog" aria-labelledby="catalog-heading">
              <div className="catalog-heading">
                <div>
                  <p className="eyebrow">THE CATALOG</p>
                  <h2 id="catalog-heading">Explore courses</h2>
                </div>
                {!loading && !catalogError && (
                  <span className="course-count">
                    {courses.length} {courses.length === 1 ? "COURSE" : "COURSES"}
                  </span>
                )}
              </div>
              {loading && <p className="status-message" role="status">Loading courses…</p>}
              {!loading && catalogError && (
                <div className="status-panel error-panel" role="alert">
                  <p>{catalogError}</p>
                  <button className="text-button" onClick={() => window.location.reload()}>
                    Reload page
                  </button>
                </div>
              )}
              {!loading && !catalogError && courses.length === 0 && (
                <p className="status-message">No courses found. Check back soon.</p>
              )}
              {!loading && !catalogError && courses.length > 0 && (
                <div className="course-grid">
                  {courses.map((course, index) => (
                    <CourseCard
                      key={course.courseId}
                      course={course}
                      index={index}
                      enrolled={enrolledIds.has(course.courseId)}
                      onDetails={() => setSelectedCourse(course)}
                      onEnroll={() => enroll(course)}
                      onOpenQuiz={() => openQuiz(course)}
                      busy={busy}
                    />
                  ))}
                </div>
              )}
            </section>
          )}

          <footer className="page-footer">
            <span>LEARN AT YOUR OWN PACE.</span>
            <span>BUILT FOR WHAT’S NEXT.</span>
          </footer>
        </>
      )}
    </main>
  );
}

export default App;
