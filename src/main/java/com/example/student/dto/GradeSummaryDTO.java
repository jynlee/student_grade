package com.example.student.dto;

/**
 * GradeSummaryDTO - 학생 성적 종합 요약 데이터 전송 객체
 *
 * 이 DTO는 여러 테이블(students + scores)의 데이터를 조합한 결과를
 * 클라이언트에 한 번에 전달하기 위해 만든 객체입니다.
 *
 * [포함 정보]
 *   - 학생 기본 정보: studentId, studentName, email, gradeYear
 *   - 계산된 성적 정보: average(평균), grade(등급), scoreCount(과목 수)
 *
 * [단일 테이블 Model로 표현할 수 없는 이유]
 *   students 테이블: 학생 기본 정보만 보유
 *   scores 테이블:   개별 과목 성적만 보유
 *   → 두 테이블을 JOIN하거나 Service에서 조합해 이 DTO 하나로 클라이언트에 전달합니다.
 *
 * [Service에서 생성하는 예시]
 *   Student student = studentService.findById(studentId);
 *   double  average = studentService.calculateAverage(studentId);
 *   String  grade   = studentService.calculateGrade(average);
 *   int     count   = scoreService.findByStudentId(studentId).size();
 *
 *   GradeSummaryDTO summary = new GradeSummaryDTO(
 *       student.getId(),
 *       student.getName(),
 *       student.getEmail(),
 *       student.getGradeYear(),
 *       average,
 *       grade,
 *       count
 *   );
 *
 * [JSON 응답 예시]
 *   {
 *     "studentId":   1,
 *     "studentName": "홍길동",
 *     "email":       "hong@example.com",
 *     "gradeYear":   2,
 *     "average":     88.5,
 *     "grade":       "B",
 *     "scoreCount":  4
 *   }
 */
public class GradeSummaryDTO {

    /** 학생 고유 번호 */
    private int studentId;

    /** 학생 이름 */
    private String studentName;

    /** 이메일 주소 */
    private String email;

    /** 학년 (1 ~ 4) */
    private int gradeYear;

    /**
     * 전체 과목 평균 점수 (소수점 포함)
     * StudentService.calculateAverage(studentId)의 반환값을 그대로 담습니다.
     * 예) 88.5, 73.0, 95.25
     */
    private double average;

    /**
     * 학점 등급 문자열
     * StudentService.calculateGrade(average)의 반환값을 담습니다.
     * 예) "A", "B", "C", "D", "F"
     */
    private String grade;

    /**
     * 등록된 과목(성적) 수
     * 해당 학생의 scores 테이블 행 수입니다.
     * 예) 수학, 영어, 과학, 국어 4과목이면 4
     */
    private int scoreCount;

    // ============================================================
    // 기본 생성자 (No-Args Constructor)
    // Jackson이 JSON → 객체 변환(역직렬화) 시 사용합니다.
    // 이 DTO는 주로 서버→클라이언트 응답용이지만 기본 생성자는 관례적으로 포함합니다.
    // ============================================================
    public GradeSummaryDTO() {}

    // ============================================================
    // 전체 필드 생성자 (All-Args Constructor)
    // Service에서 여러 조회 결과를 조합해 이 DTO를 만들 때 사용합니다.
    //
    // 사용 예:
    //   GradeSummaryDTO dto = new GradeSummaryDTO(1, "홍길동", "hong@example.com", 2, 88.5, "B", 4);
    // ============================================================
    public GradeSummaryDTO(int studentId, String studentName, String email,
                           int gradeYear, double average, String grade, int scoreCount) {
        this.studentId   = studentId;
        this.studentName = studentName;
        this.email       = email;
        this.gradeYear   = gradeYear;
        this.average     = average;
        this.grade       = grade;
        this.scoreCount  = scoreCount;
    }

    // ============================================================
    // Getter / Setter
    // Jackson이 직렬화(객체→JSON) 시 getter를 사용합니다.
    // Jackson이 역직렬화(JSON→객체) 시 setter를 사용합니다.
    // ============================================================

    /**
     * studentId 필드 값을 반환합니다.
     * JSON 응답 시: {"studentId": 1}
     */
    public int getStudentId() {
        return studentId;
    }

    /** studentId 필드 값을 설정합니다. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * studentName 필드 값을 반환합니다.
     * JSON 응답 시: {"studentName": "홍길동"}
     */
    public String getStudentName() {
        return studentName;
    }

    /** studentName 필드 값을 설정합니다. */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
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
     * JSON 응답 시: {"gradeYear": 2}
     */
    public int getGradeYear() {
        return gradeYear;
    }

    /** gradeYear 필드 값을 설정합니다. */
    public void setGradeYear(int gradeYear) {
        this.gradeYear = gradeYear;
    }

    /**
     * average 필드 값을 반환합니다.
     * JSON 응답 시: {"average": 88.5}
     */
    public double getAverage() {
        return average;
    }

    /** average 필드 값을 설정합니다. */
    public void setAverage(double average) {
        this.average = average;
    }

    /**
     * grade 필드 값을 반환합니다.
     * JSON 응답 시: {"grade": "B"}
     */
    public String getGrade() {
        return grade;
    }

    /** grade 필드 값을 설정합니다. */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /**
     * scoreCount 필드 값을 반환합니다.
     * JSON 응답 시: {"scoreCount": 4}
     */
    public int getScoreCount() {
        return scoreCount;
    }

    /** scoreCount 필드 값을 설정합니다. */
    public void setScoreCount(int scoreCount) {
        this.scoreCount = scoreCount;
    }

    /** 디버깅 시 객체 내용을 출력하기 쉽도록 오버라이드합니다. */
    @Override
    public String toString() {
        return "GradeSummaryDTO{studentId=" + studentId
                + ", studentName='" + studentName + '\''
                + ", email='" + email + '\''
                + ", gradeYear=" + gradeYear
                + ", average=" + average
                + ", grade='" + grade + '\''
                + ", scoreCount=" + scoreCount + '}';
    }
}
