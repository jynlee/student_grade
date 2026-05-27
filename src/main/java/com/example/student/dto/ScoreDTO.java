package com.example.student.dto;

/**
 * ScoreDTO - 성적 데이터 전송 객체 (Data Transfer Object)
 *
 * DB의 scores 테이블 데이터를 클라이언트와 주고받을 때 사용하는 객체입니다.
 *
 * [DB 테이블 구조와의 관계]
 *   scores 테이블: id, student_id, subject, score
 *   ScoreDTO 필드: id, studentId,  subject, score
 *
 *   student_id(snake_case) → studentId(camelCase) 변환은
 *   BeanPropertyRowMapper 또는 Service 계층의 변환 로직에서 처리됩니다.
 *
 * [사용 흐름 예시]
 *   클라이언트 → {"studentId": 1, "subject": "수학", "score": 95} JSON 전송
 *   → Controller에서 @RequestBody ScoreDTO 로 받음
 *   → Service에서 ScoreDTO → Score(Model)로 변환해 DB 저장
 *
 *   DB 조회 결과 Score(Model)
 *   → Service에서 Score → ScoreDTO 로 변환
 *   → Controller에서 @ResponseBody로 JSON 응답
 */
public class ScoreDTO {

    /** 성적 고유 번호 (DB Primary Key, DB가 자동 생성) */
    private int id;

    /**
     * 학생 고유 번호 (DB Foreign Key → students.id)
     * JSON으로 주고받을 때: {"studentId": 1}
     * DB 컬럼명: student_id
     */
    private int studentId;

    /** 과목명 (예: "수학", "영어", "과학") */
    private String subject;

    /** 점수 (0 ~ 100) */
    private int score;

    // ============================================================
    // 기본 생성자 (No-Args Constructor)
    // @RequestBody로 JSON을 받을 때 Jackson이 사용합니다.
    // 반드시 있어야 합니다!
    // ============================================================
    public ScoreDTO() {}

    // ============================================================
    // 전체 필드 생성자 (All-Args Constructor)
    // Score(Model) → ScoreDTO 변환 시 사용합니다.
    //
    // 사용 예:
    //   Score s = scoreService.findById(1);
    //   ScoreDTO dto = new ScoreDTO(s.getId(), s.getStudentId(), s.getSubject(), s.getScore());
    // ============================================================
    public ScoreDTO(int id, int studentId, String subject, int score) {
        this.id        = id;
        this.studentId = studentId;
        this.subject   = subject;
        this.score     = score;
    }

    // ============================================================
    // Getter / Setter
    // Jackson이 직렬화(객체→JSON) 시 getter를 사용합니다.
    // Jackson이 역직렬화(JSON→객체) 시 setter를 사용합니다.
    // ============================================================

    /**
     * id 필드 값을 반환합니다.
     */
    public int getId() {
        return id;
    }

    /** id 필드 값을 설정합니다. */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * studentId 필드 값을 반환합니다.
     * Jackson이 {"studentId": 1} JSON 생성 시 이 메서드를 호출합니다.
     */
    public int getStudentId() {
        return studentId;
    }

    /** studentId 필드 값을 설정합니다. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * subject 필드 값을 반환합니다.
     */
    public String getSubject() {
        return subject;
    }

    /** subject 필드 값을 설정합니다. */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * score 필드 값을 반환합니다.
     */
    public int getScore() {
        return score;
    }

    /** score 필드 값을 설정합니다. */
    public void setScore(int score) {
        this.score = score;
    }

    /** 디버깅 시 객체 내용을 출력하기 쉽도록 오버라이드합니다. */
    @Override
    public String toString() {
        return "ScoreDTO{id=" + id
                + ", studentId=" + studentId
                + ", subject='" + subject + '\''
                + ", score=" + score + '}';
    }
}
