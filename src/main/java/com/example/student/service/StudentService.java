package com.example.student.service;

import com.example.student.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * StudentService - 학생 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * @Service: root-context.xml 의 component-scan 이 이 어노테이션을 보고
 *           Spring 컨테이너(Bean)에 자동으로 등록합니다.
 *
 * [레이어 구조]
 * ┌─────────────────────────────┐
 * │  StudentController (웹 계층) │  HTTP 요청·응답만 담당
 * └─────────────┬───────────────┘
 *               │ 호출
 * ┌─────────────▼───────────────┐
 * │  StudentService (서비스 계층) │  비즈니스 로직 담당 ← 이 클래스
 * └─────────────┬───────────────┘
 *               │ 호출
 * ┌─────────────▼───────────────┐
 * │  JdbcTemplate (데이터 계층)  │  SQL 실행
 * └─────────────┬───────────────┘
 *               │
 * ┌─────────────▼───────────────┐
 * │  PostgreSQL DB               │
 * └─────────────────────────────┘
 */
@Service
public class StudentService {

    /**
     * JdbcTemplate: SQL 실행과 결과 매핑을 편리하게 처리해 주는 Spring JDBC 핵심 클래스.
     * root-context.xml 에서 <bean id="jdbcTemplate"> 으로 등록되어 있어
     * @Autowired 로 자동 주입받을 수 있습니다.
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ================================================================
    //  CRUD 메서드 (Create, Read, Update, Delete)
    // ================================================================

    /**
     * [READ] 전체 학생 목록 조회
     *
     * jdbcTemplate.query(sql, RowMapper)
     *   → SQL 을 실행하고, 결과 행(row) 마다 RowMapper 를 적용해 Student 객체 리스트를 만듭니다.
     *
     * BeanPropertyRowMapper<>(Student.class)
     *   → 컬럼명을 자동으로 camelCase 필드에 매핑합니다.
     *      예) grade_year 컬럼 → gradeYear 필드
     *
     * @return 전체 학생 목록. 데이터가 없으면 빈 리스트(size=0) 반환
     */
    public List<Student> findAll() {
        String sql = "SELECT id, name, email, grade_year FROM students ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Student.class));
    }

    /**
     * [READ] 특정 학생 1명 조회 (id 로 검색)
     *
     * jdbcTemplate.queryForObject(sql, RowMapper, args...)
     *   → 결과가 정확히 1건일 때 사용합니다.
     *   → 결과가 0건이면 EmptyResultDataAccessException 발생
     *   → 결과가 2건 이상이면 IncorrectResultSizeDataAccessException 발생
     *
     * SQL 의 ? 자리에 메서드 마지막 인자(id)가 순서대로 바인딩됩니다.
     *
     * @param id 조회할 학생의 고유 번호
     * @return 해당 학생 객체
     */
    public Student findById(int id) {
        String sql = "SELECT id, name, email, grade_year FROM students WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Student.class), id);
    }

    /**
     * [CREATE] 새 학생 등록
     *
     * jdbcTemplate.update(sql, args...)
     *   → INSERT / UPDATE / DELETE 쿼리 실행에 사용합니다.
     *   → 반환값: 영향받은 행(row) 수 (성공 시 1)
     *
     * id 는 DB 에서 SERIAL(자동 증가) 로 처리하므로 INSERT 에 포함하지 않습니다.
     *
     * @param student 등록할 학생 정보
     * @return 영향받은 행 수 (성공: 1)
     */
    public int insert(Student student) {
        // TODO: 동일한 이름 + 이메일 조합이 이미 존재하는지 중복 검사 로직 추가 필요
        // TODO: name 이 null 이거나 빈 문자열인 경우 예외 처리 추가 필요
        String sql = "INSERT INTO students (name, email, grade_year) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql,
                student.getName(),
                student.getEmail(),
                student.getGradeYear());
    }

    /**
     * [UPDATE] 학생 정보 수정
     *
     * student.getId() 로 어떤 행을 수정할지 WHERE 조건을 결정합니다.
     * 반환값이 0이면 해당 id 의 학생이 DB 에 존재하지 않는 것입니다.
     *
     * @param student 수정할 학생 정보 (id 필드가 반드시 포함되어야 합니다)
     * @return 영향받은 행 수 (성공: 1, 해당 id 없으면: 0)
     */
    public int update(Student student) {
        // TODO: 수정 전에 해당 id 의 학생이 존재하는지 먼저 확인하는 로직 추가 필요
        String sql = "UPDATE students SET name = ?, email = ?, grade_year = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                student.getName(),
                student.getEmail(),
                student.getGradeYear(),
                student.getId());
    }

    /**
     * [DELETE] 학생 삭제
     *
     * 반환값이 0이면 해당 id 의 학생이 DB 에 존재하지 않는 것입니다.
     *
     * @param id 삭제할 학생의 고유 번호
     * @return 영향받은 행 수 (성공: 1, 해당 id 없으면: 0)
     */
    public int delete(int id) {
        // TODO: 해당 학생의 성적(scores 테이블)도 함께 삭제 필요
        //       방법 1: 아래에 scores 삭제 SQL 을 먼저 실행
        //       방법 2: DB 에서 ON DELETE CASCADE 외래 키 옵션으로 자동 처리
        String sql = "DELETE FROM students WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // ================================================================
    //  비즈니스 로직 메서드
    // ================================================================

    /**
     * 특정 학생의 전체 과목 평균 점수 계산
     *
     * SQL AVG() 함수를 사용해 scores 테이블에서 해당 학생의 평균을 구합니다.
     * 성적 데이터가 없으면 AVG() 는 null 을 반환하므로, Double (래퍼 타입) 로 받습니다.
     *
     * 사용 예:
     *   double avg = studentService.calculateAverage(1);   // 학생 id=1 의 평균
     *   String grade = studentService.calculateGrade(avg); // 평균으로 등급 계산
     *
     * @param studentId 평균을 계산할 학생의 고유 번호
     * @return 평균 점수 (소수점 포함). 성적이 없으면 0.0 반환
     */
    public double calculateAverage(int studentId) {
        // TODO: 특정 과목만 평균을 계산하는 기능 추가 필요 (subject 파라미터 추가)
        // TODO: 학기(semester) 개념 추가 시 학기별 평균 계산 기능도 필요
        String sql = "SELECT AVG(score) FROM scores WHERE student_id = ?";

        // queryForObject: 단일 숫자 결과를 조회할 때 결과 타입 클래스를 두 번째 인자로 전달
        // Double.class → SQL AVG() 결과를 Double 로 받음
        Double average = jdbcTemplate.queryForObject(sql, Double.class, studentId);

        // SQL AVG() 는 행이 0건이면 null 을 반환 → null 이면 0.0 처리
        return (average != null) ? average : 0.0;
    }

    /**
     * 평균 점수를 입력받아 학점(등급) 문자열 반환
     *
     * 등급 기준 (일반적인 대학교 기준):
     *   90 ~ 100점 → "A"  (우수)
     *   80 ~  89점 → "B"  (양호)
     *   70 ~  79점 → "C"  (보통)
     *   60 ~  69점 → "D"  (미흡)
     *    0 ~  59점 → "F"  (불합격)
     *
     * 사용 예:
     *   studentService.calculateGrade(95.0);  // → "A"
     *   studentService.calculateGrade(73.5);  // → "C"
     *   studentService.calculateGrade(55.0);  // → "F"
     *
     * @param average 평균 점수 (0.0 ~ 100.0)
     * @return 학점 문자열 ("A", "B", "C", "D", "F")
     */
    public String calculateGrade(double average) {
        // TODO: 등급 기준을 DB 나 외부 설정 파일에서 읽어오도록 개선 필요
        //       (현재는 코드에 직접 기준을 작성한 하드코딩 방식)
        if (average >= 90) {
            return "A";
        } else if (average >= 80) {
            return "B";
        } else if (average >= 70) {
            return "C";
        } else if (average >= 60) {
            return "D";
        } else {
            return "F";
        }
    }
}
