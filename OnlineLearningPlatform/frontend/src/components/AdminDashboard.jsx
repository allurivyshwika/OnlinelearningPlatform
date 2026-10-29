import { useEffect, useState } from "react";
import { request } from "../api.js";

const blankQuestion = () => ({
  question: "",
  options: [
    { text: "", correct: true },
    { text: "", correct: false },
    { text: "", correct: false },
    { text: "", correct: false },
  ],
});

export default function AdminDashboard() {
  const [statistics, setStatistics] = useState(null);
  const [users, setUsers] = useState([]);
  const [categories, setCategories] = useState([]);
  const [courses, setCourses] = useState([]);
  const [categoryName, setCategoryName] = useState("");
  const [section, setSection] = useState("overview");
  const [userSearch, setUserSearch] = useState("");
  const [courseSearch, setCourseSearch] = useState("");
  const [newCourseTitle, setNewCourseTitle] = useState("");
  const [newCourseDescription, setNewCourseDescription] = useState("");
  const [newCourseCategoryId, setNewCourseCategoryId] = useState("");
  const [quizCourseId, setQuizCourseId] = useState("");
  const [quizTitle, setQuizTitle] = useState("");
  const [questions, setQuestions] = useState([blankQuestion()]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  async function loadAdminData() {
    const [stats, managedUsers, managedCategories, managedCourses] = await Promise.all([
      request("/api/admin/dashboard"),
      request("/api/admin/users"),
      request("/api/admin/categories"),
      request("/api/admin/courses"),
    ]);
    setStatistics(stats);
    setUsers(managedUsers);
    setCategories(managedCategories);
    setCourses(managedCourses);
  }

  useEffect(() => {
    loadAdminData()
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false));
  }, []);

  async function updateRole(userId, role) {
    setError("");
    setNotice("");
    try {
      await request(`/api/admin/users/${userId}/role`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role }),
      });
      setUsers((current) =>
        current.map((user) => (user.userId === userId ? { ...user, role } : user)),
      );
      setNotice("User role updated.");
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function createCategory(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await request("/api/admin/categories", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: categoryName }),
      });
      setCategoryName("");
      setNotice("Category created.");
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function deleteCategory(categoryId) {
    setError("");
    setNotice("");
    try {
      await request(`/api/admin/categories/${categoryId}`, { method: "DELETE" });
      setCategories((current) => current.filter((category) => category.categoryId !== categoryId));
      setNotice("Category deleted.");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function assignInstructor(courseId, instructorId) {
    setError("");
    setNotice("");
    try {
      const updatedCourse = await request(`/api/admin/courses/${courseId}/instructor`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ instructorId: Number(instructorId) }),
      });
      setCourses((current) =>
        current.map((course) => course.courseId === courseId ? updatedCourse : course),
      );
      setNotice("Course instructor assigned.");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function deleteCourse(courseId) {
    setError("");
    setNotice("");
    try {
      await request(`/api/admin/courses/${courseId}`, { method: "DELETE" });
      setCourses((current) => current.filter((course) => course.courseId !== courseId));
      setNotice("Course deleted.");
      await loadAdminData();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function createCourse(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");
    try {
      const course = await request("/api/admin/courses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: newCourseTitle,
          description: newCourseDescription,
          categoryId: newCourseCategoryId ? Number(newCourseCategoryId) : null,
        }),
      });
      setCourses((current) => [...current, course]);
      setNewCourseTitle("");
      setNewCourseDescription("");
      setNewCourseCategoryId("");
      setQuizCourseId(String(course.courseId));
      setNotice("Course created. You can now assign an instructor or add a quiz.");
      setSection("courses");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  function updateQuestion(questionIndex, update) {
    setQuestions((current) =>
      current.map((question, index) =>
        index === questionIndex ? { ...question, ...update } : question,
      ),
    );
  }

  function setCorrectOption(questionIndex, optionIndex) {
    setQuestions((current) =>
      current.map((question, index) =>
        index === questionIndex
          ? {
              ...question,
              options: question.options.map((option, candidate) => ({
                ...option,
                correct: candidate === optionIndex,
              })),
            }
          : question,
      ),
    );
  }

  async function createQuiz(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await request(`/api/admin/courses/${quizCourseId}/quizzes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: quizTitle, questions }),
      });
      setQuizTitle("");
      setQuestions([blankQuestion()]);
      setNotice("Quiz created with its questions and answer choices.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const instructors = users.filter((user) => user.role === "INSTRUCTOR");
  const normalizedUserSearch = userSearch.trim().toLowerCase();
  const filteredUsers = users.filter((user) =>
    `${user.name} ${user.email} ${user.role}`.toLowerCase().includes(normalizedUserSearch),
  );
  const normalizedCourseSearch = courseSearch.trim().toLowerCase();
  const filteredCourses = courses.filter((course) =>
    `${course.title} ${course.instructor || ""}`.toLowerCase().includes(normalizedCourseSearch),
  );
  const sections = [
    { id: "overview", label: "Overview" },
    { id: "users", label: "Users", count: users.length },
    { id: "courses", label: "Courses", count: courses.length },
    { id: "create-course", label: "Add course" },
    { id: "quizzes", label: "Quizzes" },
    { id: "categories", label: "Categories", count: categories.length },
  ];

  return (
    <section className="dashboard management-dashboard" aria-labelledby="admin-heading">
      <div className="catalog-heading">
        <div>
          <p className="eyebrow">ADMINISTRATION</p>
          <h2 id="admin-heading">Administrator dashboard</h2>
          <p className="admin-intro">Manage accounts, courses, and the learning catalog from one place.</p>
        </div>
        <span className="admin-status"><span aria-hidden="true" /> Platform controls</span>
      </div>
      {error && <p className="form-message form-error" role="alert">{error}</p>}
      {notice && <p className="notice-message" role="status">{notice}</p>}
      <div className="admin-workspace">
        <aside className="admin-sidebar">
          <div className="admin-sidebar-heading">
            <span className="admin-sidebar-mark" aria-hidden="true">A</span>
            <span>
              <strong>Admin panel</strong>
              <span>Workspace</span>
            </span>
          </div>
          <p className="admin-sidebar-label">MANAGE PLATFORM</p>
          <nav className="admin-navigation" aria-label="Admin dashboard sections">
            {sections.map((item, index) => (
              <button
                aria-current={section === item.id ? "page" : undefined}
                className={`admin-nav-item${section === item.id ? " admin-nav-item-active" : ""}`}
                key={item.id}
                onClick={() => setSection(item.id)}
                type="button"
              >
                <span className="admin-nav-icon" aria-hidden="true">{String(index + 1).padStart(2, "0")}</span>
                <span className="admin-nav-label">{item.label}</span>
                {item.count !== undefined && <span className="admin-nav-count">{item.count}</span>}
              </button>
            ))}
          </nav>
          <div className="admin-sidebar-note">
            <span className="admin-status-dot" aria-hidden="true" />
            <span><strong>Platform controls</strong><span>Administrator access</span></span>
          </div>
        </aside>
        <div className="admin-content">
          {loading ? (
            <p className="status-message" role="status">Loading platform management data…</p>
          ) : (
            <>
          {section === "overview" && (
            <div className="admin-overview">
              {statistics && (
                <div className="dashboard-stats">
                  <div className="stat-card"><span>STUDENTS</span><strong>{statistics.students}</strong></div>
                  <div className="stat-card"><span>INSTRUCTORS</span><strong>{statistics.instructors}</strong></div>
                  <div className="stat-card"><span>ADMINISTRATORS</span><strong>{statistics.admins}</strong></div>
                  <div className="stat-card"><span>COURSES</span><strong>{statistics.courses}</strong></div>
                </div>
              )}
              <section className="management-section admin-quick-actions">
                <div>
                  <p className="eyebrow">QUICK ACTIONS</p>
                  <h3>What would you like to manage?</h3>
                </div>
                <div className="quick-action-grid">
                  <button onClick={() => setSection("users")} type="button">
                    <span className="quick-action-icon" aria-hidden="true">01</span>
                    <strong>Review accounts</strong>
                    <span>Search users and update access roles</span>
                    <span className="quick-action-arrow" aria-hidden="true">→</span>
                  </button>
                  <button onClick={() => setSection("courses")} type="button">
                    <span className="quick-action-icon" aria-hidden="true">02</span>
                    <strong>Manage courses</strong>
                    <span>Assign instructors and oversee enrollment</span>
                    <span className="quick-action-arrow" aria-hidden="true">→</span>
                  </button>
                  <button onClick={() => setSection("categories")} type="button">
                    <span className="quick-action-icon" aria-hidden="true">03</span>
                    <strong>Organize catalog</strong>
                    <span>Create and maintain course categories</span>
                    <span className="quick-action-arrow" aria-hidden="true">→</span>
                  </button>
                </div>
              </section>
            </div>
          )}

          {section === "users" && (
            <section className="management-section">
              <div className="management-section-heading">
                <div>
                  <p className="eyebrow">ACCOUNTS</p>
                  <h3>Manage user roles</h3>
                </div>
                <label className="management-search">
                  <span className="visually-hidden">Search accounts</span>
                  <input
                    onChange={(event) => setUserSearch(event.target.value)}
                    placeholder="Search name, email, or role"
                    type="search"
                    value={userSearch}
                  />
                </label>
              </div>
              <div className="management-list">
                {filteredUsers.map((user) => (
                  <article className="management-row" key={user.userId}>
                    <div className="managed-user">
                      <span className="managed-user-avatar" aria-hidden="true">
                        {user.name?.trim().charAt(0).toUpperCase() || "U"}
                      </span>
                      <span className="managed-user-details">
                        <strong>{user.name}</strong>
                        <span>{user.email}</span>
                      </span>
                    </div>
                    <select
                      aria-label={`Role for ${user.name}`}
                      onChange={(event) => updateRole(user.userId, event.target.value)}
                      value={user.role}
                    >
                      <option value="STUDENT">Student</option>
                      <option value="INSTRUCTOR">Instructor</option>
                      <option value="ADMIN">Administrator</option>
                    </select>
                  </article>
                ))}
                {filteredUsers.length === 0 && (
                  <p className="status-message">No accounts match your search.</p>
                )}
              </div>
            </section>
          )}

          {section === "courses" && (
            <section className="management-section">
              <div className="management-section-heading">
                <div>
                  <p className="eyebrow">COURSE OVERSIGHT</p>
                  <h3>Manage courses and instructors</h3>
                </div>
                <label className="management-search">
                  <span className="visually-hidden">Search courses</span>
                  <input
                    onChange={(event) => setCourseSearch(event.target.value)}
                    placeholder="Search courses or instructors"
                    type="search"
                    value={courseSearch}
                  />
                </label>
              </div>
              <div className="management-list">
                {filteredCourses.map((course) => (
                  <article className="management-row" key={course.courseId}>
                    <div>
                      <strong>{course.title}</strong>
                      <span>{course.instructor || "No instructor assigned"}</span>
                    </div>
                    <div className="management-row-actions">
                      <span className="course-enrollment-summary">
                        <strong>{course.enrollmentCount}</strong>
                        <span>{course.enrollmentCount === 1 ? "student enrolled" : "students enrolled"}</span>
                      </span>
                      <select
                        aria-label={`Instructor for ${course.title}`}
                        disabled={instructors.length === 0}
                        onChange={(event) => assignInstructor(course.courseId, event.target.value)}
                        value={course.instructorId || ""}
                      >
                        <option value="" disabled>Assign instructor</option>
                        {instructors.map((instructor) => (
                          <option key={instructor.userId} value={instructor.userId}>{instructor.name}</option>
                        ))}
                      </select>
                      <button
                        className="text-button destructive-button"
                        disabled={course.enrollmentCount > 0}
                        onClick={() => deleteCourse(course.courseId)}
                        title={course.enrollmentCount > 0 ? "Courses with enrollments cannot be deleted" : undefined}
                        type="button"
                      >
                        Delete
                      </button>
                    </div>
                  </article>
                ))}
                {filteredCourses.length === 0 && (
                  <p className="status-message">No courses match your search.</p>
                )}
              </div>
            </section>
          )}

          {section === "create-course" && (
            <section className="management-section">
              <p className="eyebrow">COURSE CATALOG</p>
              <h3>Add a course</h3>
              <p className="admin-section-description">
                Create a course, then assign an instructor from the Courses section if needed.
              </p>
              <form className="management-form" onSubmit={createCourse}>
                <label>
                  Course title
                  <input
                    maxLength={150}
                    onChange={(event) => setNewCourseTitle(event.target.value)}
                    required
                    value={newCourseTitle}
                  />
                </label>
                <label>
                  Description
                  <textarea
                    maxLength={10000}
                    onChange={(event) => setNewCourseDescription(event.target.value)}
                    rows={4}
                    value={newCourseDescription}
                  />
                </label>
                <label>
                  Category
                  <select
                    onChange={(event) => setNewCourseCategoryId(event.target.value)}
                    value={newCourseCategoryId}
                  >
                    <option value="">No category</option>
                    {categories.map((category) => (
                      <option key={category.categoryId} value={category.categoryId}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
                <button className="primary-button" disabled={busy} type="submit">
                  {busy ? "Creating…" : "Create course"}
                </button>
              </form>
            </section>
          )}

          {section === "quizzes" && (
            <section className="management-section">
              <p className="eyebrow">ASSESS LEARNING</p>
              <h3>Create a quiz and questions</h3>
              {courses.length === 0 ? (
                <p className="status-message">
                  Add a course first, then you can attach a quiz and its questions.
                </p>
              ) : (
                <form className="management-form" onSubmit={createQuiz}>
                  <label>
                    Course
                    <select
                      onChange={(event) => setQuizCourseId(event.target.value)}
                      required
                      value={quizCourseId}
                    >
                      <option value="">Select a course</option>
                      {courses.map((course) => (
                        <option key={course.courseId} value={course.courseId}>
                          {course.title}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Quiz title
                    <input
                      maxLength={150}
                      onChange={(event) => setQuizTitle(event.target.value)}
                      required
                      value={quizTitle}
                    />
                  </label>
                  {questions.map((question, questionIndex) => (
                    <fieldset className="question-card" key={questionIndex}>
                      <legend>Question {questionIndex + 1}</legend>
                      <label>
                        Question text
                        <textarea
                          maxLength={2000}
                          onChange={(event) => updateQuestion(questionIndex, {
                            question: event.target.value,
                          })}
                          required
                          rows={3}
                          value={question.question}
                        />
                      </label>
                      <p className="field-hint">Add answer choices and mark exactly one as correct.</p>
                      {question.options.map((option, optionIndex) => (
                        <div className="management-option" key={optionIndex}>
                          <input
                            aria-label={`Question ${questionIndex + 1} answer ${optionIndex + 1}`}
                            maxLength={255}
                            onChange={(event) =>
                              updateQuestion(questionIndex, {
                                options: question.options.map((candidate, candidateIndex) =>
                                  candidateIndex === optionIndex
                                    ? { ...candidate, text: event.target.value }
                                    : candidate,
                                ),
                              })
                            }
                            placeholder={`Answer ${optionIndex + 1}`}
                            required
                            value={option.text}
                          />
                          <label>
                            <input
                              checked={option.correct}
                              name={`admin-correct-${questionIndex}`}
                              onChange={() => setCorrectOption(questionIndex, optionIndex)}
                              type="radio"
                            />
                            Correct
                          </label>
                        </div>
                      ))}
                      {questions.length > 1 && (
                        <button
                          className="text-button destructive-button"
                          onClick={() => setQuestions((current) =>
                            current.filter((_, index) => index !== questionIndex))}
                          type="button"
                        >
                          Remove question
                        </button>
                      )}
                    </fieldset>
                  ))}
                  {questions.length < 30 && (
                    <button
                      className="secondary-button"
                      onClick={() => setQuestions((current) => [...current, blankQuestion()])}
                      type="button"
                    >
                      Add question
                    </button>
                  )}
                  <button className="primary-button" disabled={busy} type="submit">
                    {busy ? "Creating…" : "Create quiz"}
                  </button>
                </form>
              )}
            </section>
          )}

          {section === "categories" && (
            <section className="management-section">
              <p className="eyebrow">COURSE CATALOG</p>
              <h3>Manage categories</h3>
              <form className="inline-form" onSubmit={createCategory}>
                <input
                  aria-label="New category name"
                  maxLength={80}
                  onChange={(event) => setCategoryName(event.target.value)}
                  placeholder="Category name"
                  required
                  value={categoryName}
                />
                <button className="primary-button" disabled={busy} type="submit">
                  {busy ? "Adding…" : "Add category"}
                </button>
              </form>
              <div className="category-list">
                {categories.map((category) => (
                  <span className="category-chip" key={category.categoryId}>
                    {category.name}
                    <button
                      aria-label={`Delete ${category.name}`}
                      onClick={() => deleteCategory(category.categoryId)}
                      type="button"
                    >
                      ×
                    </button>
                  </span>
                ))}
                {categories.length === 0 && <p className="status-message">No categories created yet.</p>}
              </div>
            </section>
          )}
            </>
          )}
        </div>
      </div>
    </section>
  );
}
