# Community API

## 1. API 개요 (Overview)
### 1. 프로젝트 배경
### 2. 프로젝트 목표
### 3. 주요 기능

## 2. 기술 스택 (Technical Stack)
- **프로그래밍 언어:** Kotlin
- **프레임워크:** Spring Boot, Spring Security
- **데이터베이스:** MySQL
- **기타 기술:** JPA

## 3. 시스템 아키텍처 (System Architecture)
![Component Diagram]
- **레이어드 아키텍쳐:** 
- **주요 컴포넌트:**
  - **사용자 관리(User Management):** 회원가입, 로그인, JWT 토큰 재발급, 수정, 삭제
  - **역할 관리(Role Management):** 역할 생성, 조회, 수정, 삭제
  - **게시판 관리(Board Management):** 게시판 생성, 조회, 수정, 삭제
  - **게시글 관리(Post Management):** 게시글 작성, 조회, 수정, 삭제
  - **댓글 관리(Comment Management):** 댓글 작성, 조회, 수정, 삭제
  - **보안(Security):** 인증, JWT 발급, JWT 검증, 인가
  - **예외 처리(Exception Handling):** 예외 처리, HTTP 실패 코드 반환
  - **로깅 / 모니터링(Logging / Monitoring):** 함수 호출 및 종료 로그 기록

## 4. 데이터베이스 (Database)
![ERD](./docs/ERD.png)

## 5. 초기 설정 (Initial State)
### 기본 관리자 계정
- **username:** `Admin`
- **password:** `00000000`

**Note:** `초기 비밀번호는 반드시 첫 로그인 후 변경해야 합니다.`

### 기본 역할
- **name:** `LV0`
- **level:** `0`

## 6. 기능 상세 설명
### 1. 사용자 관리
### 2. 역할 관리
### 3. 게시판 관리
### 4. 게시글 관리
### 5. 댓글 관리

## 7. 인증 및 보안 (Authentication & Security)
- **인증 방식:** JWT (JSON Web Tokens)
- **권한 관리:** 사용자 등급과 역할에 따른 접근 제어 (등급: 사용자/스태프/관리자)

## 8. 에러 처리 (Error Handling)
- **에러 코드와 메시지:**
  - `400 Bad Request`: 잘못된 요청
  - `401 Unauthorized`: 인증 실패
  - `403 Forbbiden`: 권한 없음
  - `404 Not Found`: 리소스를 찾을 수 없음
  - `409 Conflict`: 기존 상태와 충돌
  - `500 Internal Server Error`: 서버 내부 오류
- **예외 처리 방식:**
  - 글로벌 예외 처리기 (`@ControllerAdvice`)를 사용하여 예외를 처리하고, 사용자에게 적절한 메시지 반환

## 9. API 엔드포인트 (API Endpoints)
### 1. 사용자 관리(User Management)
- **회원가입**
  - **URI:** `/register`
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /register
    ```
    ```json
    {
      "username": "example",
      "password": "password123"
    }
    ```
    ```
    - `username` (string): 사용자의 이름. 공백이 아니어야 하며, 기존 사용자와 중복될 수 없습니다.
    - `password` (stirng): 사용자의 비밀번호. 공백이 아니어야 하며, 8자에서 20자 사이의 길이를 가져야 합니다. 
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다.
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    ```

- **로그인**
  - **URI:** `/login`
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /login
    ```
    ```json
    {
      "username": "example",
      "password": "password123"
    }
    ```
    ```
    - `username` (string): 사용자의 이름.
    - `password` (string): 사용자의 비밀번호. 
    ```
  - **응답 형식:**
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjE4NCwiZXhwIjoxNzIzMDI1Nzg0fQ.Nmh6_2fOsU3YQXe3tu3Ky4_D7IqpM6TlgEfg0IYqu_8",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwidXNlcm5hbWUiOiJleGFtcGxlIiwicm9sZSI6IntcImlkXCI6MSxcIm5hbWVcIjpcIlVzZXJcIixcImxldmVsXCI6MH0iLCJpc1N0YWZmIjpmYWxzZSwiaXNBZG1pbiI6ZmFsc2UsImlhdCI6MTcyMzAyMjE4NCwiZXhwIjoxNzIzNjI2OTg0fQ.dKrhhdCPBULmL_n0AHrONpTDF0eJPz9ziyw1IE0Sv_c"
    }
    ```
    ```
    - `accessToken` (string): 서버에서 발급한 JWT. 이후 요청에서 사용자의 인증을 위하여 API 요청의 Authorization 헤더에 포함되어 사용됩니다. 유효기간은 1시간입니다. 
    - `refreshToken` (stirng): `accessToken`의 유효 기간이 만료되었을 때, 새로운 `accessToken`을 발급받기 위하여 사용되는 토큰입니다. 유효기간은 7일입니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다. 
    - 바르지 않은 `username`이나 `password`로 로그인을 시도하면 `401 Unauthorized` 응답이 반환됩니다. 
    ```
    
- **JWT 토큰 재발급**
  - **URI:** `/refresh-token`
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /refresh-token
    ```
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
    - `accessToken` (string): 서버에서 새롭게 발급한 `accessToken`.
    - `refreshToken` (stirng): 서버에서 새롭게 발급한 `refreshToken`.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하지 않으며, 누구나 접근할 수 있습니다.
    - 유효하지 않은 `refreshToken`으로 요청하면 `401 Unauthorized` 응답이 반환됩니다.
    ```

- **프로필 요청**
  - **URI:** `/profile`
  - **HTTP 메서드:** `GET`
  - **요청 형식:**
    ```http request
    GET /profile
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
      "isAdmin": false
    }
    ```
    ```
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다. 
    ```
    
- **사용자 조회**
  - **URI:** `/users/{userId}`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `userId` (number): 조회할 사용자의 ID
    ```
  - **요청 형식:**
    ```http request
    GET /users/3
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다.
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    ```

- **사용자 정보 수정**
  - **URI:** `/users/{userId}`
  - **HTTP 메서드:** `PUT`
  - **경로 변수:**
    ```
    - `userId` (number): 수정할 사용자의 ID
    ```
  - **요청 형식:**
    ```http request
    PUT /users/3
    ```
    ```json
    {
      "username": "example2",
      "password": "password456"
    }
    ```
    ```
    - `username` (string): 사용자의 이름. 공백이 아니어야 하며, 다른 사용자와 중복될 수 없습니다.
    - `password` (stirng): 사용자의 비밀번호. 공백이 아니어야 하며, 8자에서 20자 사이의 길이를 가져야 합니다. 
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
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
  - **HTTP 메서드:** `PATCH`
  - **경로 변수:**
    ```
    - `userId` (number): 수정할 사용자의 ID
    ```
  - **요청 형식:**
    ```http request
    PATCH /users/3/role
    ```
    ```json
    2
    ```
    ```
    - `roleId` (number): 변경할 역할의 ID. 기존 역할과 달라야 합니다. 
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
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
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /staff
    ```
    ```json
    3
    ```
    ```
    - `userId` (number): 스태프 권한을 부여할 사용자의 ID
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
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
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `userId` (number): 스태프 권한을 박탈할 사용자의 ID
    ```
  - **요청 형식:**
    ```http request
    DELETE /staff/3
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
    - `id` (number): 사용자의 ID.
    - `username` (string): 사용자의 이름.
    - `role` (object): 사용자의 역할.
      - `id` (number): 역할의 ID.
      - `name` (string): 역할의 이름.
      - `level` (number): 역할의 권한 수준을 나타내는 값.
    - `isStaff` (boolean): 사용자가 스태프인지 여부를 나타냅니다.
    - `isAdmin` (boolean): 사용자가 관리자인지 여부를 나타냅니다.
    - `writtenPostCount` (number): 사용자가 작성한 게시글의 수.
    - `writtenCommentCount` (number): 사용자가 작성한 댓글의 수.
    - `likedPostCount` (number): 사용자가 좋아요를 누른 게시글의 수.
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
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `userId` (number): 삭제할 사용자의 ID
    ```
  - **요청 형식:**
    ```http request
    DELETE /users/3
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
- **역할 생성**
  - **URI:** `/roles`
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /roles
    ```
    ```json
    {
      "name": "LV1",
      "level": 1
    }
    ```
    ```
    - `name` (string): 새로운 역할의 이름. 공백이 아니어야 하며, 기존 역할과 중복될 수 없습니다.
    - `level` (number): 새로운 역할의 레벨. null이 아니어야 하며, 0보다 크거나 같은 정수여야 하고, 기존 역할과 중복될 수 없습니다.
    ```
  - **응답 형식:**
    ```json
    {
      "id": 2,
      "name": "LV1",
      "level": 1
    }
    ```
    ```
    - `id` (number): 역할의 ID.
    - `name` (string): 역할의 이름.
    - `level` (number): 역할의 레벨.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    ```

- **역할 전체 조회**
  - **URI:** `/roles`
  - **HTTP 메서드:** `GET`
  - **요청 형식:**
    ```http request
    GET /roles
    ```
  - **응답 형식:**
    ```json
    [
      {
        "id": 1,
        "name": "LV0",
        "level": 0
      },
      {
        "id": 2,
        "name": "LV1",
        "level": 1
      }
    ]
    ```
    ```
    - 모든 역할의 리스트가 반환된다. 리스트는 `level`의 오름차순으로 정렬되어 있다.
    - `id` (number): 역할의 ID. 
    - `name` (string): 역할의 이름. 
    - `level` (number): 역할의 레벨. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다. 
    ```

- **역할 수정**
  - **URI:** `/roles/{roleId}`
  - **HTTP 메서드:** `PUT`
  - **경로 변수:**
    ```
    - `roleId` (number): 수정하려는 역할의 ID.
    ```
  - **요청 형식:**
    ```http request
    PUT /roles/2
    ```
    ```json
    {
      "name": "LV2",
      "level": 2
    }
    ```
    ```
    - `name` (string): 역할의 새로운 이름. 공백이 아니어야 하며, 다른 역할과 중복될 수 없습니다.
    - `level` (number): 역할의 새로운 레벨. null이 아니어야 하며, 0보다 크거나 같은 정수여야 하고, 다른 역할과 중복될 수 없습니다.
    ```
  - **응답 형식:**
    ```json
    {
      "id": 2,
      "name": "LV2",
      "level": 2
    }
    ```
    ```
    - `id` (number): 변경된 역할의 ID. 
    - `name` (string): 변경된 역할의 이름. 
    - `level` (number): 변경된 역할의 레벨.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다.
    - 존재하지 않는 `roleId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다.   
    ```

- **역할 삭제 및 기본 역할에 이동**
  - **URI:** `/roles/{roleId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:** 
    ```
    - `roleId` (number): 삭제하려는 역할의 ID.
    ```
  - **요청 형식:**
    ```http request
    DELETE /roles/2
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "name": "LV0", 
      "level": 0
    }
    ```
    ```
    - 기본 역할(가장 낮은 레벨의 역할)이 반환됩니다.
    - `id` (number): 기본 역할의 ID. 
    - `name` (string): 기본 역할의 이름. 
    - `level` (number): 기본 역할의 레벨.
    ```
  - **추가 설명:**
    ```
    - 이 엔드포인트로 역할을 삭제하면, 해당 역할과 연결된 모든 게시판과 사용자를 전부 기본 역할(삭제할 역할을 제외한 역할 중 레벨이 가장 낮은 역할)로 이동시킵니다.
    - 인증이 필요하며, 관리자 권한이 필요합니다.
    - 존재하지 않는 `roleId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    - 역할은 최소 1개 이상 존재해야 합니다. 마지막 역할을 삭제하려고 시도하면 `409 Conflict` 응답이 반환됩니다.
    ```

- **역할 삭제 및 다른 역할에 이동**
  - **URI:** `/roles/{sourceRoleId}/transfer/{targetRoleId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `sourceRoleId` (number): 삭제하려는 역할의 ID.
    - `targetRoleId` (number): 삭제하려는 역할과 연결된 게시판과 사용자를 이동시킬 목표 역할의 ID.
    ```
  - **요청 형식:**
    ```http request
    DELETE /roles/2/transfer/3
    ```
  - **응답 형식:**
    ```json
    {
      "id": 3,
      "name": "New LV1", 
      "level": 1
    }
    ```
    ```
    - 목표 역할이 반환됩니다.
    - `id` (number): 목표 역할의 ID. 
    - `name` (string): 목표 역할의 이름. 
    - `level` (number): 목표 역할의 레벨.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다. 
    - `sourceRoleId`와 `targetRoleId`가 같으면 `400 Bad Request` 응답이 반환됩니다. 
    - 존재하지 않는 `sourceRoleId`와 `targetRoleId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    ```

- **역할 일괄 수정**
  - **URI:** `/roles`
  - **HTTP 메서드:** `PUT`
  - **요청 형식:**
    ```http request
    PUT /roles
    ```
    ```json
    {
      "updates": [
        {
          "first": 1,
          "second": {
            "name": "Updated LV0",
            "level": 0
          }
        },
        {
          "first": 4,
          "second": {
            "name": "Updated LV3",
            "level": 3
          }
        }
      ],
      "creates": [
        {
          "name": "New LV1",
          "level": 1
        },
        {
          "name": "New LV2",
          "level": 2
        }
      ],
      "moves": [
        {
          "first": 2,
          "second": 1
        },
        {
          "first": 3,
          "second": 1
        }
      ]
    }

    ```
    ```
    - `updates` (array): 역할을 수정하는 요청의 리스트. 각 항목은 수정할 역할의 ID와 새로운 역할 데이터를 포함합니다.
      - `first` (number): 수정할 역할의 ID. 
      - `second` (object): 역할 수정 요청 DTO. 
        - `name` (string): 수정할 역할의 새로운 이름. 
        - `level` (number): 수정할 역할의 새로운 레벨. 
    - `creates` (array): 새로운 역할을 생성하는 요청의 리스트. 
      - `name` (string): 새로운 역할의 이름.
      - `level` (number): 새로운 역할의 레벨. 
    - `moves` (array): 역할을 삭제하고 연관 데이터를 이동시키는 요청의 리스트. 각 항목은 삭제할 역할의 ID와 데이터를 이동시킬 목표 역할의 ID를 포함합니다.
      - `first` (number): 삭제할 역할의 ID.
      - `second` (number): 삭제할 역할의 연관 데이터(게시판, 사용자)를 이동시킬 목표 역할의 ID.
    ```
  - **응답 형식:**
    ```json
    [
      {
        "id": 1,
        "name": "Updated LV0",
        "level": 0
      },
      {
        "id": 5,
        "name": "New LV1",
        "level": 1
      },
      {
        "id": 6,
        "name": "New LV2",
        "level": 2
      },
      {
        "id": 4,
        "name": "Updated LV3",
        "level": 3
      }
    ]
    ```
    ```
    - 모든 역할의 리스트가 반환됩니다. 리스트는 `level`의 오름차순으로 정렬되어 있습니다.
    - `id` (number): 역할의 ID. 
    - `name` (string): 역할의 이름. 
    - `level` (number): 역할의 레벨. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 권한이 필요합니다. 
    - 일괄요청에 포함된 요청 중 하나라도 실패할 경우, 일괄요청에 포함된 모든 요청이 실패하며 실패한 요청의 실패사유에 해당하는 응답이 반환됩니다. 
    ```
### 3. 게시판 관리(Board Management)
- **게시판 생성**
  - **URI:** `/boards`
  - **HTTP 메서드:** `POST`
  - **요청 형식:**
    ```http request
    POST /boards
    ```
    ```json
    {
      "name": "Board 1",
      "priority": 0, 
      "readableRoleId": 1
    }
    ```
    ```
    - `name` (string): 새로운 게시판의 이름. 공백이 아니어야 하며, 다른 게시판과 중복될 수 없습니다.
    - `priority` (number): 새로운 게시판의 정렬 우선순위(오름차순). null이 아니어야 하며, 0보다 크거나 같은 정수여야 하고, 다른 게시판과 중복될 수 없습니다. 
    - `readableRoleId` (number): 새로운 게시판의 최소 열람 가능 역할. null이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "name": "Board 1",
      "priority": 0,
      "readableRoleId": 1,
      "postCount": 0
    }
    ```
    ```
    - 생성된 게시판의 정보가 반환됩니다. 
    - `id` (number): 게시판의 ID. 
    - `name` (string): 게시판의 이름. 
    - `priority` (number): 게시판의 정렬 우선순위. 
    - `readableRoleId` (number): 게시판의 최소 열람 가능 역할. 
    - `postCount` (number): 게시판에 등록된 게시글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다.
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    - 존재하지 않는 `readableRoleId`를 입력하면 `404 Not Found` 응답이 반환됩니다. 
    ```

- **게시판 전체 조회**
  - **URI:** `/boards`
  - **HTTP 메서드:** `GET`
  - **요청 형식:**
    ```http request
    GET /boards
    ```
  - **응답 형식:**
    ```json
    [
      {
        "id": 1,
        "name": "Board 1",
        "priority": 0,
        "readableRoleId": 1, 
        "postCount": 0
      }, 
      {
        "id": 2,
        "name": "Board 2",
        "priority": 1,
        "readableRoleId": 1, 
        "postCount": 2
      }
    ]
    ```
    ```
    - 모든 게시판의 리스트가 반환됩니다. 리스트는 `priority`의 오름차순으로 정렬되어 있습니다.  
    - `id` (number): 게시판의 ID. 
    - `name` (string): 게시판의 이름. 
    - `priority` (number): 게시판의 정렬 우선순위. 
    - `readableRoleId` (number): 게시판의 최소 열람 가능 역할. 
    - `postCount` (number): 게시판에 등록된 게시글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다.
    ```

- **게시판 수정**
  - **URI:** `/boards/{boardId}`
  - **HTTP 메서드:** `PUT`
  - **경로 변수:**
    ```
    - `boardId` (number): 수정할 게시판의 ID. 
    ```
  - **요청 형식:**
    ```http request
    PUT /boards/2
    ```
    ```json
    {
      "name": "Updated Board 2",
      "priority": 2, 
      "readableRoleId": 2
    }
    ```
    ```
    - `name` (string): 수정할 게시판의 새로운 이름. 공백이 아니어야 하며, 다른 게시판과 중복될 수 없습니다.
    - `priority` (number): 수정할 게시판의 새로운 정렬 우선순위(오름차순). null이 아니어야 하며, 0보다 크거나 같은 정수여야 하고, 다른 게시판과 중복될 수 없습니다. 
    - `readableRoleId` (number): 수정할 게시판의 새로운 최소 열람 가능 역할. null이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 2,
      "name": "Updated Board 2",
      "priority": 2,
      "readableRoleId": 2,
      "postCount": 2
    }
    ```
    ```
    - 수정된 게시판의 정보가 반환됩니다. 
    - `id` (number): 게시판의 ID. 
    - `name` (string): 게시판의 이름. 
    - `priority` (number): 게시판의 정렬 우선순위. 
    - `readableRoleId` (number): 게시판의 최소 열람 가능 역할. 
    - `postCount` (number): 게시판에 등록된 게시글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다.
    - 존재하지 않는 `boardId`를 입력하면 `404 Not Found` 응답이 반환됩니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답이 반환됩니다. 
    - 존재하지 않는 `readableRoleId`를 입력하면 `404 Not Found` 응답이 반환됩니다. 
    ```

- **게시판 삭제**
  - **URI:** `/boards/{boardId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `boardId` (number): 삭제할 게시판의 ID. 
    ```
  - **요청 형식:**
    ```http request
    DELETE /boards/2
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다. 
    - 존재하지 않는 `boardId`를 입력하면 `404 Not Found` 응답이 반환됩니다. 
    ```

- **게시판 삭제 및 다른 게시판에 게시글 이동**
  - **URI:** `/boards/{sourceBoardId}/posts/transfer/{targetBoardId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `sourceBoardId` (number): 
    - `targetBoardId` (number): 
    ```
  - **요청 형식:**
    ```http request
    DELETE /boards/2/posts/tranfer/1
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "name": "Board 1",
      "priority": 0,
      "readableRoleId": 1,
      "postCount": 2
    }
    ```
    ```
    - 목표 게시판이 반환됩니다. 
    - `id` (number): 게시판의 ID. 
    - `name` (string): 게시판의 이름. 
    - `priority` (number): 게시판의 정렬 우선순위. 
    - `readableRoleId` (number): 게시판의 최소 열람 가능 역할. 
    - `postCount` (number): 게시판에 등록된 게시글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다.
    - 삭제한 게시판에 등록되어 있던 모든 게시글을 목표 게시판으로 이동시킵니다. 
    - `sourceBoardId`와 `targetBoardId`가 같으면 `400 Bad Request` 응답이 반환됩니다. 
    - 존재하지 않는 `sourceBoardId`와 `targetBoardId`를 입력하면 `404 Not Found` 응답이 반환됩니다.
    ```

- **게시판 일괄 수정**
  - **URI:** `/boards`
  - **HTTP 메서드:** `PUT`
  - **요청 형식:**
    ```http request
    PUT /boards
    ```
    ```json
    {
      "updates": [
        {
          "first": 1,
          "second": {
            "name": "Updated Board 1",
            "priority": 0,
            "readableRoleId": 1
          }
        },
        {
          "first": 2,
          "second": {
            "name": "Updated Board 2",
            "priority": 2,
            "readableRoleId": 2
          }
        }
      ],
      "creates": [
        {
          "name": "New Board 1",
          "priority": 3,
          "readableRoleId": 1
        },
        {
          "name": "New Board 2",
          "priority": 1,
          "readableRoleId": 2
        }
      ],
      "deletes": [ 3, 4 ],
      "moves": [
        {
          "first": 5,
          "second": 1
        },
        {
          "first": 6,
          "second": 2
        }
      ]
    }
    ```
    ```
    - `updates` (array): 게시판을 수정하는 요청의 리스트. 각 항목은 수정할 게시판의 ID와 새로운 게시판 데이터를 포함합니다.
      - `first` (number): 수정할 게시판의 ID. 
      - `second` (object): 게시판 수정 요청 DTO. 
        - `name` (string): 수정할 게시판의 새로운 이름. 
        - `priority` (number): 수정할 게시판의 새로운 정렬 우선순위(오름차순).
        - `readableRoleId` (number): 수정할 게시판의 새로운 최소 열람 가능 역할.
    - `creates` (array): 새로운 게시판을 생성하는 요청의 리스트. 
      - `name` (string): 새로운 게시판의 이름.
      - `priority` (number): 새로운 게시판의 정렬 우선순위(오름차순).
      - `readableRoleId` (number): 새로운 게시판의 최소 열람 가능 역할. 
    - `deletes` (array): 게시판을 삭제하는 요청의 리스트
    - `moves` (array): 게시판을 삭제하고 등록된 게시글을 이동시키는 요청의 리스트. 각 항목은 삭제할 게시판의 ID와 게시글을 이동시킬 목표 게시판의 ID를 포함합니다.
      - `first` (number): 삭제할 게시판의 ID.
      - `second` (number): 삭제할 게시판에 등록된 게시글을 이동시킬 목표 게시판의 ID.
    ```
  - **응답 형식:**
    ```json
    [
      {
        "id": 1,
        "name": "Updated Board 1",
        "priority": 0,
        "readableRoleId": 1, 
        "postCount": 3
      }, 
      {
        "id": 8,
        "name": "New Board 2",
        "priority": 1,
        "readableRoleId": 2, 
        "postCount": 0
      },
      {
        "id": 2,
        "name": "Updated Board 2",
        "priority": 2,
        "readableRoleId": 1, 
        "postCount": 4
      },
      {
        "id": 7,
        "name": "New Board 1",
        "priority": 3,
        "readableRoleId": 1, 
        "postCount": 0
      }
    ]
    ```
    ```
    - 모든 게시판의 리스트가 반환됩니다. 리스트는 `priority`의 오름차순으로 정렬되어 있습니다.  
    - `id` (number): 게시판의 ID. 
    - `name` (string): 게시판의 이름. 
    - `priority` (number): 게시판의 정렬 우선순위. 
    - `readableRoleId` (number): 게시판의 최소 열람 가능 역할. 
    - `postCount` (number): 게시판에 등록된 게시글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한이 필요합니다. 
    - 일괄요청에 포함된 요청 중 하나라도 실패할 경우, 일괄요청에 포함된 모든 요청이 실패하며 실패한 요청의 실패사유에 해당하는 응답이 반환됩니다. 
    ```
### 4. 게시글 관리(Post Management)
- **게시글 작성**
  - **URI:** `/boards/{boardId}/posts`
  - **HTTP 메서드:** `POST`
  - **경로 변수:**
    ```
    - `boardId` (number): 새로운 게시글이 등록될 게시판의 ID. 
    ```
  - **요청 형식:**
    ```http request
    POST /boards/1/posts
    ```
    ```json
    {
      "title": "Post 1",
      "content": "Content 1"
    }
    ```
    ```
    - `title` (string): 새로운 게시글의 제목. 공백이 아니어야 합니다. 
    - `content` (string): 새로운 게시글의 본문. 공백이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post 1",
      "content": "Content 1",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      }, 
      "board": {
        "id": 1,
        "name": "Board 1", 
        "priority": 0,
        "readableRole": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "postCount": 1
      },
      "createdDate": "2024-08-20T17:34:30.4288789",
      "viewCount": 0,
      "likeCount": 0,
      "commentCount": 0
    }
    ```
    ```
    - 새로운 게시글의 정보를 반환한다. 
    - `id` (number): 게시글의 ID. 
    - `title` (string): 게시글의 제목. 
    - `content` (string): 게시글의 본문. 
    - `author` (object): 게시글의 작성자 정보. 
    - `board` (object): 게시글이 등록되어 있는 게시판 정보. 
    - `createdDate` (string, ISO 8601): 게시글이 생성된 날짜 및 시간. 
    - `viewCount` (number): 게시글의 조회수. 
    - `likeCount` (number): 게시글을 좋아요한 사용자의 수. 
    - `commentCount` (number): 게시글에 달린 댓글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프 권한을 가지고 있거나 게시판의 최소 가능 열람 역할의 레벨보다 사용자의 역할의 레벨이 더 높아야 합니다.
    - 존재하지 않는 `boardId`를 입력하면 `403 Forbidden` 응답을 반환합니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답을 반환합니다.   
    ```

- **게시글 요약 전체 조회**
  - **URI:** `/posts`
  - **HTTP 메서드:** `GET`
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `20`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /posts
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 2,
          "title": "Post 2",
          "author": {
            "id": 3,
            "username": "User 2",
            "role": {
              "id": 2,
              "name": "LV1",
              "level": 1
            },
            "isStaff": false,
            "isAdmin": false
          },
          "board": {
            "id": 2,
            "name": "Board 2",
            "priority": 1,
            "readableRole": {
              "id": 2,
              "name": "LV1",
              "level": 1
            },
            "postCount": 1
          },
          "createdDate": "2024-08-20T20:37:04.428879",
          "viewCount": 0,
          "likeCount": 0,
          "commentCount": 0
        },
        {
          "id": 1,
          "title": "Post 1",
          "author": {
            "id": 2,
            "username": "User 1",
            "role": {
              "id": 1,
              "name": "LV0",
              "level": 0
            },
            "isStaff": false,
            "isAdmin": false
          },
          "board": {
            "id": 1,
            "name": "Board 1",
            "priority": 0,
            "readableRole": {
              "id": 1,
              "name": "LV0",
              "level": 0
            },
            "postCount": 1
          },
          "createdDate": "2024-08-20T17:34:30.109282",
          "viewCount": 0,
          "likeCount": 0,
          "commentCount": 0
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 2,
      "totalPages": 1,
      "size": 20,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 2,
      "empty": false
    }
    ```
    ```
    - `content`: 모든 게시글 요약의 배열입니다. 배열은 생성시간의 내림차순으로 정렬되어 있습니다. 게시글 요약에는 본문이 제외되어 있습니다. 
    - `pageable`: 페이지네이션 관련 정보를 포함하는 객체입니다.
      - `pageNumber` (number): 현재 페이지 번호.
      - `pageSize` (number): 한 페이지당 표시되는 항목의 수.
      - `sort` (object): 정렬 정보. 
        - `empty` (boolean): 정렬 기준이 비어있는지 여부.
        - `sorted` (boolean): 정렬이 되었는지 여부.
        - `unsorted` (boolean): 정렬되지 않았는지 여부.
      - `offset` (number): 페이지의 시작점 오프셋.
      - `paged` (boolean): 페이지네이션이 활성화되었는지 여부.
      - `unpaged` (boolean): 페이지네이션이 비활성화되었는지 여부.
    - `last` (boolean): 마지막 페이지인지 여부를 나타냅니다.
    - `totalElements` (number): 전체 게시물 수.
    - `totalPages` (number): 전체 페이지 수.
    - `size` (number): 한 페이지에 표시되는 게시물 수
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근 가능합니다. 
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다. 
    ```

- **특정 게시판에 등록된 게시글 요약 페이지 조회**
  - **URI:** `/boards/{boardId}/posts`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `boardId` (number): 게시판의 ID. 
    ```
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `20`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /boards/1/posts
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 1,
          "title": "Post 1",
          "author": {
            "id": 2,
            "username": "User 1",
            "role": {
              "id": 1,
              "name": "LV0",
              "level": 0
            },
            "isStaff": false,
            "isAdmin": false
          },
          "board": {
            "id": 1,
            "name": "Board 1",
            "priority": 0,
            "readableRole": {
              "id": 1,
              "name": "LV0",
              "level": 0
            },
            "postCount": 1
          },
          "createdDate": "2024-08-20T17:34:30.109282",
          "viewCount": 0,
          "likeCount": 0,
          "commentCount": 0
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 20,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
    ```
    ```
    - `content`: 해당 게시판에 등록된 모든 게시글 요약의 배열입니다. 배열은 생성시간의 내림차순으로 정렬되어 있습니다. 게시글 요약에는 본문이 제외되어 있습니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근 가능합니다. 
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다.
    - 존재하지 않는 `boardId`를 입력하면 `404 Not Found` 응답을 반환합니다. 
    ```

- **특정 사용자가 작성한 게시글 요약 페이지 조회**
  - **URI:** `/users/{userId}/posts`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `userId` (number): 사용자의 ID. 
    ```
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `20`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /users/2/posts
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 2,
          "title": "Post 2",
          "author": {
            "id": 3,
            "username": "User 2",
            "role": {
              "id": 2,
              "name": "LV1",
              "level": 1
            },
            "isStaff": false,
            "isAdmin": false
          },
          "board": {
            "id": 2,
            "name": "Board 2",
            "priority": 1,
            "readableRole": {
              "id": 2,
              "name": "LV1",
              "level": 1
            },
            "postCount": 1
          },
          "createdDate": "2024-08-20T20:37:04.428879",
          "viewCount": 0,
          "likeCount": 0,
          "commentCount": 0
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 20,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
    ```
    ```
    - `content`: 해당 사용자가 작성한 모든 게시글 요약의 배열입니다. 배열은 생성시간의 내림차순으로 정렬되어 있습니다. 게시글 요약에는 본문이 제외되어 있습니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근 가능합니다. 
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다.
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답을 반환합니다. 
    ```

- **특정 사용자가 좋아요한 게시글 요약 페이지 조회**
  - **URI:** `/users/{userId}/liked-posts`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `userId` (number): 사용자의 ID. 
    ```
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `20`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /users/2/liked-posts
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 1,
          "title": "Post 1",
          "author": {
            "id": 2,
            "username": "User 1",
            "role": {
              "id": 1,
              "name": "User",
              "level": 0
            },
            "isStaff": false,
            "isAdmin": false
          },
          "board": {
            "id": 1,
            "name": "Board 1",
            "priority": 0,
            "readableRole": {
              "id": 1,
              "name": "LV0",
              "level": 0
            },
            "postCount": 1
          },
          "createdDate": "2024-08-20T17:34:30.109282",
          "viewCount": 0,
          "likeCount": 1,
          "commentCount": 0
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 20,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
    ```
    ```
    - `content`: 해당 사용자가 좋아요한 모든 게시글 요약의 배열입니다. 배열은 좋아요를 누른 순서의 내림차순으로 정렬되어 있습니다. 게시글 요약에는 본문이 제외되어 있습니다.
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근 가능합니다. 
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다.
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답을 반환합니다. 
    ```

- **게시글 조회**
  - **URI:** `/posts/{postId}`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `postId` (number): 조회하려는 게시글의 ID. 
    ```
  - **요청 형식:**
    ```http request
    GET /posts/1
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post 1",
      "content": "Content 1",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      }, 
      "board": {
        "id": 1,
        "name": "Board 1", 
        "priority": 0,
        "readableRole": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "postCount": 1
      },
      "createdDate": "2024-08-20T20:37:04.428879",
      "viewCount": 1,
      "likeCount": 0,
      "commentCount": 0
    }
    ```
    ```
    - 게시글의 정보를 반환합니다. 
    - `id` (number): 게시글의 ID. 
    - `title` (string): 게시글의 제목. 
    - `content` (string): 게시글의 본문. 
    - `author` (object): 게시글의 작성자 정보. 
    - `board` (object): 게시글이 등록되어 있는 게시판 정보. 
    - `createdDate` (string, ISO 8601): 게시글이 생성된 날짜 및 시간. 
    - `viewCount` (number): 게시글의 조회수. 
    - `likeCount` (number): 게시글을 좋아요한 사용자의 수. 
    - `commentCount` (number): 게시글에 달린 댓글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 관리자 또는 스태프이거나 게시글이 등록된 게시판의 최소 열람 가능 역할의 레벨보다 사용자의 역할의 레벨이 높아야합니다. 
    - 존재하지 않는 `postId`를 입력하면 `403 Forbidden` 응답을 반환합니다. 
    - 해당 엔드포인트에 접근하여 게시글의 정보를 반환받는데 성공하면 해당 게시글의 조회수가 +1 됩니다. 
      - 이러한 구현 방식은 GET 메소드의 멱등성을 어기므로 REST를 위배합니다.
      - REST를 엄격하게 지키는 것보다 클라이언트와 서버의 분리가 더 중요하다고 판단하여 이렇게 구현했습니다. 
    ```

- **게시글 수정**
  - **URI:** `/posts/{postId}`
  - **HTTP 메서드:** `PUT`
  - **경로 변수:**
    ```
    - `postId` (number): 수정할 게시글의 ID.
    ```
  - **요청 형식:**
    ```http request
    PUT /posts/2
    ```
    ```json
    {
      "title": "Updated Post 2", 
      "content": "Updated Content 2",
      "boardId": 1
    }
    ```
    ```
    - `title` (string): 수정할 게시글의 새로운 제목. 공백이 아니어야 합니다. 
    - `content` (string): 수정할 게시글의 새로운 본문. 공백이 아니어야 합니다. 
    - `boardId` (number): 수정할 게시글의 새로운 게시판의 ID. null이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 2,
      "title": "Updated Post 2",
      "content": "Updated Content 2",
      "author": {
        "id": 3, 
        "username": "User 2",
        "role": {
          "id": 2, 
          "name": "LV1",
          "level": 1
        },
        "isStaff": false, 
        "isAdmin": false
      }, 
      "board": {
        "id": 1,
        "name": "Board 1", 
        "priority": 0,
        "readableRole": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "postCount": 2
      },
      "createdDate": "2024-08-20T17:34:30.4288789",
      "viewCount": 1,
      "likeCount": 1,
      "commentCount": 0
    }
    ```
    ```
    - 수정된 게시글의 정보를 반환합니다. 
    - `id` (number): 게시글의 ID. 
    - `title` (string): 게시글의 제목. 
    - `content` (string): 게시글의 본문. 
    - `author` (object): 게시글의 작성자 정보. 
    - `board` (object): 게시글이 등록되어 있는 게시판 정보. 
    - `createdDate` (string, ISO 8601): 게시글이 생성된 날짜 및 시간. 
    - `viewCount` (number): 게시글의 조회수. 
    - `likeCount` (number): 게시글을 좋아요한 사용자의 수. 
    - `commentCount` (number): 게시글에 달린 댓글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 게시글의 작성자이어야 하고, 게시판을 이동시킬 경우 이동시킬 게시판에 글을 작성할 권한이 있어야 합니다. 
    - 존재하지 않는 `postId` 또는 `boardId`를 입력하면 `403 Forbidden` 응답을 반환합니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답을 반환합니다. 
    ```

- **게시글 좋아요 등록**
  - **URI:** `/posts/{postId}/liked-users`
  - **HTTP 메서드:** `POST`
  - **경로 변수:**
    ```
    - `postId` (number): 좋아요할 게시글의 ID. 
    ```
  - **요청 형식:**
    ```http request
    POST /posts/1/liked-users
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post 1",
      "content": "Content 1",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      }, 
      "board": {
        "id": 1,
        "name": "Board 1", 
        "priority": 0,
        "readableRole": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "postCount": 1
      },
      "createdDate": "2024-08-20T20:37:04.428879",
      "viewCount": 1,
      "likeCount": 1,
      "commentCount": 0
    }
    ```
    ```
    - 게시글의 정보를 반환합니다. 
    - `id` (number): 게시글의 ID. 
    - `title` (string): 게시글의 제목. 
    - `content` (string): 게시글의 본문. 
    - `author` (object): 게시글의 작성자 정보. 
    - `board` (object): 게시글이 등록되어 있는 게시판 정보. 
    - `createdDate` (string, ISO 8601): 게시글이 생성된 날짜 및 시간. 
    - `viewCount` (number): 게시글의 조회수. 
    - `likeCount` (number): 게시글을 좋아요한 사용자의 수. 
    - `commentCount` (number): 게시글에 달린 댓글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 해당 게시글의 작성자가 아니어야 하고, 해당 게시글을 읽을 권한이 필요합니다. 
    - 요청자가 이미 해당 게시글을 좋아요하고 있거나 해당 게시글의 작성자이면 `409 Conflict` 응답을 반환합니다. 
    ```

- **게시글 좋아요 취소**
  - **URI:** `/posts/{postId}/liked-users`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `postId` (number): 좋아요를 취소할 게시글의 ID.
    ```
  - **요청 형식:**
    ```http request
    DELETE /posts/1/liked-users
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "title": "Post 1",
      "content": "Content 1",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      }, 
      "board": {
        "id": 1,
        "name": "Board 1", 
        "priority": 0,
        "readableRole": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "postCount": 1
      },
      "createdDate": "2024-08-20T20:37:04.428879",
      "viewCount": 1,
      "likeCount": 0,
      "commentCount": 0
    }
    ```
    ```
    - 게시글의 정보를 반환합니다. 
    - `id` (number): 게시글의 ID. 
    - `title` (string): 게시글의 제목. 
    - `content` (string): 게시글의 본문. 
    - `author` (object): 게시글의 작성자 정보. 
    - `board` (object): 게시글이 등록되어 있는 게시판 정보. 
    - `createdDate` (string, ISO 8601): 게시글이 생성된 날짜 및 시간. 
    - `viewCount` (number): 게시글의 조회수. 
    - `likeCount` (number): 게시글을 좋아요한 사용자의 수. 
    - `commentCount` (number): 게시글에 달린 댓글의 수. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 해당 게시글을 읽을 권한이 필요합니다. 
    - 요청자가 해당 게시글을 좋아요하고 있지 않으면 `409 Conflict` 응답을 반환합니다. 
    ```

- **게시글 삭제**
  - **URI:** `/posts/{postId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `postId` (number): 삭제할 게시글의 ID.
    ```
  - **요청 형식:**
    ```http request
    DELETE /posts/1
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 해당 게시글의 작성자이거나 관리자 또는 스태프 권한이 필요합니다. 
    - 존재하지 않는 `postId`를 입력하면 `403 Forbidden` 응답이 반환됩니다.
    ```
### 5. 댓글 관리(Comment Management)
- **댓글 작성**
  - **URI:** `/posts/{postId}/comments`
  - **HTTP 메서드:** `POST`
  - **경로 변수:**
    ```
    - `postId` (number): 댓글을 작성할 게시글의 ID.
    ```
  - **요청 형식:**
    ```http request
    POST /posts/1/comments
    ```
    ```json
    {
      "content": "Post 1 - Comment 1"
    }
    ```
    ```
    - `content` (string): 새로운 댓글의 내용. 공백이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "content": "Post 1 - Comment 1",
      "createdDate": "2024-08-21T15:03:12.728634",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      },
      "post": {
        "id": 1,
        "title": "Post 1",
        "content": "Content 1",
        "author": {
          "id": 2, 
          "username": "User 1",
          "role": {
            "id": 1, 
            "name": "LV0",
            "level": 0
          },
          "isStaff": false, 
          "isAdmin": false
        }, 
        "board": {
          "id": 1,
          "name": "Board 1", 
          "priority": 0,
          "readableRole": {
            "id": 1, 
            "name": "LV0",
            "level": 0
          },
          "postCount": 1
        },
        "createdDate": "2024-08-20T20:37:04.428879",
        "viewCount": 1,
        "likeCount": 0,
        "commentCount": 1
      }
    }
    ```
    ```
    - `id` (integer): 새로운 댓글의 ID. 
    - `content` (string): 새로운 댓글이 내용. 
    - `createdDate` (string, ISO 8601): 새로운 댓글이 생성된 날짜와 시간. 
    - `author` (object): 새로운 댓글의 작성자 정보. 
    - `post` (object): 새로운 댓글이 등록된 게시글의 정보. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, `postId`가 가리키는 게시글을 읽을 권한이 필요합니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답을 반환합니다. 
    ```

- **특정 게시글에 등록된 댓글 페이지 조회**
  - **URI:** `/posts/{postId}/comments`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `postId` (number): 게시글의 ID. 
    ```
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `100`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /posts/1/comments
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 1,
          "content": "Post 1 - Comment 1",
          "createdDate": "2024-08-21T15:03:12.728634",
          "author": {
            "id": 2, 
            "username": "User 1",
            "role": {
              "id": 1, 
              "name": "LV0",
              "level": 0
            },
            "isStaff": false, 
            "isAdmin": false
          }
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 100,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 100,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
    ```
    ```
    - `content`: 해당 게시글에 등록된 모든 댓글의 배열입니다. 배열은 생성시간의 오름차순으로 정렬되어 있습니다. 댓글의 정보에는 게시글 정보가 제외되어 있습니다. 
    - `pageable`: 페이지네이션 관련 정보를 포함하는 객체입니다.
      - `pageNumber` (number): 현재 페이지 번호.
      - `pageSize` (number): 한 페이지당 표시되는 항목의 수.
      - `sort` (object): 정렬 정보. 
        - `empty` (boolean): 정렬 기준이 비어있는지 여부.
        - `sorted` (boolean): 정렬이 되었는지 여부.
        - `unsorted` (boolean): 정렬되지 않았는지 여부.
      - `offset` (number): 페이지의 시작점 오프셋.
      - `paged` (boolean): 페이지네이션이 활성화되었는지 여부.
      - `unpaged` (boolean): 페이지네이션이 비활성화되었는지 여부.
    - `last` (boolean): 마지막 페이지인지 여부를 나타냅니다.
    - `totalElements` (number): 전체 게시물 수.
    - `totalPages` (number): 전체 페이지 수.
    - `size` (number): 한 페이지에 표시되는 게시물 수
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, `postId`가 가리키는 게시글을 읽을 권한이 필요합니다. 
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다. 
    - 존재하지 않는 `postId`를 입력하면 `403 Forbidden` 응답을 반환합니다. 
    ```

- **특정 사용자가 작성한 댓글 페이지 조회**
  - **URI:** `/users/{userId}/comments`
  - **HTTP 메서드:** `GET`
  - **경로 변수:**
    ```
    - `userId` (number): 사용자의 ID.
    ```
  - **쿼리 파라미터:**
    ```
    - `page` (optional): 요청할 페이지 번호, 기본값은 `0`입니다.
    - `size` (optional): 한 페이지에 포함될 게시글의 수, 기본값은 `100`입니다.
    ```
  - **요청 형식:**
    ```http request
    GET /users/2/comments
    ```
  - **응답 형식:**
    ```json
    {
      "content": [
        {
          "id": 1,
          "content": "Post 1 - Comment 1",
          "createdDate": "2024-08-21T15:03:12.728634",
          "author": {
            "id": 2, 
            "username": "User 1",
            "role": {
              "id": 1, 
              "name": "LV0",
              "level": 0
            },
            "isStaff": false, 
            "isAdmin": false
          }
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 100,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 100,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
    ```
    ```
    - `content`: 해당 사용자가 작성한 모든 댓글의 배열입니다. 배열은 생성시간의 오름차순으로 정렬되어 있습니다. 댓글의 정보에는 작성자 정보가 제외되어 있습니다. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 인증된 사용자라면 누구나 접근할 수 있습니다.
    - jpa의 정렬 기능을 사용하여 정렬된 데이터를 가져오기 때문에 페이지네이션에서 제공하는 정렬 기능은 사용하지 않습니다. 
    - 존재하지 않는 `userId`를 입력하면 `404 Not Found` 응답을 반환합니다. 
    ```

- **댓글 수정**
  - **URI:** `/comments/{commentId}`
  - **HTTP 메서드:** `PUT`
  - **경로 변수:**
    ```
    - `commentId` (number): 수정할 댓글의 ID. 
    ```
  - **요청 형식:**
    ```http request
    PUT /comments/1
    ```
    ```json
    {
      "content": "Updated Post 1 - Comment 1"
    }
    ```
    ```
    - `content` (string): 수정할 댓글의 새로운 내용. 공백이 아니어야 합니다. 
    ```
  - **응답 형식:**
    ```json
    {
      "id": 1,
      "content": "Updated Post 1 - Comment 1",
      "createdDate": "2024-08-21T15:03:12.728634",
      "author": {
        "id": 2, 
        "username": "User 1",
        "role": {
          "id": 1, 
          "name": "LV0",
          "level": 0
        },
        "isStaff": false, 
        "isAdmin": false
      },
      "post": {
        "id": 1,
        "title": "Post 1",
        "content": "Content 1",
        "author": {
          "id": 2, 
          "username": "User 1",
          "role": {
            "id": 1, 
            "name": "LV0",
            "level": 0
          },
          "isStaff": false, 
          "isAdmin": false
        }, 
        "board": {
          "id": 1,
          "name": "Board 1", 
          "priority": 0,
          "readableRole": {
            "id": 1, 
            "name": "LV0",
            "level": 0
          },
          "postCount": 1
        },
        "createdDate": "2024-08-20T20:37:04.428879",
        "viewCount": 1,
        "likeCount": 0,
        "commentCount": 1
      }
    }
    ```
    ```
    - `id` (integer): 새로운 댓글의 ID. 
    - `content` (string): 새로운 댓글이 내용. 
    - `createdDate` (string, ISO 8601): 새로운 댓글이 생성된 날짜와 시간. 
    - `author` (object): 새로운 댓글의 작성자 정보. 
    - `post` (object): 새로운 댓글이 등록된 게시글의 정보. 
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 수정할 댓글의 작성자여야 합니다. 
    - 요청 형식의 유효성 검사를 통과하지 못하면 `400 Bad Request` 응답을 반환합니다. 
    - 존재하지 않는 `commentId`를 입력하면 `403 Forbidden` 응답을 반환합니다. 
    ```

- **댓글 삭제**
  - **URI:** `/comments/{commentId}`
  - **HTTP 메서드:** `DELETE`
  - **경로 변수:**
    ```
    - `commentId` (number): 수정할 댓글의 ID. 
    ```
  - **요청 형식:**
    ```http request
    DELETE /comments/1
    ```
  - **추가 설명:**
    ```
    - 인증이 필요하며, 해당 댓글의 작성자이거나 관리자 또는 스태프 권한이 필요합니다. 
    - 존재하지 않는 `commentId`를 입력하면 `403 Forbidden` 응답이 반환됩니다.
    ```

## 테스트 및 문서화 (Testing & Documentation)
- **테스트 방법:**
  - 유닛 테스트 (JUnit)
  - 통합 테스트 (Spring Boot Test)
