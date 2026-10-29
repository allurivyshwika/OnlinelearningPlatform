CREATE DATABASE IF NOT EXISTS learning_platform;
USE learning_platform;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  instructor VARCHAR(120) NOT NULL,
  category VARCHAR(80) NOT NULL,
  duration_hours INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS enrollments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  progress_percent INT NOT NULL DEFAULT 0,
  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_user_course (user_id, course_id),
  CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

INSERT INTO courses (title, description, instructor, category, duration_hours)
SELECT 'React JS Fundamentals', 'Learn components, props, state, and hooks.', 'Ananya Rao', 'Web Development', 8
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'React JS Fundamentals');

INSERT INTO courses (title, description, instructor, category, duration_hours)
SELECT 'Java and Spring MVC', 'Build Java web applications and REST APIs.', 'Rahul Kumar', 'Backend Development', 10
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'Java and Spring MVC');

INSERT INTO courses (title, description, instructor, category, duration_hours)
SELECT 'MySQL Essentials', 'Understand relational data, SQL queries, and joins.', 'Meera Shah', 'Database', 6
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'MySQL Essentials');
