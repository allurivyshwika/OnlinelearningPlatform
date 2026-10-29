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

export default function InstructorDashboard() {
  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [quizCourseId, setQuizCourseId] = useState("");
  const [quizTitle, setQuizTitle] = useState("");
  const [questions, setQuestions] = useState([blankQuestion()]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  async function loadInstructorData() {
    const [ownedCourses, availableCategories] = await Promise.all([
      request("/api/instructor/dashboard"),
      request("/api/categories"),
    ]);
    setCourses(ownedCourses);
    setCategories(availableCategories);
  }

  useEffect(() => {
    loadInstructorData().catch((requestError) => setError(requestError.message));
  }, []);

  async function createCourse(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await request("/api/instructor/courses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          description,
          categoryId: categoryId ? Number(categoryId) : null,
        }),
      });
      setTitle("");
      setDescription("");
      setCategoryId("");
      setNotice("Course created.");
      await loadInstructorData();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function deleteCourse(courseId) {
    setError("");
    setNotice("");
    try {
      await request(`/api/instructor/courses/${courseId}`, { method: "DELETE" });
      setCourses((current) => current.filter((course) => course.courseId !== courseId));
      setNotice("Course deleted.");
    } catch (requestError) {
      setError(requestError.message);
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
    if (!quizCourseId) {
      setError("Create or select one of your courses before adding a quiz.");
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await request(`/api/instructor/courses/${quizCourseId}/quizzes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: quizTitle, questions }),
      });
      setQuizTitle("");
      setQuestions([blankQuestion()]);
      setNotice("Quiz created and available to enrolled students.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="dashboard management-dashboard" aria-labelledby="instructor-heading">
      <div className="catalog-heading">
        <div>
          <p className="eyebrow">COURSE CREATOR</p>
          <h2 id="instructor-heading">Instructor dashboard</h2>
        </div>
        <span className="course-count">{courses.length} COURSES</span>
      </div>
      {error && <p className="form-message form-error" role="alert">{error}</p>}
      {notice && <p className="notice-message" role="status">{notice}</p>}
      <section className="management-section">
        <p className="eyebrow">YOUR CONTENT</p>
        <h3>Courses and enrollment</h3>
        {courses.length === 0 ? (
          <p className="status-message">You have not created any courses yet.</p>
        ) : (
          <div className="management-list">
            {courses.map((course) => (
              <article className="management-row" key={course.courseId}>
                <div>
                  <strong>{course.title}</strong>
                  <span>{course.enrollmentCount} enrolled</span>
                </div>
                <button
                  className="text-button destructive-button"
                  onClick={() => deleteCourse(course.courseId)}
                  type="button"
                >
                  Delete
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
      <section className="management-section">
        <p className="eyebrow">PUBLISH CONTENT</p>
        <h3>Create a course</h3>
        <form className="management-form" onSubmit={createCourse}>
          <label>Course title<input maxLength={150} onChange={(event) => setTitle(event.target.value)} required value={title} /></label>
          <label>Description<textarea maxLength={10000} onChange={(event) => setDescription(event.target.value)} rows={3} value={description} /></label>
          <label>
            Category
            <select onChange={(event) => setCategoryId(event.target.value)} value={categoryId}>
              <option value="">No category</option>
              {categories.map((category) => (
                <option key={category.categoryId} value={category.categoryId}>{category.name}</option>
              ))}
            </select>
          </label>
          <button className="primary-button" disabled={busy} type="submit">Create course</button>
        </form>
      </section>
      <section className="management-section">
        <p className="eyebrow">ASSESS LEARNING</p>
        <h3>Create a multiple-choice quiz</h3>
        <form className="management-form" onSubmit={createQuiz}>
          <label>
            Course
            <select onChange={(event) => setQuizCourseId(event.target.value)} required value={quizCourseId}>
              <option value="">Select a course</option>
              {courses.map((course) => (
                <option key={course.courseId} value={course.courseId}>{course.title}</option>
              ))}
            </select>
          </label>
          <label>Quiz title<input maxLength={150} onChange={(event) => setQuizTitle(event.target.value)} required value={quizTitle} /></label>
          {questions.map((question, questionIndex) => (
            <fieldset className="question-card" key={questionIndex}>
              <legend>Question {questionIndex + 1}</legend>
              <label>
                Question text
                <textarea
                  maxLength={2000}
                  onChange={(event) => updateQuestion(questionIndex, { question: event.target.value })}
                  required
                  rows={2}
                  value={question.question}
                />
              </label>
              {question.options.map((option, optionIndex) => (
                <div className="management-option" key={optionIndex}>
                  <input
                    aria-label={`Answer ${optionIndex + 1}`}
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
                      name={`correct-${questionIndex}`}
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
                  onClick={() => setQuestions((current) => current.filter((_, index) => index !== questionIndex))}
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
          <button className="primary-button" disabled={busy || courses.length === 0} type="submit">
            {busy ? "Creating…" : "Create quiz"}
          </button>
        </form>
      </section>
    </section>
  );
}
