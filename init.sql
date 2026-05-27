-- ================================================================
-- init.sql - 성적 관리 시스템 DB 초기화 스크립트
-- ================================================================
--
-- [자동 실행 시점]
--   docker-compose.yml에서 이 파일을 PostgreSQL 컨테이너의
--   /docker-entrypoint-initdb.d/ 폴더에 마운트합니다.
--   PostgreSQL은 처음 시작될 때(DB 데이터가 없을 때) 이 폴더의
--   .sql 파일을 알파벳 순서대로 자동 실행합니다.
--
-- [재실행 방법]
--   볼륨에 이미 데이터가 있으면 재실행하지 않습니다.
--   처음부터 다시 실행하려면:
--     docker compose down -v    ← 컨테이너 + 볼륨(DB 데이터) 삭제
--     docker compose up --build ← 재시작
--
-- [실행 대상 DB]
--   docker-compose.yml의 POSTGRES_DB: studentdb
-- ================================================================


-- ================================================================
-- 인코딩 설정 (한글 처리)
-- ================================================================
-- PostgreSQL은 기본적으로 UTF-8을 사용하므로 별도 설정이 불필요하나
-- 클라이언트 연결 인코딩을 명시적으로 지정합니다.
SET client_encoding = 'UTF8';


-- ================================================================
-- 1. students 테이블 생성 (학생 기본 정보)
-- ================================================================
--
-- IF NOT EXISTS: 테이블이 이미 존재하면 오류 없이 건너뜁니다.
--               스크립트를 여러 번 실행해도 안전합니다. (멱등성)
-- ================================================================
CREATE TABLE IF NOT EXISTS students (

    -- id: 학생 고유 번호 (Primary Key)
    -- SERIAL: 자동 증가 정수 (1, 2, 3, ...) → PostgreSQL의 AUTO_INCREMENT
    -- PRIMARY KEY: 중복 불가, NULL 불가, 고유 식별자
    id         SERIAL       PRIMARY KEY,

    -- name: 학생 이름 (필수)
    -- VARCHAR(50): 최대 50자의 가변 길이 문자열
    -- NOT NULL: 반드시 값이 있어야 합니다. (빈 이름 불가)
    name       VARCHAR(50)  NOT NULL,

    -- email: 이메일 주소 (선택)
    -- NULL 허용: 이메일 없이도 등록 가능합니다.
    email      VARCHAR(100),

    -- grade_year: 학년 (1~4학년)
    -- INT: 정수형 (4바이트)
    -- NULL 허용: 학년 정보 없이도 등록 가능합니다.
    -- snake_case(grade_year) → Java BeanPropertyRowMapper가 camelCase(gradeYear)로 자동 변환
    grade_year INT

);

-- 테이블 생성 확인용 주석 출력
-- \echo 'students 테이블 생성 완료';


-- ================================================================
-- 2. scores 테이블 생성 (성적 정보)
-- ================================================================
--
-- students 테이블과 FK(외래 키)로 연결됩니다.
-- 반드시 students 테이블이 먼저 생성되어 있어야 합니다.
-- ================================================================
CREATE TABLE IF NOT EXISTS scores (

    -- id: 성적 고유 번호 (Primary Key)
    id         SERIAL       PRIMARY KEY,

    -- student_id: 이 성적이 속한 학생의 id (Foreign Key)
    -- NOT NULL: 반드시 어떤 학생에게 속해야 합니다.
    -- REFERENCES students(id): students 테이블의 id 컬럼을 참조합니다.
    --   → students 테이블에 존재하는 id 값만 입력 가능합니다.
    --   → 존재하지 않는 학생 id로 성적 등록 시 오류가 발생합니다.
    student_id INT          NOT NULL,

    -- subject: 과목명 (예: '수학', '영어', '과학')
    -- NOT NULL: 과목명은 필수입니다.
    subject    VARCHAR(50)  NOT NULL,

    -- score: 점수 (0 ~ 100)
    -- NOT NULL: 점수는 필수입니다.
    -- CHECK: 입력값이 조건을 만족하는지 DB 레벨에서 검증합니다.
    --   score BETWEEN 0 AND 100 → 0 미만이나 100 초과 값 입력 시 오류 발생
    score      INT          NOT NULL
                            CHECK (score BETWEEN 0 AND 100),

    -- -------------------------------------------------------
    -- FK(Foreign Key) 제약 조건 정의
    -- -------------------------------------------------------
    -- CONSTRAINT fk_scores_student: 이 FK의 이름 (오류 메시지에서 식별에 사용)
    -- FOREIGN KEY (student_id): 이 테이블의 student_id 컬럼이 외래 키
    -- REFERENCES students(id): students 테이블의 id 를 참조
    --
    -- ON DELETE CASCADE: 부모(students) 행 삭제 시 자식(scores) 행도 자동 삭제
    --   예) 학생 id=1 삭제 → 해당 학생의 모든 성적도 자동 삭제
    --   이 설정이 없으면 성적이 있는 학생은 삭제할 수 없습니다.
    CONSTRAINT fk_scores_student
        FOREIGN KEY (student_id)
        REFERENCES  students(id)
        ON DELETE CASCADE

);


-- ================================================================
-- 3. 인덱스 생성 (검색 성능 최적화)
-- ================================================================
--
-- 인덱스(Index)란?
--   책의 목차처럼 특정 컬럼으로 빠르게 검색할 수 있게 해주는 구조입니다.
--   없으면 모든 행을 하나씩 확인하는 Full Scan을 수행합니다. (느림)
--
-- student_id 로 성적을 자주 조회하므로 인덱스를 추가합니다.
-- 예: SELECT * FROM scores WHERE student_id = 1
-- ================================================================
CREATE INDEX IF NOT EXISTS idx_scores_student_id
    ON scores(student_id);


-- ================================================================
-- 4. 샘플 데이터 삽입
-- ================================================================
--
-- [주의]
-- 볼륨에 이미 데이터가 있는 경우 이 스크립트가 실행되지 않으므로
-- 중복 INSERT는 발생하지 않습니다.
-- ================================================================

-- ---------------------------------------------------------------
-- 4-1. 학생 샘플 데이터 (3명)
-- ---------------------------------------------------------------
-- INSERT INTO 테이블명 (컬럼1, ...) VALUES (값1, ...);
-- id는 SERIAL(자동 증가)이므로 직접 지정하지 않습니다.
-- ---------------------------------------------------------------
INSERT INTO students (name, email, grade_year) VALUES
    -- 학생 1: 홍길동 (id=1 자동 할당 예정)
    ('홍길동', 'hong@example.com', 2),
    -- 학생 2: 김철수 (id=2 자동 할당 예정)
    ('김철수', 'kim@example.com',  3),
    -- 학생 3: 이영희 (id=3 자동 할당 예정)
    ('이영희', 'lee@example.com',  1);


-- ---------------------------------------------------------------
-- 4-2. 성적 샘플 데이터
--
-- student_id는 위에서 삽입된 students 테이블의 id를 참조합니다.
-- SERIAL의 시작값은 1이므로 홍길동=1, 김철수=2, 이영희=3 입니다.
-- ---------------------------------------------------------------

-- 홍길동 (student_id = 1) 성적: 3과목
INSERT INTO scores (student_id, subject, score) VALUES
    (1, '수학', 95),
    (1, '영어', 88),
    (1, '과학', 72);
-- 홍길동 평균: (95 + 88 + 72) / 3 = 85.0 → 학점 B

-- 김철수 (student_id = 2) 성적: 3과목
INSERT INTO scores (student_id, subject, score) VALUES
    (2, '수학', 78),
    (2, '영어', 91),
    (2, '과학', 85);
-- 김철수 평균: (78 + 91 + 85) / 3 = 84.67 → 학점 B

-- 이영희 (student_id = 3) 성적: 3과목
INSERT INTO scores (student_id, subject, score) VALUES
    (3, '수학', 65),
    (3, '영어', 58),
    (3, '과학', 90);
-- 이영희 평균: (65 + 58 + 90) / 3 = 71.0 → 학점 C


-- ================================================================
-- 5. 삽입 결과 확인 (초기화 완료 메시지)
-- ================================================================
--
-- DO $$ BEGIN ... END $$: PostgreSQL 익명 블록 (pl/pgSQL)
-- RAISE NOTICE: 서버 로그에 메시지를 출력합니다.
-- 'docker compose logs postgres' 명령어로 이 메시지를 확인할 수 있습니다.
-- ================================================================
DO $$
DECLARE
    student_count INT;
    score_count   INT;
BEGIN
    SELECT COUNT(*) INTO student_count FROM students;
    SELECT COUNT(*) INTO score_count   FROM scores;

    RAISE NOTICE '========================================';
    RAISE NOTICE ' [init.sql] DB 초기화 완료!';
    RAISE NOTICE '   students 테이블: % 건', student_count;
    RAISE NOTICE '   scores   테이블: % 건', score_count;
    RAISE NOTICE '========================================';
END $$;
