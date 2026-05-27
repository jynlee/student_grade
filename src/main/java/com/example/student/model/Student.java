package com.example.student.model;

/**
 * Student (학생) 모델 클래스
 * DB의 students 테이블 한 행(row)을 Java 객체로 표현합니다.
 *
 * [연결된 DB 테이블 구조 예시]
 * CREATE TABLE students (
 *     id         SERIAL PRIMARY KEY,    -- 학생 고유 번호 (자동 증가)
 *     name       VARCHAR(50) NOT NULL,  -- 학생 이름
 *     email      VARCHAR(100),          -- 이메일 주소
 *     grade_year INT                    -- 학년 (1, 2, 3, 4)
 * );
 */
public class Student {

    /** 학생 고유 번호 (Primary Key, DB가 자동 생성) */
    private int id;

    /** 학생 이름 */
    private String name;

    /** 이메일 주소 */
    private String email;

    /**
     * 학년
     * DB 컬럼명: grade_year (snake_case)
     * Java 필드명: gradeYear (camelCase)
     * → BeanPropertyRowMapper가 자동으로 변환해 줍니다.
     */
    private int gradeYear;

    // ============================================================
    // 기본 생성자 (반드시 있어야 합니다!)
    // BeanPropertyRowMapper가 결과를 객체로 변환할 때
    // 기본 생성자로 객체를 먼저 만든 다음, setter를 호출해 값을 넣습니다.
    // ============================================================
    public Student() {}

    // ============================================================
    // Getter / Setter
    // Lombok 없이 직접 작성합니다.
    // Getter: 외부에서 필드 값을 읽을 때 사용 (get + 필드명 첫 글자 대문자)
    // Setter: 외부에서 필드 값을 변경할 때 사용 (set + 필드명 첫 글자 대문자)
    // ============================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGradeYear() {
        return gradeYear;
    }

    public void setGradeYear(int gradeYear) {
        this.gradeYear = gradeYear;
    }

    /** 디버깅 시 객체 내용을 출력하기 쉽도록 오버라이드 */
    @Override
    public String toString() {
        return "Student{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", gradeYear=" + gradeYear + '}';
    }
}
