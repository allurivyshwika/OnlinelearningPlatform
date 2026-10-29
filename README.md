# Online Learning Platform

A starter full-stack case-study project using React (Vite), Spring Boot with Spring MVC, Spring Data JPA, and MySQL.

## Features
- Course catalogue with search
- Register and login (basic demo authentication)
- Enroll in courses
- Student dashboard showing enrollments
- REST API with Spring MVC
- MySQL schema and sample courses

> This is an academic starter project, not production-ready authentication. Before public use, replace demo authentication with Spring Security, password hashing, validation, and proper authorization.

## Requirements
- Node.js 20+
- Java 17+
- Maven 3.9+
- MySQL 8+

## Run locally

### 1. Database
Create a MySQL database and run `database/schema.sql`. Set environment variables for the backend:
- `DB_URL` (example: `jdbc:mysql://localhost:3306/learning_platform`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_PORT` (optional, defaults to 8080)

### 2. Backend
From `backend/`:
```bash
mvn spring-boot:run
```
API base URL: `http://localhost:8080/api`

### 3. Frontend
From `frontend/`:
```bash
npm install
npm run dev
```
Open the local URL Vite prints (usually `http://localhost:5173`).

Set `VITE_API_URL` if the backend is not at `http://localhost:8080/api`.

## GitHub and deployment
1. Create a new GitHub repository named `online-learning-platform`.
2. Upload/push the contents of this folder to that repository (do not upload `.env`, passwords, or database credentials).
3. Deploy the frontend as a Vercel project using the `frontend` directory as the root; build command `npm run build`, output directory `dist`. Set `VITE_API_URL` to your deployed backend API URL plus `/api`.
4. Deploy the `backend` folder as a Java/Maven web service on a host that supports Spring Boot. Build command: `./mvnw clean package` (if using Maven Wrapper) or `mvn clean package`; start command: `java -jar target/online-learning-backend-1.0.0.jar`. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `APP_PORT` in the host's environment settings.
5. Create a hosted MySQL database and allow the backend host to connect. Use the provider's exact JDBC URL and TLS requirements.
6. Update backend CORS allowed origins in `backend/src/main/java/com/example/learning/config/CorsConfig.java` to include your deployed frontend domain.
7. Test registration, login, course loading, and enrollment on the deployed URL.

Actual GitHub and deployment URLs are generated only after you publish and deploy from your own accounts; this starter archive does not create those accounts or publish automatically.
