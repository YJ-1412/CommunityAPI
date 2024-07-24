# Community API

백엔드를 공부하고 연습하기 위하여 개발한 커뮤니티 RESTful API 토이 프로젝트

## API 개요 (Overview)
- **이름:** 커뮤니티 API
- **목적:** 여러 게시판에 사용자들이 글을 작성하고, 댓글을 달며, 좋아요를 누를 수 있는 커뮤니티 플랫폼 제공
- **대상 사용자:** 일반 사용자, 관리자

## 기술 스택 (Technical Stack)
- **프로그래밍 언어:** Kotlin
- **프레임워크:** Spring Boot, Spring Security
- **데이터베이스:** MySQL
- **기타 기술:** JPA, Docker

이하 내용 수정 필요

## 아키텍처 (Architecture)
- **시스템 구조도:**
  ![Architecture Diagram](링크)
- **주요 컴포넌트:**
  - **사용자 서비스:** 사용자 관리 및 인증
  - **게시글 서비스:** 게시글 작성, 조회, 수정, 삭제
  - **댓글 서비스:** 댓글 작성, 조회, 삭제
  - **알림 서비스:** 사용자 활동에 대한 알림 제공

## 엔드포인트 (Endpoints)
### 1. 사용자 관리 (User Management)
- **회원가입**
  - **URI:** `/api/users/signup`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "username": "example",
      "password": "password123",
      "email": "user@example.com"
    }
    ```
  - **응답 형식:**
    ```json
    {
      "message": "User registered successfully"
    }
    ```

- **로그인**
  - **URI:** `/api/users/login`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "username": "example",
      "password": "password123"
    }
    ```
  - **응답 형식:**
    ```json
    {
      "token": "jwt_token"
    }
    ```

### 2. 게시글 관리 (Post Management)
- **게시글 작성**
  - **URI:** `/api/posts`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "title": "Post Title",
      "content": "This is the content of the post."
    }
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post Title",
      "content": "This is the content of the post.",
      "author": "example"
    }
    ```

- **게시글 조회**
  - **URI:** `/api/posts/{id}`
  - **HTTP 메서드:** GET
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post Title",
      "content": "This is the content of the post.",
      "author": "example"
    }
    ```

## 인증 및 보안 (Authentication & Security)
- **인증 방식:** JWT (JSON Web Tokens)
- **권한 관리:** 사용자 역할에 따른 접근 제어 (일반 사용자, 관리자)

## 에러 처리 (Error Handling)
- **에러 코드와 메시지:**
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 리소스를 찾을 수 없음
  - `500 Internal Server Error`: 서버 내부 오류
- **예외 처리 방식:**
  - 글로벌 예외 처리기 (`@ControllerAdvice`)를 사용하여 예외를 처리하고, 사용자에게 적절한 메시지 반환

## 성능 및 확장성 (Performance & Scalability)
- **성능 최적화 방안:**
  - 데이터베이스 인덱싱
  - 캐싱 (예: Redis)
- **확장성 고려 사항:**
  - 마이크로서비스 아키텍처 도입
  - 수평적 확장 지원

## 테스트 및 문서화 (Testing & Documentation)
- **테스트 방법:**
  - 유닛 테스트 (JUnit)
  - 통합 테스트 (Spring Boot Test)
- **API 문서화 도구:**
  - Swagger를 이용한 API 문서화 및 테스트
