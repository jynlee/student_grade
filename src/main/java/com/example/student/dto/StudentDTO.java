package com.example.student.dto;

/**
 * StudentDTO - 학생 데이터 전송 객체 (Data Transfer Object)
 *
 * DTO(Data Transfer Object)란?
 *   계층 간에 데이터를 주고받을 때 사용하는 객체입니다.
 *   주로 Controller ↔ Service, 또는 서버 ↔ 클라이언트(브라우저) 사이에서 사용됩니다.
 *
 * [Model vs DTO 차이점]
 *   Student.java (Model): DB 테이블 구조를 그대로 반영 → DB 중심 설계
 *   StudentDTO.java (DTO): 클라이언트와 주고받는 형태에 최적화 → API 중심 설계
 *
 *   DTO를 따로 두는 이유:
 *     - DB 테이블 구조가 바뀌어도 API 응답 형태를 유지할 수 있습니다.
 *     - 클라이언트에 노출하면 안 되는 필드(비밀번호 등)를 제외할 수 있습니다.
 *     - 여러 테이블을 조합한 응답을 하나의 객체로 표현할 수 있습니다.
 *
 * [사용 흐름 예시]
 *   클라이언트 → JSON 전송
 *   → Controller에서 @RequestBody StudentDTO 로 받음
 *   → Service에서 StudentDTO → Student(Model)로 변환해 DB 저장
 *
 *   DB 조회 결과 Student(Model)
 *   → Service에서 Student → StudentDTO 로 변환
 *   → Controller에서 @ResponseBody로 JSON 응답
 */
public class StudentDTO {

    /** 학생 고유 번호 (DB Primary Key) */
    private int id;

    /** 학생 이름 */
    private String name;

    /** 이메일 주소 */
    private String email;

    /**
     * 학년 (1 ~ 4)
     * DB 컬럼명 grade_year → Java 필드명 gradeYear (BeanPropertyRowMapper 자동 변환)
     */
    private int gradeYear;

    // ============================================================
    // 기본 생성자 (No-Args Constructor)
    // Spring이 JSON을 객체로 변환할 때(역직렬화) 기본 생성자가 필요합니다.
    // @RequestBody로 JSON을 받을 때 Jackson이 이 생성자로 객체를 먼저 만듭니다.
    // ============================================================
    public StudentDTO() {}

    // ============================================================
    // 전체 필드 생성자 (All-Args Constructor)
    // Service 계층에서 Student(Model) → StudentDTO 변환 시 편리하게 사용합니다.
    //
    // 사용 예:
    //   Student s = studentService.findById(1);
    //   StudentDTO dto = new StudentDTO(s.getId(), s.getName(), s.getEmail(), s.getGradeYear());
    // ============================================================
    public StudentDTO(int id, String name, String email, int gradeYear) {
        this.id        = id;
        this.name      = name;
        this.email     = email;
        this.gradeYear = gradeYear;
    }

    // ============================================================
    // Getter / Setter
    // Lombok 없이 직접 작성합니다.
    // Jackson은 getter를 통해 Java 객체 → JSON 변환(직렬화)을 수행합니다.
    // Jackson은 setter를 통해 JSON → Java 객체 변환(역직렬화)을 수행합니다.
    // ============================================================

    /**
     * id 필드 값을 반환합니다.
     * Jackson이 {"id": 1} JSON 생성 시 이 메서드를 호출합니다.
     */
    public int getId() {
        return id;
    }

    /** id 필드 값을 설정합니다. */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * name 필드 값을 반환합니다.
     * Jackson이 {"name": "홍길동"} JSON 생성 시 이 메서드를 호출합니다.
     */
    public String getName() {
        return name;
    }

    /** name 필드 값을 설정합니다. */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * email 필드 값을 반환합니다.
     */
    public String getEmail() {
        return email;
    }

    /** email 필드 값을 설정합니다. */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * gradeYear 필드 값을 반환합니다.
     * Jackson이 {"gradeYear": 2} JSON 생성 시 이 메서드를 호출합니다.
     */
    public int getGradeYear() {
        return gradeYear;
    }

    /** gradeYear 필드 값을 설정합니다. */
    public void setGradeYear(int gradeYear) {
        this.gradeYear = gradeYear;
    }

    /** 디버깅 시 객체 내용을 출력하기 쉽도록 오버라이드합니다. */
    @Override
    public String toString() {
        return "StudentDTO{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", gradeYear=" + gradeYear + '}';
    }
}
