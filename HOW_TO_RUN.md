# How To Run

1. [소개 (Introduction)](#1-소개-introduction)
2. [사전 요구 사항 (Prerequisites)](#2-사전-요구-사항-prerequisites)
    - 2.1 [Docker](#21-docker)
    - 2.2 [Docker Compose](#22-docker-compose)
    - 2.3 [SSL 인증서](#23-ssl-인증서)
3. [Docker Compose 파일 설명 (Docker Compose File Explanation)](#3-docker-compose-파일-설명-docker-compose-file-explanation)
    - 3.1 [주요 환경 변수 (Environment Variables)](#31-주요-환경-변수-environment-variables)
    - 3.2 [포트 설정 (Ports)](#32-포트-설정-ports)
    - 3.3 [데이터 영속성 설정 (Volumes)](#33-데이터-영속성-설정-volumes)
    - 3.4 [의존성 설정 (Depends On)](#34-의존성-설정-depends-on)
4. [프로젝트 실행 방법 (How to Run the Project)](#4-프로젝트-실행-방법-how-to-run-the-project)
    - 4.1 [Docker Compose 실행](#41-docker-compose-실행)
    - 4.2 [애플리케이션 접근](#42-애플리케이션-접근)
    - 4.3 [프로젝트 종료](#43-프로젝트-종료)

---

## 1. 소개 (Introduction)
이 문서는 Docker Compose를 사용하여 이 프로젝트를 실행하는 방법을 설명합니다. Docker Compose는 여러 컨테이너를 정의하고 동시에 실행할 수 있는 도구로, 이 프로젝트에서는 **애플리케이션 컨테이너**와 **MySQL 데이터베이스 컨테이너**를 설정하고 관리합니다.

본 프로젝트는 **Kotlin**, **Spring Boot**, **MySQL** 등을 기반으로 한 RESTful API입니다. 이 문서에서는 Docker Hub에 빌드된 이미지를 가져와 애플리케이션을 실행하며, SSL 인증서를 사용하여 HTTPS를 통해 보안이 강화된 환경에서 애플리케이션을 구동하는 방법을 설명합니다.

Docker Compose를 이용함으로써 애플리케이션의 배포와 실행 환경을 **일관성 있게 유지**할 수 있으며, 이를 통해 **빠르게 프로젝트를 실행**하고 **환경 구성 문제를 최소화**할 수 있습니다.

본 가이드에서는 다음과 같은 내용을 다룹니다:
- Docker Compose 파일 설명
- SSL 인증서 설정
- 환경 변수 및 포트 설정
- 프로젝트 실행 및 종료 방법

---

## 2. 사전 요구 사항 (Prerequisites)
프로젝트를 실행하기 전에 아래의 소프트웨어가 시스템에 설치되어 있어야 합니다. 각 소프트웨어의 설치 링크도 함께 제공하니 참고하여 환경을 설정하십시오.
### 2.1 Docker
- **Docker**는 컨테이너화된 애플리케이션을 손쉽게 실행할 수 있는 도구입니다. 이 프로젝트는 Docker를 사용하여 애플리케이션과 데이터베이스를 관리합니다.
- **설치 링크**: [Docker 설치 가이드](https://docs.docker.com/get-docker/)
### 2.2 Docker Compose
- **Docker Compose**는 여러 컨테이너를 정의하고 동시에 실행할 수 있게 도와주는 도구입니다. 이 프로젝트는 애플리케이션과 데이터베이스 컨테이너를 구성하기 위해 Docker Compose를 사용합니다.
- **설치 링크**: [Docker Compose 설치 가이드](https://docs.docker.com/compose/install/)
- **필수 버전**: Docker Compose 2.0 이상
### 2.3 SSL 인증서
- 이 프로젝트는 **HTTPS**를 사용하여 보안이 강화된 통신을 제공합니다. 따라서 **SSL 인증서**가 필요하며, 테스트 환경에서는 자체 서명된 인증서(self-signed certificate)를 사용할 수 있습니다.
- **테스트용 SSL 인증서 생성 방법**: 아래 명령어를 사용하여 테스트 환경에서 사용할 SSL 인증서를 생성할 수 있습니다.
  ```bash
  keytool -genkeypair -alias selfsigned -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650
  ```
생성된 keystore.p12 파일을 프로젝트의 루트 디렉터리에 위치시킵니다.

---

## 3. Docker Compose 파일 설명 (Docker Compose File Explanation)
이 프로젝트는 **Docker Compose**를 사용하여 애플리케이션과 MySQL 데이터베이스를 동시에 실행합니다.
```yaml
services:
  app:
    image: yj1412/community-api:latest
    ports:
      - "8443:8443"
    environment:
      - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/community?serverTimezone=UTC
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=   # 입력 필요: MySQL 비밀번호를 설정하세요
      - JWT_SECRET=                   # 입력 필요: JWT 서명에 사용할 비밀 키를 입력하세요
      - JWT_ACCESS_TOKEN_VALIDITY=3600
      - JWT_REFRESH_TOKEN_VALIDITY=604800
      - SERVER_SSL_KEY_STORE=/app/keystore.p12
      - SERVER_SSL_KEY_STORE_PASSWORD=  # 입력 필요: SSL keystore 비밀번호를 입력하세요
      - SERVER_SSL_KEY_STORE_TYPE=PKCS12
      - SERVER_SSL_KEY_ALIAS=mycert
    volumes:
      - ./keystore.p12:/app/keystore.p12
    depends_on:
      db:
        condition: service_healthy

  db:
    image: mysql:8.0
    container_name: db
    environment:
      - MYSQL_ROOT_PASSWORD=  # 입력 필요: MySQL 루트 비밀번호를 설정하세요
      - MYSQL_DATABASE=community
    volumes:
      - db_data:/var/lib/mysql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 30s
      
volumes:
  db_data:
```

### 3.1 주요 환경 변수 (Environment Variables)
Docker Compose 파일에서 설정해야 할 **중요한 환경 변수**는 아래와 같습니다. 각 변수를 실행 환경에 맞게 설정하십시오.
- **SPRING_DATASOURCE_URL**: MySQL 데이터베이스의 연결 URL입니다. 데이터베이스가 컨테이너 내에서 실행되므로, 컨테이너 이름(`db`)을 사용해 데이터베이스에 접근합니다.
- **SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD**: 데이터베이스 연결에 필요한 자격 증명입니다.
- **MYSQL_ROOT_PASSWORD**: MySQL 루트 사용자의 비밀번호를 설정해야 합니다. **SPRING_DATASOURCE_PASSWORD**와 **MYSQL_ROOT_PASSWORD**는 일치해야 합니다.
- **JWT_SECRET**: JWT 토큰을 서명하는 데 사용되는 비밀 키입니다. 애플리케이션에서 보안적으로 중요한 역할을 합니다.
- **JWT_ACCESS_TOKEN_VALIDITY / JWT_REFRESH_TOKEN_VALIDITY**: 각각 **Access Token**과 **Refresh Token**의 유효 기간을 초 단위로 설정합니다.
- **SERVER_SSL_KEY_STORE**: SSL 인증서의 경로입니다. 
- **SERVER_SSL_KEY_STORE_PASSWORD**: SSL 인증서의 비밀번호를 입력해야 합니다.

### 3.2 포트 설정 (Ports)
- **8443:8443**: 로컬 시스템의 **8443 포트**를 컨테이너의 **8443 포트**로 매핑하여 HTTPS를 통한 애플리케이션 접근을 지원합니다.
- **3306:3306**: MySQL 데이터베이스의 **3306 포트**를 로컬 시스템에 노출하여 외부에서 데이터베이스에 접근할 수 있습니다.

### 3.3 데이터 영속성 설정 (Volumes)
- **db_data**: MySQL 데이터베이스의 데이터를 **영구적으로 저장**하기 위해, 컨테이너 내의 데이터를 로컬 시스템의 볼륨에 저장합니다. 이를 통해 데이터베이스를 재시작하거나 컨테이너가 삭제되더라도 데이터는 보존됩니다.
- **SSL 인증서 마운트**: 로컬 시스템의 `keystore.p12` 파일을 컨테이너 내의 **SERVER_SSL_KEY_STORE**로 마운트하여 SSL 인증서를 사용할 수 있게 설정합니다.

### 3.4 의존성 설정 (Depends On)
- **depends_on**: `app` 서비스는 `db` 서비스가 정상적으로 실행된 후에만 실행되도록 설정되어 있습니다. 이는 데이터베이스가 준비되지 않은 상태에서 애플리케이션이 실행되는 문제를 방지합니다.
- **healthcheck**: MySQL 데이터베이스 컨테이너의 상태를 주기적으로 확인하기 위해 **healthcheck**가 설정되어 있습니다. 데이터베이스가 정상적으로 실행 중인지 확인한 후, 애플리케이션이 실행됩니다.

---

## 4. 프로젝트 실행 방법 (How to Run the Project)
이 섹션에서는 Docker Compose를 사용하여 프로젝트를 실행하는 절차를 설명합니다.
### 4.1 Docker Compose 실행
환경 변수 설정과 SSL 인증서 준비가 완료되었으면, 아래 명령어를 실행하여 Docker Compose로 컨테이너를 시작하십시오:
```bash
docker-compose up -d
```
이 명령어는 백그라운드에서 애플리케이션과 데이터베이스 컨테이너를 실행합니다. 모든 컨테이너가 정상적으로 시작되면, 애플리케이션에 접근할 수 있습니다.
### 4.2 애플리케이션 접근
애플리케이션이 성공적으로 실행되면, HTTPS를 통해 다음 주소에서 애플리케이션과 API 문서에 접근할 수 있습니다:
- **애플리케이션 메인 페이지**:  
  ```
  https://localhost:8443
  ```
- **Swagger UI**: Swagger UI를 통해 프로젝트의 API 엔드포인트를 테스트하고, API의 동작을 시각적으로 확인할 수 있습니다.
  ```
  https://localhost:8443/swagger-ui/index.html
  ```
  
- **API 문서 (JSON 형식)**: API 문서를 **JSON 형식**으로 확인할 수 있습니다. 이 문서는 Swagger를 통해 정의된 모든 엔드포인트와 데이터 스키마를 포함합니다.
  ```
  https://localhost:8443/v3/api-docs
  ```

브라우저에서 위 주소로 이동하여 애플리케이션과 Swagger UI, API 문서에 접근하십시오.
### 4.3 프로젝트 종료
프로젝트를 종료하려면 다음 명령어를 사용하여 Docker Compose로 실행된 모든 컨테이너를 중지할 수 있습니다:
```bash
docker-compose down
```
이 명령어는 컨테이너를 종료하고, 실행 중인 애플리케이션을 중지합니다. 데이터베이스의 데이터는 db_data 볼륨에 저장되어 있으므로, 재실행 시에도 데이터가 유지됩니다.