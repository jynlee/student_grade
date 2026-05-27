package com.example.student.controller;

import com.example.student.model.Student;
import com.example.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentController - 학생 관련 HTTP 요청을 처리하는 컨트롤러
 *
 * @Controller: Spring MVC 가 이 클래스를 웹 요청 처리 컨트롤러로 인식합니다.
 *              servlet-context.xml 의 component-scan 이 감지해 Bean 으로 등록합니다.
 *
 * @RequestMapping("/students"): 이 클래스의 모든 메서드는
 *                               /students 로 시작하는 URL 요청을 담당합니다.
 *
 * [REST API 엔드포인트]
 *   GET    /students       → 전체 학생 목록 조회
 *   POST   /students       → 새 학생 등록
 *   PUT    /students/{id}  → 특정 학생 정보 수정
 *   DELETE /students/{id}  → 특정 학생 삭제
 *
 * [응답 형식]
 * 모든 응답은 JSON 형식입니다.
 * Jackson 라이브러리(pom.xml 에 포함)가 Java 객체 ↔ JSON 자동 변환을 담당합니다.
 */
@Controller
@RequestMapping("/students")
public class StudentController {

    /**
     * StudentService: 학생 비즈니스 로직을 처리하는 서비스 객체
     * @Autowired: Spring 이 자동으로 StudentService Bean 을 찾아 이 필드에 주입합니다.
     */
    @Autowired
    private StudentService studentService;

    // ================================================================
    //  GET /students - 전체 학생 목록 조회
    // ================================================================

    /**
     * 전체 학생 목록을 JSON 배열로 반환합니다.
     *
     * @GetMapping: HTTP GET 요청만 처리합니다.
     * @ResponseBody: 반환값을 View(JSP)로 보내지 않고, JSON 문자열로 변환해
     *                HTTP 응답 본문(body)에 직접 씁니다.
     *
     * 요청 예시:
     *   GET http://localhost:8080/grade-system/students
     *
     * 응답 예시 (HTTP 200 OK):
     *   [
     *     {"id": 1, "name": "홍길동", "email": "hong@example.com", "gradeYear": 2},
     *     {"id": 2, "name": "김철수", "email": "kim@example.com",  "gradeYear": 3}
     *   ]
     *
     * @return 전체 학생 목록과 HTTP 200 상태코드
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Student>> getStudents() {
        List<Student> students = studentService.findAll();
        // ResponseEntity.ok(body): HTTP 200 OK 와 함께 body 를 JSON 으로 응답
        return ResponseEntity.ok(students);
    }

    // ================================================================
    //  POST /students - 새 학생 등록
    // ================================================================

    /**
     * 요청 본문(JSON)으로 전달된 학생 정보를 DB 에 저장합니다.
     *
     * @PostMapping: HTTP POST 요청만 처리합니다.
     * @RequestBody Student student: HTTP 요청 본문의 JSON 을 Student 객체로 자동 변환합니다.
     *               (Jackson 라이브러리가 처리)
     *               요청 시 Content-Type: application/json 헤더가 반드시 있어야 합니다.
     *
     * 요청 예시:
     *   POST http://localhost:8080/grade-system/students
     *   Content-Type: application/json
     *   Body:
     *     {"name": "홍길동", "email": "hong@example.com", "gradeYear": 2}
     *
     * 성공 응답 (HTTP 201 Created):
     *   {"success": true, "message": "학생이 성공적으로 등록되었습니다."}
     *
     * 실패 응답 (HTTP 500 Internal Server Error):
     *   {"success": false, "message": "학생 등록에 실패했습니다."}
     *
     * @param student 요청 본문 JSON 에서 변환된 학생 정보
     * @return 처리 결과 메시지 (JSON)
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addStudent(@RequestBody Student student) {
        // 결과 메시지를 담을 Map (JSON 으로 변환됩니다)
        Map<String, Object> result = new HashMap<>();

        int rowsAffected = studentService.insert(student);

        if (rowsAffected > 0) {
            result.put("success", true);
            result.put("message", "학생이 성공적으로 등록되었습니다.");
            // HTTP 201 Created: 새 리소스가 성공적으로 생성되었음을 의미
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            result.put("success", false);
            result.put("message", "학생 등록에 실패했습니다.");
            // HTTP 500 Internal Server Error: 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ================================================================
    //  PUT /students/{id} - 특정 학생 정보 수정
    // ================================================================

    /**
     * URL 경로의 id 에 해당하는 학생 정보를 수정합니다.
     *
     * @PutMapping("/{id}"): /students/1, /students/2 처럼
     *                       URL 에 id 가 포함된 HTTP PUT 요청을 처리합니다.
     *
     * @PathVariable int id: URL 경로의 {id} 부분을 int 변수로 받습니다.
     *                       예) PUT /students/3 → id = 3
     *
     * @RequestBody Student student: 요청 본문 JSON 을 Student 객체로 변환합니다.
     *               본문에 id 가 있어도 URL 의 id 를 우선으로 사용합니다.
     *
     * 요청 예시:
     *   PUT http://localhost:8080/grade-system/students/1
     *   Content-Type: application/json
     *   Body:
     *     {"name": "홍길순", "email": "hongsoon@example.com", "gradeYear": 3}
     *
     * 성공 응답 (HTTP 200 OK):
     *   {"success": true, "message": "학생 정보가 성공적으로 수정되었습니다."}
     *
     * 실패 응답 (HTTP 404 Not Found):
     *   {"success": false, "message": "수정할 학생을 찾을 수 없습니다. id: 1"}
     *
     * @param id      URL 경로에서 추출한 학생 고유 번호
     * @param student 요청 본문 JSON 에서 변환된 수정 내용
     * @return 처리 결과 메시지 (JSON)
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable int id,
            @RequestBody Student student) {

        Map<String, Object> result = new HashMap<>();

        // URL 경로의 id 를 student 객체에 세팅합니다.
        // 요청 본문(JSON)의 id 값보다 URL 경로의 id 를 우선 사용하기 위함입니다.
        student.setId(id);

        int rowsAffected = studentService.update(student);

        if (rowsAffected > 0) {
            result.put("success", true);
            result.put("message", "학생 정보가 성공적으로 수정되었습니다.");
            // HTTP 200 OK
            return ResponseEntity.ok(result);
        } else {
            // rowsAffected == 0: 해당 id 의 학생이 DB 에 없음
            result.put("success", false);
            result.put("message", "수정할 학생을 찾을 수 없습니다. id: " + id);
            // HTTP 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // ================================================================
    //  DELETE /students/{id} - 특정 학생 삭제
    // ================================================================

    /**
     * URL 경로의 id 에 해당하는 학생을 DB 에서 삭제합니다.
     *
     * @DeleteMapping("/{id}"): /students/{id} 경로의 HTTP DELETE 요청을 처리합니다.
     *
     * 요청 예시:
     *   DELETE http://localhost:8080/grade-system/students/1
     *
     * 성공 응답 (HTTP 200 OK):
     *   {"success": true, "message": "학생이 성공적으로 삭제되었습니다. id: 1"}
     *
     * 실패 응답 (HTTP 404 Not Found):
     *   {"success": false, "message": "삭제할 학생을 찾을 수 없습니다. id: 1"}
     *
     * @param id URL 경로에서 추출한 삭제할 학생의 고유 번호
     * @return 처리 결과 메시지 (JSON)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();

        int rowsAffected = studentService.delete(id);

        if (rowsAffected > 0) {
            result.put("success", true);
            result.put("message", "학생이 성공적으로 삭제되었습니다. id: " + id);
            // HTTP 200 OK
            return ResponseEntity.ok(result);
        } else {
            // rowsAffected == 0: 해당 id 의 학생이 DB 에 없음
            result.put("success", false);
            result.put("message", "삭제할 학생을 찾을 수 없습니다. id: " + id);
            // HTTP 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
