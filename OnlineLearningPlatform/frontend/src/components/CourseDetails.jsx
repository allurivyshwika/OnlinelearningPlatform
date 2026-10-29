export default function CourseDetails({
  course,
  index,
  enrolled,
  busy,
  onBack,
  onEnroll,
  onOpenQuiz,
}) {
  return (
    <section className="course-details" aria-labelledby="course-details-heading">
      <button className="text-button" onClick={onBack}>← Back to courses</button>
      <div className={`details-art course-art-${index % 3}`}>
        <span className="course-art-mark" aria-hidden="true">
          {index % 3 === 0 ? "{ }" : index % 3 === 1 ? "◈" : "▤"}
        </span>
      </div>
      <div className="details-content">
        <p className="eyebrow">COURSE DETAILS</p>
        <h2 id="course-details-heading">{course.title}</h2>
        <p className="details-description">
          {course.description || "Explore the topics and build your skills."}
        </p>
        <p className="details-instructor">
          Instructor <strong>{course.instructor || "To be announced"}</strong>
        </p>
        <div className="details-actions">
          {enrolled ? (
            <button className="primary-button" onClick={onOpenQuiz}>Continue learning</button>
          ) : (
            <button className="primary-button" disabled={busy} onClick={onEnroll}>
              {busy ? "Enrolling…" : "Enroll in this course"}
            </button>
          )}
        </div>
      </div>
    </section>
  );
}
