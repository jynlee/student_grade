package com.example.student.model;

/**
 * Score (성적) 모델 클래스
 * DB의 scores 테이블 한 행(row)을 Java 객체로 표현합니다.
 *
 * [연결된 DB 테이블 구조 예시]
 * CREATE TABLE scores (
 *     id         SERIAL PRIMARY KEY,          -- 성적 고유 번호 (자동 증가)
 *     student_id INT NOT NULL,                -- 학생 번호 (students.id 참조)
 *     subject    VARCHAR(50) NOT NULL,        -- 과목명 (예: "수학", "영어")
 *     score      INT NOT NULL CHECK (score BETWEEN 0 AND 100) -- 점수
 * );
 */
public class Score {

    /** 성적 고유 번호 (Primary Key, DB가 자동 생성) */
    private int id;

    /**
     * 학생 고유 번호 (Foreign Key → students.id)
     * DB 컬럼명: student_id (snake_case)
     * Java 필드명: studentId (camelCase)
     * → BeanPropertyRowMapper가 자동으로 변환해 줍니다.
     */
    private int studentId;

    /** 과목명 (예: "수학", "영어", "과학") */
    private String subject;

    /** 점수 (0 ~ 100) */
    private int score;

    // ============================================================
    // 기본 생성자 (반드시 있어야 합니다!)
    // BeanPropertyRowMapper가 객체를 생성할 때 사용합니다.
    // ============================================================
    public Score() {}

    // ============================================================
    // Getter / Setter
    // ============================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /** 디버깅 시 객체 내용을 출력하기 쉽도록 오버라이드 */
    @Override
    public String toString() {
        return "Score{id=" + id
                + ", studentId=" + studentId
                + ", subject='" + subject + '\''
                + ", score=" + score + '}';
    }
}
