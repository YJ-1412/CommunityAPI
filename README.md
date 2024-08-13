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
  - **사용자 관리(User Management):** 회원가입, 로그인, JWT 토큰 재발급, 수정, 삭제
  - **역할 관리(Role Management):** 역할 생성, 조회, 수정, 삭제
  - **게시판 관리(Board Management):** 게시판 생성, 조회, 수정, 삭제
  - **게시글 관리(Post Management):** 게시글 작성, 조회, 수정, 삭제
  - **댓글 관리(Comment Management):** 댓글 작성, 조회, 수정, 삭제
  - **보안(Security):** 인증, JWT 발급, JWT 검증, 인가
  - **예외 처리(Exception Handling):** 예외 처리, HTTP 실패 코드 반환
  - **로깅 / 모니터링(Logging / Monitoring):** 함수 호출 및 종료 로그 기록

## 엔드포인트 (Endpoints)
### 1. 사용자 관리(User Management)
- **회원가입**
  - **URI:** `/register`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "username": "example",
      "password": "password123"
    }
    ```
    ```
    - `username` (string): 사용자의 이름입니다. 공백이 아니어야 하며, 기존 사용자와 중복될 수 없습니다.
    - `password` (stirng): 사용자의 비밀번호입니다. 공백이 아니어야 하며, 8자에서 20자 사이의 길이를 가져야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example",
      "role": {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      "isStaff": false,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다.
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    ```

- **로그인**
  - **URI:** `/login`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "username": "example",
      "password": "password123"
    }
    ```
    ```
    - `username` (string): 사용자의 이름입니다.
    - `password` (string): 사용자의 비밀번호입니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjE4NCwiZXhwIjoxNzIzMDI1Nzg0fQ.Nmh6_2fOsU3YQXe3tu3Ky4_D7IqpM6TlgEfg0IYqu_8",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjE4NCwiZXhwIjoxNzIzNjI2OTg0fQ.dKrhhdCPBULmL_n0AHrONpTDF0eJPz9ziyw1IE0Sv_c"
    }
    ```
    ```
    - `accessToken` (string): 서버에서 발급한 JWT입니다. 이후 요청에서 사용자의 인증을 위하여 API 요청의 Authorization 헤더에 포함되어 사용됩니다. 유효기간은 1시간입니다. 
    - `refreshToken` (stirng): `accessToken`의 유효 기간이 만료되었을 때, 새로운 `accessToken`을 발급받기 위하여 사용되는 토큰입니다. 유효기간은 7일입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다. 
    - 바르지 않은 `username`이나 `password`로 로그인을 시도하면 `401 Unauthorized` 응답이 반환됩니다. 
    ```
    
- **JWT 토큰 재발급**
  - **URI:** `/refresh-token`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjE4NCwiZXhwIjoxNzIzNjI2OTg0fQ.dKrhhdCPBULmL_n0AHrONpTDF0eJPz9ziyw1IE0Sv_c"
    }
    ```
    ```
    - `refreshToken` (string): `accessToken`의 유효 기간이 만료되었을 때, 새로운 `accessToken`을 발급받기 위하여 사용되는 토큰입니다.
    ```
  - **응답 형식:**
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjQ5OCwiZXhwIjoxNzIzMDI2MDk4fQ.MWF74zjubefaiEI0jZkgChLHRmJkNBvbEAZMt2OuqY8",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjQ5OCwiZXhwIjoxNzIzNjI3Mjk4fQ.eHH2Itdpy4Exp9qBSzEHrugFHIctXhhn2nqxwFOppeg"
    }
    ```
    ```
    - `accessToken` (string): 서버에서 새롭게 발급한 `accessToken`입니다.
    - `refreshToken` (stirng): 서버에서 새롭게 발급한 `refreshToken`입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다.
    - 유효하지 않은 `refreshToken`으로 요청하면 `401 Unauthorized` 응답이 반환됩니다.
    ```
    
- **사용자 조회**
  - **URI:** `/users/{userId}`
  - **HTTP 메서드:** GET
  - **경로 변수:**
    ```
    - `userId` (integer): 조회할 사용자의 고유 ID
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example",
      "role": {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      "isStaff": false,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다.
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    ```

- **사용자 정보 수정**
  - **URI:** `/users/{userId}`
  - **HTTP 메서드:** PUT
  - **경로 변수:**
    ```
    - `userId` (integer): 수정할 사용자의 고유 ID
    ```
  - **요청 형식:**
    ```json
    {
      "username": "example2",
      "password": "password456"
    }
    ```
    ```
    - `username` (string): 사용자의 이름입니다. 공백이 아니어야 하며, 다른 사용자와 중복될 수 없습니다.
    - `password` (stirng): 사용자의 비밀번호입니다. 공백이 아니어야 하며, 8자에서 20자 사이의 길이를 가져야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example2",
      "role": {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      "isStaff": false,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 사용자 자신의 정보만 수정 가능합니다.
    - 요청자와 수정 대상이 일치하지 않으면 `403 Forbidden` 응답이 반환됩니다. 
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    - 사용자 정보 수정에 성공하면 기존에 발급한 refreshToken이 무효화됩니다. 
    ```

- **사용자 역할 수정**
  - **URI:** `/users/{userId}/role`
  - **HTTP 메서드:** PATCH
  - **경로 변수:**
    ```
    - `userId` (integer): 수정할 사용자의 고유 ID
    ```
  - **요청 형식:**
    ```json
    {
      "roleId": "2"
    }
    ```
    ```
    - `roleId` (integer): 변경할 역할의 고유 ID입니다. 기존 역할과 달라야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example",
      "role": {
        "id": 2,
        "name": "LV1",
        "level": 1
      },
      "isStaff": false,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다. 
    - 관리자나 스태프가 아닌 사용자가 접근하면 `403 Forbidden` 응답이 반환됩니다.
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    - 존재하지 않는 `roleId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    - 기존 역할과 같은 `roleId`를 입력하면 `409 Conflict` 응답이 반환됩니다. 
    ```

- **스태프 권한 부여**
  - **URI:** `/staff`
  - **HTTP 메서드:** POST
  - **요청 형식:**
    ```json
    {
      "userId": "3"
    }
    ```
    ```
    - `userId` (integer): 스태프 권한을 부여할 사용자의 고유 ID
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example",
      "role": {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      "isStaff": true,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다. 
    - 일반 사용자만을 대상으로 요청할 수 있습니다.
    - 관리자가 아닌 사용자가 접근하면 `403 Forbidden` 응답을 반환합니다.
    - 관리자를 대상으로 요청하면 `403 Forbidden` 응답을 반환합니다.
    - 존재하지 않는 `userId`를 입력하면 `403 Forbidden` 응답이 반환됩니다.
    - 이미 스태프인 사용자를 대상으로 요청하면 `409 Conflict` 응답이 반환됩니다.
    ```

- **스태프 권한 박탈**
  - **URI:** `/staff/{userId}`
  - **HTTP 메서드:** DELETE
  - **경로 변수:**
    ```
    - `userId` (integer): 스태프 권한을 박탈할 사용자의 고유 ID
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "username": "example",
      "role": {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      "isStaff": false,
      "isAdmin": false,
      "writtenPostCount": 0,
      "writtenCommentCount": 0,
      "likedPostCount": 0
    }
    ```
    ```
    - `id` (integer): 사용자의 고유 ID입니다.
    - `username` (string): 사용자의 이름입니다.
    - `role` (object): 사용자의 역할을 나타내는 객체입니다.
      - `role.id` (integer): 역할의 고유 ID입니다.
      - `role.name` (string): 역할의 이름입니다.
      - `role.level` (integer): 역할의 권한 수준을 나타내는 값입니다.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (integer): 사용자가 작성한 게시글의 수입니다.
    - `writtenCommentCount` (integer): 사용자가 작성한 댓글의 수입니다.
    - `likedPostCount` (integer): 사용자가 좋아요를 누른 게시글의 수입니다.
    ```
    - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다. 
    - 스태프만을 대상으로 요청할 수 있습니다.
    - 관리자가 아닌 사용자가 접근하면 `403 Forbidden` 응답을 반환합니다.
    - 관리자를 대상으로 요청하면 `403 Forbidden` 응답을 반환합니다.
    - 존재하지 않는 `userId`를 입력하면 `403 Forbidden` 응답이 반환됩니다.
    - 일반 사용자를 대상으로 요청하면 `409 Conflict` 응답이 반환됩니다.
    ```

- **사용자 삭제**
  - **URI:** `/users/{userId}`
  - **HTTP 메서드:** DELETE
  - **경로 변수:**
    ```
    - `userId` (integer): 삭제할 사용자의 고유 ID
    ```
  - **추가 설명:**
    ```
    - 인증이 필요합니다.
    - 관리자는 자기 자신을 제외한 모든 사용자를 삭제할 수 있습니다.
    - 스태프는 자기 자신과 모든 일반 사용자를 삭제할 수 있습니다.
    - 일반 사용자는 자기 자신만을 삭제할 수 있습니다. 
    - 위에서 언급한 조건을 충족하지 못하면 `403 Forbidden` 응답이 반환됩니다.
    - 존재하지 않는 `userId`를 입력하면 `403 Forbidden` 응답이 반환됩니다.
    ```
### 2. 역할 관리(Role Management)
### 3. 게시판 관리(Board Management)
### 4. 게시글 관리(Post Management)
### 5. 댓글 관리(Comment Management)
- **기능**
  - **URI:** `/`
  - **HTTP 메서드:**
  - **경로 변수:**
    ```
    ```
  - **요청 형식:**
    ```json
    {
      
    }
    ```
    ```
    ```
  - **응답 형식:**
    ```json
    {
      
    }
    ```
    ```
    ```
  - **추가 설명:**
    ```
    ```

## 인증 및 보안 (Authentication & Security)
- **인증 방식:** JWT (JSON Web Tokens)
- **권한 관리:** 사용자 등급과 역할에 따른 접근 제어 (등급: 사용자/스태프/관리자)

## 에러 처리 (Error Handling)
- **에러 코드와 메시지:**
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패
  - `403 Forbbiden`: 권한 없음
  - `404 Not Found`: 리소스를 찾을 수 없음
  - `409 Conflict`: 기존 상태와 충돌
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
