USE online_learning;

CREATE TABLE IF NOT EXISTS quizzes (
    quiz_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    question_order INT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_options (
    option_id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    option_order INT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    quiz_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_instructors (
    course_id INT PRIMARY KEY,
    instructor_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course_category_assignments (
    course_id INT PRIMARY KEY,
    category_id INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES course_categories(category_id) ON DELETE CASCADE
);

INSERT INTO quizzes (course_id, title)
SELECT c.course_id, seed.title
FROM courses c
JOIN (
    SELECT 'Java Programming' AS course_title, 'Java Fundamentals Check-in' AS title
    UNION ALL SELECT 'React JS', 'React Fundamentals Check-in'
    UNION ALL SELECT 'Database Management', 'Database Fundamentals Check-in'
) seed ON seed.course_title = c.title
WHERE NOT EXISTS (
    SELECT 1 FROM quizzes q WHERE q.course_id = c.course_id AND q.title = seed.title
);

INSERT INTO quiz_questions (quiz_id, question_text, question_order)
SELECT q.quiz_id, seed.question_text, seed.question_order
FROM quizzes q
JOIN courses c ON c.course_id = q.course_id
JOIN (
    SELECT 'Java Fundamentals Check-in' AS quiz_title,
           'Which keyword declares a class in Java?' AS question_text, 1 AS question_order
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which method is the Java application entry point?', 2
    UNION ALL SELECT 'React Fundamentals Check-in', 'Which hook manages local state in a function component?', 1
    UNION ALL SELECT 'React Fundamentals Check-in', 'What syntax is used to describe a React UI?', 2
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which SQL statement retrieves rows from a table?', 1
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which key uniquely identifies a table row?', 2
) seed ON seed.quiz_title = q.title
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_questions existing
    WHERE existing.quiz_id = q.quiz_id
      AND existing.question_order = seed.question_order
);

INSERT INTO quiz_options (question_id, option_text, option_order, is_correct)
SELECT question.question_id, seed.option_text, seed.option_order, seed.is_correct
FROM quiz_questions question
JOIN quizzes quiz ON quiz.quiz_id = question.quiz_id
JOIN (
    SELECT 'Java Fundamentals Check-in' AS quiz_title,
           'Which keyword declares a class in Java?' AS question_text,
           'class' AS option_text, 1 AS option_order, TRUE AS is_correct
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which keyword declares a class in Java?', 'define', 2, FALSE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which keyword declares a class in Java?', 'struct', 3, FALSE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which keyword declares a class in Java?', 'module', 4, FALSE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which method is the Java application entry point?', 'main', 1, TRUE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which method is the Java application entry point?', 'start', 2, FALSE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which method is the Java application entry point?', 'run', 3, FALSE
    UNION ALL SELECT 'Java Fundamentals Check-in', 'Which method is the Java application entry point?', 'open', 4, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'Which hook manages local state in a function component?', 'useState', 1, TRUE
    UNION ALL SELECT 'React Fundamentals Check-in', 'Which hook manages local state in a function component?', 'useRoute', 2, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'Which hook manages local state in a function component?', 'useStyle', 3, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'Which hook manages local state in a function component?', 'useFetch', 4, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'What syntax is used to describe a React UI?', 'JSX', 1, TRUE
    UNION ALL SELECT 'React Fundamentals Check-in', 'What syntax is used to describe a React UI?', 'SQL', 2, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'What syntax is used to describe a React UI?', 'XML only', 3, FALSE
    UNION ALL SELECT 'React Fundamentals Check-in', 'What syntax is used to describe a React UI?', 'CSS', 4, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which SQL statement retrieves rows from a table?', 'SELECT', 1, TRUE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which SQL statement retrieves rows from a table?', 'PUSH', 2, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which SQL statement retrieves rows from a table?', 'FETCH ALL', 3, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which SQL statement retrieves rows from a table?', 'READ TABLE', 4, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which key uniquely identifies a table row?', 'Primary key', 1, TRUE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which key uniquely identifies a table row?', 'Foreign key', 2, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which key uniquely identifies a table row?', 'Sort key', 3, FALSE
    UNION ALL SELECT 'Database Fundamentals Check-in', 'Which key uniquely identifies a table row?', 'Alias', 4, FALSE
) seed ON seed.quiz_title = quiz.title AND seed.question_text = question.question_text
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_options existing
    WHERE existing.question_id = question.question_id
      AND existing.option_order = seed.option_order
);
