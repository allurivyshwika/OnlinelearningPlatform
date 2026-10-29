USE online_learning;

INSERT INTO courses (title, description, instructor)
SELECT 'Java Programming', 'Learn Java fundamentals', 'Ravi Kumar'
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE title = 'Java Programming'
);

INSERT INTO courses (title, description, instructor)
SELECT 'React JS', 'Learn frontend development', 'Priya Sharma'
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE title = 'React JS'
);

INSERT INTO courses (title, description, instructor)
SELECT 'Database Management', 'Learn SQL and MySQL', 'Anil Kumar'
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE title = 'Database Management'
);

INSERT INTO courses (title, description, instructor)
SELECT 'Spring Boot', 'Learn Spring Boot development', 'Annaya.K'
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE title = 'Spring Boot'
);