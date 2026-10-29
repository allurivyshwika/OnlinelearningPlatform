export default function Dashboard({
  user,
  courses,
  attempts,
  onMarkComplete,
  onOpenQuiz,
  busy,
}) {
  const completed = courses.filter((course) => course.progressPercent >= 100).length;
  const average = courses.length
    ? Math.round(
        courses.reduce((sum, course) => sum + course.progressPercent, 0) /
          courses.length,
      )
    : 0;

  return (
    <section className="dashboard" aria-labelledby="dashboard-heading">
      <div className="catalog-heading">
        <div>
          <p className="eyebrow">YOUR LEARNING</p>
          <h2 id="dashboard-heading">Welcome, {user.name}</h2>
        </div>
        <span className="course-count">{courses.length} ENROLLED</span>
      </div>
      <div className="dashboard-stats">
        <div className="stat-card"><span>ENROLLED COURSES</span><strong>{courses.length}</strong></div>
        <div className="stat-card"><span>COMPLETED</span><strong>{completed}</strong></div>
        <div className="stat-card"><span>AVERAGE PROGRESS</span><strong>{average}%</strong></div>
      </div>
      {courses.length === 0 ? (
        <div className="status-message">
          You haven’t enrolled in a course yet. Explore the catalog to get started.
        </div>
      ) : (
        <div className="learning-list">
          {courses.map((course, index) => (
            <article className="learning-card" key={course.courseId}>
              <div className={`learning-icon course-art-${index % 3}`}>
                {String(index + 1).padStart(2, "0")}
              </div>
              <div className="learning-info">
                <p className="course-category">{course.instructor || "LEARNSPACE"}</p>
                <h3>{course.title}</h3>
                <div
                  className="progress-track"
                  aria-label={`${course.progressPercent}% complete`}
                  role="progressbar"
                  aria-valuenow={course.progressPercent}
                  aria-valuemin={0}
                  aria-valuemax={100}
                >
                  <span style={{ width: `${course.progressPercent}%` }} />
                </div>
                <p className="progress-caption">{course.progressPercent}% complete</p>
              </div>
              <div className="learning-actions">
                <button className="secondary-button" onClick={() => onOpenQuiz(course)}>
                  Take a quiz
                </button>
                <button
                  className="text-button"
                  disabled={busy || course.progressPercent >= 100}
                  onClick={() => onMarkComplete(course.courseId)}
                >
                  {course.progressPercent >= 100 ? "Completed" : "Mark complete"}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
      <section className="attempt-history" aria-labelledby="attempt-history-heading">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">YOUR RESULTS</p>
            <h2 id="attempt-history-heading">Quiz history</h2>
          </div>
        </div>
        {attempts.length === 0 ? (
          <p className="status-message">Your quiz results will appear here after your first attempt.</p>
        ) : (
          <div className="attempt-list">
            {attempts.map((attempt) => (
              <article className="attempt-card" key={attempt.attemptId}>
                <div>
                  <h3>{attempt.quizTitle}</h3>
                  <p>{attempt.courseTitle}</p>
                </div>
                <strong>{attempt.score}/{attempt.totalQuestions}</strong>
                <time dateTime={attempt.attemptedAt}>
                  {new Date(attempt.attemptedAt).toLocaleDateString()}
                </time>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
