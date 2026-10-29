export default function QuizPanel({
  course,
  quizzes,
  loading,
  answers,
  result,
  busy,
  error,
  onAnswer,
  onSubmit,
  onClose,
}) {
  const quiz = quizzes[0];

  return (
    <section className="quiz-panel" aria-labelledby="quiz-heading">
      <div className="quiz-panel-heading">
        <div>
          <p className="eyebrow">{course.title}</p>
          <h2 id="quiz-heading">{quiz?.title || "Course quiz"}</h2>
        </div>
        <button className="text-button" onClick={onClose}>Close</button>
      </div>
      {error && <p className="form-message form-error" role="alert">{error}</p>}
      {loading && <p className="status-message" role="status">Loading quiz…</p>}
      {!loading && !quiz && (
        <p className="status-message">No quizzes are available for this course yet.</p>
      )}
      {quiz && !result && (
        <form className="quiz-form" onSubmit={onSubmit}>
          {quiz.questions.map((question, index) => (
            <fieldset className="question-card" key={question.questionId}>
              <legend>
                <span>QUESTION {String(index + 1).padStart(2, "0")}</span>
                {question.question}
              </legend>
              <div className="answer-list">
                {question.options.map((option) => (
                  <label className="answer-option" key={option.optionId}>
                    <input
                      checked={answers[question.questionId] === option.optionId}
                      name={`question-${question.questionId}`}
                      onChange={() => onAnswer(question.questionId, option.optionId)}
                      type="radio"
                    />
                    <span>{option.text}</span>
                  </label>
                ))}
              </div>
            </fieldset>
          ))}
          {quiz.questions.length > 0 && (
            <button className="primary-button" disabled={busy} type="submit">
              {busy ? "Submitting…" : "Submit answers"}
            </button>
          )}
        </form>
      )}
      {result && (
        <div className="quiz-result" role="status">
          <p className="eyebrow">QUIZ COMPLETE</p>
          <h3>{result.score} / {result.totalQuestions} correct</h3>
          <p>Your course progress is now {result.progressPercent}%.</p>
          <button className="primary-button" onClick={onClose}>Back to learning</button>
        </div>
      )}
    </section>
  );
}
