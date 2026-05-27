# 성적 관리 시스템 (Grade System)

Spring Legacy MVC + PostgreSQL + Docker 기반의 학생 성적 관리 REST API 서버입니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| 언어 | Java 11 |
| 프레임워크 | Spring MVC 5.3.39 (Legacy, XML 설정) |
| DB 접근 | Spring JdbcTemplate |
| 데이터베이스 | PostgreSQL 15 |
| 서버 | Apache Tomcat 9 |
| 빌드 | Apache Maven 3.9 |
| 컨테이너 | Docker, Docker Compose |
| 뷰 | JSP |

---

## 실행 방법

### 사전 요구사항

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) 설치 및 실행

### 1. 저장소 클론

```bash
git clone <저장소 URL>
cd grade_system
```

### 2. 빌드 및 실행

```bash
docker compose up --build
```

> 처음 실행 시 Maven 의존성 다운로드와 WAR 빌드가 진행되어 수 분이 걸릴 수 있습니다.

**내부 실행 순서:**

1. PostgreSQL 컨테이너 시작
2. `init.sql` 자동 실행 → 테이블 생성 + 샘플 데이터 삽입
3. PostgreSQL 헬스체크 통과 확인
4. Maven WAR 빌드 (`mvn package -DskipTests`)
5. Tomcat에 `ROOT.war` 배포
6. `http://localhost:8181` 접속 가능

### 3. 접속 확인

| 접속 경로 | 설명 |
|---|---|
| `http://localhost:8181` | 메인 화면 (index.jsp) |
| `http://localhost:8181/students` | 학생 목록 API |
| `http://localhost:8181/scores` | 성적 목록 API |

### 4. 중지 및 재시작

```bash
# 중지 (컨테이너 삭제, DB 데이터 유지)
docker compose down

# 중지 + DB 데이터까지 완전 초기화
docker compose down -v

# 백그라운드로 실행
docker compose up --build -d

# 로그 확인
docker compose logs -f
docker compose logs app
docker compose logs postgres
```

### 5. 로컬 직접 실행 (Docker 없이)

로컬에서 실행하려면 PostgreSQL을 직접 설치하고 `db.properties`의 호스트를 변경해야 합니다.

```properties
# src/main/resources/db.properties
db.url=jdbc:postgresql://localhost:5432/studentdb
```

```bash
mvn package -DskipTests
# 생성된 target/grade-system.war → Tomcat webapps에 배포
```

---

## 폴더 구조

```
grade_system/
│
├── Dockerfile                          # 멀티스테이지 빌드 (Maven → Tomcat)
├── docker-compose.yml                  # PostgreSQL + Tomcat 컨테이너 구성
├── init.sql                            # DB 초기화 (테이블 생성 + 샘플 데이터)
├── pom.xml                             # Maven 의존성 및 빌드 설정
│
└── src/
    └── main/
        ├── java/
        │   └── com/example/student/
        │       ├── controller/         # HTTP 요청 처리 계층
        │       │   ├── StudentController.java
        │       │   └── ScoreController.java
        │       ├── service/            # 비즈니스 로직 계층
        │       │   ├── StudentService.java
        │       │   └── ScoreService.java
        │       ├── model/              # DB 테이블 매핑 객체
        │       │   ├── Student.java
        │       │   └── Score.java
        │       └── dto/                # 데이터 전송 객체
        │           ├── StudentDTO.java
        │           ├── ScoreDTO.java
        │           └── GradeSummaryDTO.java
        │
        ├── resources/
        │   └── db.properties           # DB 접속 정보 (host, port, user, password)
        │
        └── webapp/
            ├── index.jsp               # 메인 화면 (학생 등록 폼 + 목록 테이블)
            └── WEB-INF/
                ├── web.xml             # 서블릿 설정 (DispatcherServlet, 인코딩 필터)
                ├── root-context.xml    # Spring Root 컨텍스트 (DataSource, JdbcTemplate)
                └── servlet-context.xml # Spring MVC 컨텍스트 (ViewResolver, 컴포넌트 스캔)
```

---

## DB 스키마

### students 테이블

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | SERIAL | PRIMARY KEY | 학생 고유 번호 (자동 증가) |
| `name` | VARCHAR(50) | NOT NULL | 학생 이름 |
| `email` | VARCHAR(100) | - | 이메일 주소 |
| `grade_year` | INT | - | 학년 (1~4) |

### scores 테이블

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | SERIAL | PRIMARY KEY | 성적 고유 번호 (자동 증가) |
| `student_id` | INT | FK → students(id) | 학생 고유 번호 |
| `subject` | VARCHAR(50) | NOT NULL | 과목명 (예: 수학, 영어) |
| `score` | INT | 0 ≤ score ≤ 100 | 점수 |

> `ON DELETE CASCADE` 적용 — 학생 삭제 시 해당 학생의 성적도 자동 삭제됩니다.

---

## API 목록

기본 URL: `http://localhost:8181`

모든 요청/응답은 `Content-Type: application/json` 형식입니다.

### 학생 API

#### 전체 학생 목록 조회

```
GET /students
```

**응답 예시 (200 OK)**
```json
[
  { "id": 1, "name": "홍길동", "email": "hong@example.com", "gradeYear": 2 },
  { "id": 2, "name": "김철수", "email": "kim@example.com",  "gradeYear": 3 }
]
```

---

#### 학생 등록

```
POST /students
Content-Type: application/json
```

**요청 Body**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "gradeYear": 2
}
```

**응답 예시 (201 Created)**
```json
{ "success": true, "message": "학생이 성공적으로 등록되었습니다." }
```

---

#### 학생 정보 수정

```
PUT /students/{id}
Content-Type: application/json
```

**요청 예시**
```
PUT /students/1
```
```json
{
  "name": "홍길순",
  "email": "hongsoon@example.com",
  "gradeYear": 3
}
```

**응답 예시 (200 OK)**
```json
{ "success": true, "message": "학생 정보가 성공적으로 수정되었습니다." }
```

**응답 예시 (404 Not Found)**
```json
{ "success": false, "message": "수정할 학생을 찾을 수 없습니다. id: 1" }
```

---

#### 학생 삭제

```
DELETE /students/{id}
```

**요청 예시**
```
DELETE /students/1
```

**응답 예시 (200 OK)**
```json
{ "success": true, "message": "학생이 성공적으로 삭제되었습니다. id: 1" }
```

**응답 예시 (404 Not Found)**
```json
{ "success": false, "message": "삭제할 학생을 찾을 수 없습니다. id: 1" }
```

---

### 성적 API

#### 성적 목록 조회

```
GET /scores
GET /scores?studentId={id}    ← 특정 학생의 성적만 조회
```

**응답 예시 (200 OK)**
```json
[
  { "id": 1, "studentId": 1, "subject": "수학", "score": 95 },
  { "id": 2, "studentId": 1, "subject": "영어", "score": 88 }
]
```

---

#### 성적 등록

```
POST /scores
Content-Type: application/json
```

**요청 Body**
```json
{
  "studentId": 1,
  "subject": "수학",
  "score": 95
}
```

**응답 예시 (201 Created)**
```json
{ "success": true, "message": "성적이 성공적으로 등록되었습니다." }
```

---

### HTTP 상태 코드 정리

| 코드 | 의미 | 사용 시점 |
|---|---|---|
| `200 OK` | 요청 성공 | 조회, 수정, 삭제 성공 |
| `201 Created` | 생성 성공 | 학생/성적 등록 성공 |
| `404 Not Found` | 리소스 없음 | 해당 ID가 DB에 없을 때 |
| `500 Internal Server Error` | 서버 오류 | DB 오류 등 예기치 않은 실패 |

---

## 환경 변수 및 설정

### DB 접속 정보 (`src/main/resources/db.properties`)

```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://postgres:5432/studentdb
db.username=student
db.password=student123
```

> `postgres`는 Docker Compose 서비스 이름으로, 컨테이너 내부 네트워크에서 호스트명으로 사용됩니다.

### Docker Compose 포트

| 서비스 | 내부 포트 | 외부 포트 | 용도 |
|---|---|---|---|
| Tomcat (app) | 8080 | **8181** | 웹 애플리케이션 접속 |
| PostgreSQL | 5432 | 5432 | DB 직접 접속 (DBeaver 등) |

---

## 레이어 구조

```
[브라우저 / API 클라이언트]
         │  HTTP 요청
         ▼
[Controller]  — StudentController, ScoreController
         │  메서드 호출
         ▼
[Service]     — StudentService, ScoreService
         │  JdbcTemplate SQL 실행
         ▼
[PostgreSQL DB]
```

| 계층 | 패키지 | 역할 |
|---|---|---|
| Controller | `com.example.student.controller` | HTTP 요청/응답 처리, JSON 변환 |
| Service | `com.example.student.service` | 비즈니스 로직, 평균/등급 계산 |
| Model | `com.example.student.model` | DB 테이블 매핑 객체 |
| DTO | `com.example.student.dto` | 계층 간 데이터 전달 객체 |

---

## 샘플 데이터

`init.sql` 실행 시 아래 데이터가 자동으로 삽입됩니다.

**students**

| id | name | email | grade_year |
|---|---|---|---|
| 1 | 홍길동 | hong@example.com | 2 |
| 2 | 김철수 | kim@example.com | 3 |
| 3 | 이영희 | lee@example.com | 1 |

**scores**

| student | 수학 | 영어 | 과학 | 평균 | 학점 |
|---|---|---|---|---|---|
| 홍길동 | 95 | 88 | 72 | 85.0 | B |
| 김철수 | 78 | 91 | 85 | 84.7 | B |
| 이영희 | 65 | 58 | 90 | 71.0 | C |

**등급 기준**

| 평균 | 학점 |
|---|---|
| 90 ~ 100 | A |
| 80 ~ 89 | B |
| 70 ~ 79 | C |
| 60 ~ 69 | D |
| 0 ~ 59 | F |
