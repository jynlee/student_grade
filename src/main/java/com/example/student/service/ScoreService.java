package com.example.student.service;

import com.example.student.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ScoreService - 성적 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * @Service: root-context.xml 의 component-scan 이 이 어노테이션을 보고
 *           Spring 컨테이너(Bean)에 자동으로 등록합니다.
 *
 * [레이어 구조]
 * ┌─────────────────────────────┐
 * │  ScoreController (웹 계층)   │  HTTP 요청·응답만 담당
 * └─────────────┬───────────────┘
 *               │ 호출
 * ┌─────────────▼───────────────┐
 * │  ScoreService (서비스 계층)   │  비즈니스 로직 담당 ← 이 클래스
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
public class ScoreService {

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
     * [READ] 전체 성적 목록 조회
     *
     * BeanPropertyRowMapper<>(Score.class)
     *   → SELECT 결과의 컬럼명을 Score 클래스의 필드에 자동 매핑합니다.
     *      예) student_id 컬럼 → studentId 필드
     *
     * @return 전체 성적 목록. 데이터가 없으면 빈 리스트(size=0) 반환
     */
    public List<Score> findAll() {
        // TODO: 페이징(paging) 처리 추가 필요 (데이터가 많을 경우 성능 저하)
        //       예) LIMIT ? OFFSET ? 구문을 추가해 한 번에 가져올 건수를 제한
        String sql = "SELECT id, student_id, subject, score FROM scores ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Score.class));
    }

    /**
     * [READ] 특정 성적 1건 조회 (id 로 검색)
     *
     * queryForObject: 결과가 정확히 1건일 때 사용합니다.
     * 결과가 0건이면 EmptyResultDataAccessException 발생 → 호출부에서 처리 필요
     *
     * @param id 조회할 성적의 고유 번호
     * @return 해당 성적 객체
     */
    public Score findById(int id) {
        String sql = "SELECT id, student_id, subject, score FROM scores WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Score.class), id);
    }

    /**
     * [READ] 특정 학생의 모든 성적 조회
     *
     * ScoreController 의 GET /scores?studentId=1 요청을 처리할 때 사용됩니다.
     *
     * @param studentId 조회할 학생의 고유 번호
     * @return 해당 학생의 성적 목록 (없으면 빈 리스트 반환)
     */
    public List<Score> findByStudentId(int studentId) {
        // TODO: subject(과목) 파라미터를 추가해 특정 과목만 필터링하는 기능 추가 필요
        String sql = "SELECT id, student_id, subject, score FROM scores WHERE student_id = ? ORDER BY subject";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Score.class), studentId);
    }

    /**
     * [CREATE] 새 성적 등록
     *
     * jdbcTemplate.update(sql, args...)
     *   → INSERT 쿼리를 실행하고 영향받은 행 수를 반환합니다.
     *
     * id 는 DB 에서 SERIAL(자동 증가)로 처리하므로 INSERT 에 포함하지 않습니다.
     *
     * @param score 등록할 성적 정보
     * @return 영향받은 행 수 (성공: 1)
     */
    public int insert(Score score) {
        // TODO: 같은 학생(studentId)의 같은 과목(subject) 성적이 이미 있는지 중복 검사 필요
        // TODO: 점수 유효 범위 검사 추가 필요 (0 이상 100 이하인지 확인)
        // TODO: studentId 가 실제 students 테이블에 존재하는지 확인하는 로직 추가 필요
        String sql = "INSERT INTO scores (student_id, subject, score) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql,
                score.getStudentId(),
                score.getSubject(),
                score.getScore());
    }

    /**
     * [UPDATE] 성적 수정
     *
     * 반환값이 0이면 해당 id 의 성적이 DB 에 존재하지 않는 것입니다.
     *
     * @param score 수정할 성적 정보 (id 필드가 반드시 포함되어야 합니다)
     * @return 영향받은 행 수 (성공: 1, 해당 id 없으면: 0)
     */
    public int update(Score score) {
        // TODO: 수정 전에 해당 id 의 성적이 존재하는지 확인하는 로직 추가 필요
        // TODO: 점수 유효 범위 검사 추가 필요 (0 이상 100 이하인지 확인)
        String sql = "UPDATE scores SET student_id = ?, subject = ?, score = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                score.getStudentId(),
                score.getSubject(),
                score.getScore(),
                score.getId());
    }

    /**
     * [DELETE] 성적 삭제
     *
     * 반환값이 0이면 해당 id 의 성적이 DB 에 존재하지 않는 것입니다.
     *
     * @param id 삭제할 성적의 고유 번호
     * @return 영향받은 행 수 (성공: 1, 해당 id 없으면: 0)
     */
    public int delete(int id) {
        // TODO: 삭제 전 해당 성적이 존재하는지 확인하는 로직 추가 필요
        String sql = "DELETE FROM scores WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
