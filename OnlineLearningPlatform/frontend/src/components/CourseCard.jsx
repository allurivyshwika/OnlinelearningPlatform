export default function CourseCard({
  course,
  index,
  enrolled,
  onDetails,
  onEnroll,
  onOpenQuiz,
  busy,
}) {
  return (
    <article className="course-card">
      <div className={`course-art course-art-${index % 3}`}>
        <span className="course-number">{String(index + 1).padStart(2, "0")}</span>
        <span className="course-art-mark" aria-hidden="true">
          {index % 3 === 0 ? "{ }" : index % 3 === 1 ? "◈" : "▤"}
        </span>
        <span className="course-label">COURSE</span>
      </div>
      <div className="course-content">
        <p className="course-category">ONLINE LEARNING</p>
        <button className="course-title-button" onClick={onDetails}>
          <h2>{course.title}</h2>
        </button>
        <p className="course-description">
          {course.description || "Explore the topics and build your skills."}
        </p>
        <div className="course-footer">
          <div className="instructor-avatar" aria-hidden="true">
            {(course.instructor || "I").charAt(0).toUpperCase()}
          </div>
          <div>
            <span className="instructor-caption">INSTRUCTOR</span>
            <p className="instructor-name">{course.instructor || "TBA"}</p>
          </div>
        </div>
        <div className="course-actions">
          <button className="text-button" onClick={onDetails}>Course details</button>
          {enrolled ? (
            <button className="secondary-button" onClick={onOpenQuiz}>Open quizzes</button>
          ) : (
            <button className="primary-button" disabled={busy} onClick={onEnroll}>
              {busy ? "Enrolling…" : "Enroll"}
            </button>
          )}
        </div>
      </div>
    </article>
  );
}
