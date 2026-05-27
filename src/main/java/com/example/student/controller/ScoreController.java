package com.example.student.controller;

import com.example.student.model.Score;
import com.example.student.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScoreController - 성적 관련 HTTP 요청을 처리하는 컨트롤러
 *
 * @Controller: Spring MVC 가 이 클래스를 웹 요청 처리 컨트롤러로 인식합니다.
 *              servlet-context.xml 의 component-scan 이 감지해 Bean 으로 등록합니다.
 *
 * @RequestMapping("/scores"): 이 클래스의 모든 메서드는
 *                             /scores 로 시작하는 URL 요청을 담당합니다.
 *
 * [REST API 엔드포인트]
 *   GET  /scores              → 전체 성적 목록 조회
 *   GET  /scores?studentId=1  → 특정 학생의 성적 목록 조회 (쿼리 파라미터 사용)
 *   POST /scores              → 새 성적 등록
 *
 * [응답 형식]
 * 모든 응답은 JSON 형식입니다.
 * Jackson 라이브러리(pom.xml 에 포함)가 Java 객체 ↔ JSON 자동 변환을 담당합니다.
 */
@Controller
@RequestMapping("/scores")
public class ScoreController {

    /**
     * ScoreService: 성적 비즈니스 로직을 처리하는 서비스 객체
     * @Autowired: Spring 이 자동으로 ScoreService Bean 을 찾아 이 필드에 주입합니다.
     */
    @Autowired
    private ScoreService scoreService;

    // ================================================================
    //  GET /scores - 성적 목록 조회 (전체 또는 특정 학생)
    // ================================================================

    /**
     * 성적 목록을 JSON 배열로 반환합니다.
     * 쿼리 파라미터 studentId 가 있으면 해당 학생의 성적만, 없으면 전체 성적을 반환합니다.
     *
     * @GetMapping: HTTP GET 요청만 처리합니다.
     * @ResponseBody: 반환값을 JSON 으로 변환해 응답 본문에 직접 씁니다.
     *
     * @RequestParam(required = false) Integer studentId
     *   → URL 뒤의 ?studentId=1 같은 쿼리 파라미터를 Integer 변수로 받습니다.
     *   → required = false: 이 파라미터가 없어도 오류가 발생하지 않습니다.
     *   → int 가 아닌 Integer(래퍼 타입)를 사용하는 이유:
     *        파라미터가 없을 때 null 로 처리하기 위함입니다.
     *        기본형 int 는 null 을 가질 수 없습니다.
     *
     * 요청 예시 1 - 전체 성적 조회:
     *   GET http://localhost:8080/grade-system/scores
     *
     * 요청 예시 2 - 특정 학생 성적 조회 (학생 id = 1):
     *   GET http://localhost:8080/grade-system/scores?studentId=1
     *
     * 응답 예시 (HTTP 200 OK):
     *   [
     *     {"id": 1, "studentId": 1, "subject": "수학", "score": 95},
     *     {"id": 2, "studentId": 1, "subject": "영어", "score": 88}
     *   ]
     *
     * @param studentId 조회할 학생 ID (선택적. 없으면 전체 조회)
     * @return 성적 목록과 HTTP 200 상태코드
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Score>> getScores(
            @RequestParam(required = false) Integer studentId) {

        List<Score> scores;

        if (studentId != null) {
            // studentId 파라미터가 있으면 해당 학생의 성적만 조회
            scores = scoreService.findByStudentId(studentId);
        } else {
            // studentId 파라미터가 없으면 전체 성적 조회
            scores = scoreService.findAll();
        }

        // ResponseEntity.ok(body): HTTP 200 OK 와 함께 body 를 JSON 으로 응답
        return ResponseEntity.ok(scores);
    }

    // ================================================================
    //  POST /scores - 새 성적 등록
    // ================================================================

    /**
     * 요청 본문(JSON)으로 전달된 성적 정보를 DB 에 저장합니다.
     *
     * @PostMapping: HTTP POST 요청만 처리합니다.
     * @RequestBody Score score: HTTP 요청 본문의 JSON 을 Score 객체로 자동 변환합니다.
     *               요청 시 Content-Type: application/json 헤더가 반드시 있어야 합니다.
     *
     * 요청 예시:
     *   POST http://localhost:8080/grade-system/scores
     *   Content-Type: application/json
     *   Body:
     *     {"studentId": 1, "subject": "수학", "score": 95}
     *
     * 성공 응답 (HTTP 201 Created):
     *   {"success": true, "message": "성적이 성공적으로 등록되었습니다."}
     *
     * 실패 응답 (HTTP 500 Internal Server Error):
     *   {"success": false, "message": "성적 등록에 실패했습니다."}
     *
     * @param score 요청 본문 JSON 에서 변환된 성적 정보
     * @return 처리 결과 메시지 (JSON)
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addScore(@RequestBody Score score) {
        // 결과 메시지를 담을 Map (JSON 으로 변환됩니다)
        Map<String, Object> result = new HashMap<>();

        int rowsAffected = scoreService.insert(score);

        if (rowsAffected > 0) {
            result.put("success", true);
            result.put("message", "성적이 성공적으로 등록되었습니다.");
            // HTTP 201 Created: 새 리소스가 성공적으로 생성되었음을 의미
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            result.put("success", false);
            result.put("message", "성적 등록에 실패했습니다.");
            // HTTP 500 Internal Server Error: 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
