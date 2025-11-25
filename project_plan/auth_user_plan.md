# 8. REST API Summary

## Auth

- [x] `POST /auth/register`
- [x] `POST /auth/login`
- [x] `POST /auth/refresh`

## Users

- [x] `GET /me`

Implemented components:
- Repositories: `UserRepository`
- Services: `AuthService`, `UserService`, `CustomUserDetailsService`
- Controllers: `AuthController`, `UserController`
- DTOs: `RegisterRequest`, `LoginRequest`, `RefreshRequest`, `AuthResponse`, `UserDto`
- Security: `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil` (JJWT 0.11.5)

Notes / how to run:
- The application runs on port 8081 by default (see `src/main/resources/application.yaml`).
- Set a secure JWT secret before running in production. You can set an environment variable:

  export JWT_SECRET="$(openssl rand -base64 48)"
  ./gradlew bootRun

- Example endpoints:
  - POST /auth/register  { "email": "a@b.com", "password": "secret", "name": "Alice" }
  - POST /auth/login     { "email": "a@b.com", "password": "secret" }
  - POST /auth/refresh   { "token": "<jwt>" }
  - GET  /me (requires Authorization: Bearer <jwt>)

All items above have been implemented and wired into the application. If you want, I can add integration tests for the endpoints or scaffold DTO validation error handlers next.
