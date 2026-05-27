<%--
  index.jsp - 성적 관리 시스템 메인 페이지

  [위치]
    src/main/webapp/index.jsp
    → http://localhost:8080/grade-system/ 로 직접 접근 가능합니다.

  [동작 방식]
    이 페이지는 서버에서 HTML을 만들어 보내는 방식(SSR)이 아닌,
    브라우저에서 fetch API를 통해 REST API를 호출하는 방식(CSR)을 사용합니다.
      1. 페이지 로드 → JavaScript가 GET /students 호출
      2. 서버가 JSON 응답 → JavaScript가 테이블을 동적으로 그림
      3. 사용자 입력 → POST /students 호출 → 서버 저장 후 목록 새로고침

  [JSP 표현식 언어(EL)]
    ${pageContext.request.contextPath}
    → 웹 애플리케이션 루트 경로를 자동으로 가져옵니다.
    → 예) /grade-system
    → API URL 하드코딩을 피하기 위해 사용합니다.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>성적 관리 시스템</title>

    <%-- ============================================================
         CSS 스타일 (외부 파일 없이 <style> 태그 안에 직접 작성)
         ============================================================ --%>
    <style>

        /* -------------------------------------------------------
           전역 초기화 및 기본 설정
           box-sizing: border-box → padding/border를 width 안에 포함
           ------------------------------------------------------- */
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Malgun Gothic', '맑은 고딕', Arial, sans-serif;
            background-color: #f0f2f5;
            color: #333;
            padding: 20px;
            min-height: 100vh;
        }

        /* -------------------------------------------------------
           헤더
           ------------------------------------------------------- */
        .page-header {
            text-align: center;
            background: linear-gradient(135deg, #2c3e50, #3498db);
            color: white;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 25px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }

        .page-header h1 {
            font-size: 1.8rem;
            letter-spacing: 1px;
        }

        .page-header p {
            margin-top: 8px;
            font-size: 0.9rem;
            opacity: 0.85;
        }

        /* -------------------------------------------------------
           카드 컴포넌트 (각 섹션을 감싸는 흰 박스)
           ------------------------------------------------------- */
        .card {
            background-color: #ffffff;
            border-radius: 10px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
            padding: 25px;
            margin-bottom: 25px;
        }

        /* 카드 제목 (왼쪽 파란 세로 선) */
        .card-title {
            font-size: 1.1rem;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 18px;
            padding-left: 12px;
            border-left: 4px solid #3498db;
        }

        /* -------------------------------------------------------
           폼(Form) 레이아웃
           ------------------------------------------------------- */
        .form-row {
            display: flex;        /* 가로 방향으로 배치 */
            flex-wrap: wrap;      /* 화면이 좁으면 줄바꿈 */
            gap: 15px;            /* 요소 사이 간격 */
            align-items: flex-end; /* 버튼과 입력 필드 세로 정렬 */
        }

        /* 개별 입력 필드 묶음 (label + input 한 세트) */
        .form-field {
            display: flex;
            flex-direction: column; /* label 위, input 아래 */
            gap: 5px;
            min-width: 150px;
        }

        /* 필드 레이블 */
        .form-field label {
            font-size: 0.82rem;
            font-weight: bold;
            color: #555;
        }

        /* 필수 표시 별표 */
        .required-mark {
            color: #e74c3c;
            margin-left: 2px;
        }

        /* 입력 필드 */
        .form-field input {
            padding: 9px 12px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 0.92rem;
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
            background-color: #fafafa;
        }

        /* 포커스 시 파란 테두리 */
        .form-field input:focus {
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
            background-color: #fff;
        }

        /* -------------------------------------------------------
           버튼
           ------------------------------------------------------- */
        .btn {
            padding: 9px 22px;
            border: none;
            border-radius: 5px;
            font-size: 0.9rem;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.2s, transform 0.1s;
            white-space: nowrap; /* 버튼 텍스트 줄바꿈 방지 */
        }

        /* 버튼 클릭 시 살짝 눌리는 효과 */
        .btn:active {
            transform: scale(0.97);
        }

        /* 파란 기본 버튼 */
        .btn-primary {
            background-color: #3498db;
            color: white;
        }
        .btn-primary:hover {
            background-color: #2980b9;
        }

        /* 빨간 위험 버튼 (삭제) */
        .btn-danger {
            background-color: #e74c3c;
            color: white;
            padding: 5px 14px;
            font-size: 0.82rem;
        }
        .btn-danger:hover {
            background-color: #c0392b;
        }

        /* 회색 보조 버튼 */
        .btn-secondary {
            background-color: #95a5a6;
            color: white;
            font-size: 0.85rem;
            padding: 7px 16px;
        }
        .btn-secondary:hover {
            background-color: #7f8c8d;
        }

        /* -------------------------------------------------------
           알림 메시지 (성공 / 오류)
           ------------------------------------------------------- */
        .alert {
            padding: 12px 16px;
            border-radius: 5px;
            margin-bottom: 15px;
            font-size: 0.88rem;
            font-weight: bold;
            display: none; /* 기본은 숨김, JS로 보이게 전환 */
        }

        /* 성공 (초록) */
        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        /* 오류 (빨강) */
        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        /* -------------------------------------------------------
           테이블
           ------------------------------------------------------- */
        /* 가로 스크롤 컨테이너 (좁은 화면 대응) */
        .table-wrap {
            overflow-x: auto;
            margin-top: 15px;
        }

        table {
            width: 100%;
            border-collapse: collapse; /* 셀 사이 이중 테두리 제거 */
        }

        /* 헤더 행 */
        thead tr {
            background-color: #2c3e50;
            color: white;
        }

        th {
            padding: 13px 16px;
            text-align: left;
            font-size: 0.88rem;
            letter-spacing: 0.5px;
        }

        td {
            padding: 11px 16px;
            border-bottom: 1px solid #eee;
            font-size: 0.9rem;
            vertical-align: middle;
        }

        /* 짝수 행 배경 (가독성) */
        tbody tr:nth-child(even) {
            background-color: #f8f9fa;
        }

        /* 행 호버 효과 */
        tbody tr:hover {
            background-color: #eaf4fb;
        }

        /* 데이터 없을 때 안내 문구 */
        .empty-msg {
            text-align: center;
            color: #aaa;
            padding: 30px 0;
            font-style: italic;
            font-size: 0.9rem;
        }

        /* -------------------------------------------------------
           등급 뱃지 (A/B/C/D/F를 색상 태그로 표시)
           ------------------------------------------------------- */
        .grade-badge {
            display: inline-block;
            padding: 3px 11px;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: bold;
            color: white;
        }

        .grade-A { background-color: #27ae60; } /* 초록 - 우수 */
        .grade-B { background-color: #2980b9; } /* 파랑 - 양호 */
        .grade-C { background-color: #f39c12; } /* 주황 - 보통 */
        .grade-D { background-color: #e67e22; } /* 진주황 - 미흡 */
        .grade-F { background-color: #e74c3c; } /* 빨강 - 불합격 */

        /* -------------------------------------------------------
           API 사용법 설명 코드 블록
           ------------------------------------------------------- */
        .code-block {
            background-color: #2c3e50;
            color: #ecf0f1;
            border-radius: 6px;
            padding: 15px 18px;
            font-family: 'Consolas', 'D2Coding', monospace;
            font-size: 0.82rem;
            line-height: 1.7;
            overflow-x: auto;
            margin-top: 10px;
            white-space: pre;
        }

        /* 코드 블록 내 강조 색상 */
        .code-method  { color: #e74c3c; font-weight: bold; }
        .code-url     { color: #2ecc71; }
        .code-comment { color: #95a5a6; }

        /* -------------------------------------------------------
           반응형 (좁은 화면)
           ------------------------------------------------------- */
        @media (max-width: 600px) {
            body { padding: 10px; }
            .form-row { flex-direction: column; }
            .form-field { min-width: 100%; }
            .btn { width: 100%; }
        }

    </style>
</head>
<body>

    <%-- ============================================================
         헤더 영역
         ============================================================ --%>
    <div class="page-header">
        <h1>성적 관리 시스템</h1>
        <p>학생 등록, 성적 입력, 등급 조회를 한 곳에서 관리합니다.</p>
    </div>


    <%-- ============================================================
         섹션 1: 학생 등록 폼
         - form 태그의 onsubmit="return false": 기본 폼 제출(페이지 새로고침)을 막습니다.
           fetch API로 비동기 전송하기 때문에 페이지 이동이 필요 없습니다.
         ============================================================ --%>
    <div class="card">
        <h2 class="card-title">학생 등록</h2>

        <%-- 알림 메시지 표시 영역 (JS의 showAlert()가 여기에 메시지를 표시합니다) --%>
        <div id="register-alert" class="alert"></div>

        <form id="student-form" onsubmit="return false">
            <div class="form-row">

                <%-- 이름 입력 --%>
                <div class="form-field">
                    <label for="input-name">
                        이름 <span class="required-mark">*</span>
                    </label>
                    <input type="text"
                           id="input-name"
                           placeholder="홍길동"
                           maxlength="50"
                           required />
                </div>

                <%-- 이메일 입력 (선택) --%>
                <div class="form-field">
                    <label for="input-email">이메일</label>
                    <input type="email"
                           id="input-email"
                           placeholder="hong@example.com"
                           maxlength="100" />
                </div>

                <%-- 학년 입력 (선택) --%>
                <div class="form-field">
                    <label for="input-grade-year">학년</label>
                    <input type="number"
                           id="input-grade-year"
                           placeholder="1~4"
                           min="1"
                           max="4"
                           style="width: 80px" />
                </div>

                <%-- 등록 버튼 --%>
                <button type="button"
                        class="btn btn-primary"
                        onclick="registerStudent()">
                    + 등록
                </button>

            </div>
        </form>
    </div>


    <%-- ============================================================
         섹션 2: 학생 목록 테이블
         - <tbody id="student-tbody"> 안의 내용은
           JavaScript의 loadStudents() 함수가 동적으로 채웁니다.
         ============================================================ --%>
    <div class="card">
        <h2 class="card-title">학생 목록</h2>

        <button class="btn btn-secondary" onclick="loadStudents()">
            ↻ 새로고침
        </button>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>학년</th>
                        <th>삭제</th>
                    </tr>
                </thead>
                <tbody id="student-tbody">
                    <%-- JavaScript가 채워 넣을 영역 --%>
                    <tr>
                        <td colspan="5" class="empty-msg">데이터를 불러오는 중...</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>


    <%-- ============================================================
         섹션 3: REST API 사용법 안내 (학습용)
         ============================================================ --%>
    <div class="card">
        <h2 class="card-title">REST API 엔드포인트 안내</h2>

        <p style="font-size:0.88rem; color:#666; margin-bottom:12px;">
            아래 URL로 직접 HTTP 요청을 보낼 수 있습니다.
            (Postman, curl, fetch API 등 사용 가능)
        </p>

        <%-- 컨텍스트 경로는 JSP EL로 자동 삽입됩니다 --%>
        <div class="code-block"><span class="code-comment"># 학생 전체 조회</span>
<span class="code-method">GET</span>    <span class="code-url">${pageContext.request.contextPath}/students</span>

<span class="code-comment"># 학생 등록 (Body: JSON)</span>
<span class="code-method">POST</span>   <span class="code-url">${pageContext.request.contextPath}/students</span>
       { "name": "홍길동", "email": "hong@example.com", "gradeYear": 2 }

<span class="code-comment"># 학생 수정 (Body: JSON)</span>
<span class="code-method">PUT</span>    <span class="code-url">${pageContext.request.contextPath}/students/{id}</span>
       { "name": "홍길순", "email": "new@example.com", "gradeYear": 3 }

<span class="code-comment"># 학생 삭제</span>
<span class="code-method">DELETE</span> <span class="code-url">${pageContext.request.contextPath}/students/{id}</span>

<span class="code-comment"># 성적 전체 조회</span>
<span class="code-method">GET</span>    <span class="code-url">${pageContext.request.contextPath}/scores</span>

<span class="code-comment"># 특정 학생 성적만 조회 (쿼리 파라미터)</span>
<span class="code-method">GET</span>    <span class="code-url">${pageContext.request.contextPath}/scores?studentId=1</span>

<span class="code-comment"># 성적 등록 (Body: JSON)</span>
<span class="code-method">POST</span>   <span class="code-url">${pageContext.request.contextPath}/scores</span>
       { "studentId": 1, "subject": "수학", "score": 95 }</div>
    </div>


    <%-- ============================================================
         JavaScript (외부 파일 없이 <script> 태그 안에 직접 작성)

         [핵심 개념]
         fetch API: 브라우저에서 서버로 HTTP 요청을 보내는 현대적인 방법입니다.
         async/await: Promise를 읽기 쉽게 작성하는 문법입니다.
           - async 키워드: 이 함수가 비동기 함수임을 선언합니다.
           - await 키워드: Promise가 완료될 때까지 기다립니다.
                          (async 함수 안에서만 사용 가능합니다)
         ============================================================ --%>
    <script>

        /*
         * [컨텍스트 경로 설정]
         * JSP EL ${pageContext.request.contextPath} 를 JavaScript 변수에 저장합니다.
         *
         * 렌더링 결과 예시:
         *   const BASE_URL = '/grade-system';
         *
         * 이렇게 하면 서버에 배포 경로가 바뀌어도 코드를 수정하지 않아도 됩니다.
         */
        const BASE_URL = '${pageContext.request.contextPath}';


        /* ==============================================================
           1. GET /students - 학생 목록 조회
           ==============================================================
           실행 시점:
             - 페이지 처음 로드될 때 (window.onload)
             - "새로고침" 버튼 클릭 시
             - 학생 등록/삭제 후 자동 호출
           ============================================================== */

        /**
         * 서버에서 전체 학생 목록을 가져와 테이블에 표시합니다.
         *
         * [fetch 흐름 요약]
         *   1) fetch(url)             → HTTP GET 요청 전송
         *   2) await response         → 서버 응답 대기
         *   3) response.json()        → 응답 본문을 JSON 파싱
         *   4) await students         → 파싱 완료 대기
         *   5) renderStudentTable()   → 파싱된 배열로 테이블 그리기
         */
        async function loadStudents() {
            try {
                /*
                 * fetch(url): 서버에 HTTP GET 요청을 보냅니다.
                 * await: 서버 응답이 올 때까지 다음 줄로 넘어가지 않고 기다립니다.
                 * response: HTTP 응답 전체 객체 (상태코드 + 헤더 + 본문)
                 */
                const response = await fetch(BASE_URL + '/students');

                /*
                 * response.ok: HTTP 상태코드가 200~299 범위이면 true입니다.
                 * 404, 500 같은 오류 코드이면 false입니다.
                 *
                 * 주의: fetch는 HTTP 오류 코드(404, 500)가 와도 예외를 던지지 않습니다.
                 *       response.ok를 직접 확인해야 합니다.
                 */
                if (!response.ok) {
                    throw new Error('서버 오류: HTTP ' + response.status);
                }

                /*
                 * response.json(): 응답 본문(JSON 문자열)을 파싱해
                 *                  JavaScript 배열/객체로 변환합니다.
                 * await: 파싱이 완료될 때까지 기다립니다.
                 *
                 * students의 형태:
                 *   [
                 *     { id: 1, name: "홍길동", email: "hong@example.com", gradeYear: 2 },
                 *     { id: 2, name: "김철수", email: "kim@example.com",  gradeYear: 3 }
                 *   ]
                 */
                const students = await response.json();

                // 파싱된 학생 배열로 테이블을 그립니다.
                renderStudentTable(students);

            } catch (error) {
                /*
                 * try-catch: 네트워크 오류, 서버 오류, 파싱 오류를 모두 잡습니다.
                 * console.error(): 브라우저 개발자 도구 콘솔에 오류를 출력합니다.
                 * F12 → Console 탭에서 확인할 수 있습니다.
                 */
                console.error('[loadStudents] 오류:', error);
                document.getElementById('student-tbody').innerHTML =
                    '<tr><td colspan="5" class="empty-msg">'
                    + '목록을 불러오지 못했습니다. 서버를 확인하세요.</td></tr>';
            }
        }

        /**
         * 학생 배열을 받아서 테이블 <tbody>를 동적으로 생성합니다.
         *
         * @param {Array} students - 학생 객체 배열
         *
         * [템플릿 리터럴 사용]
         * 백틱(`) 문자로 감싼 문자열 안에서
         *   ${변수} 형태로 변수를 삽입할 수 있습니다.
         *   여러 줄 문자열도 자유롭게 작성할 수 있습니다.
         */
        function renderStudentTable(students) {
            const tbody = document.getElementById('student-tbody');

            // 학생이 없는 경우
            if (!students || students.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="5" class="empty-msg">등록된 학생이 없습니다.</td></tr>';
                return;
            }

            /*
             * Array.map(): 배열의 각 요소를 변환해 새 배열을 만듭니다.
             *   students 배열의 각 학생 객체 → HTML <tr> 문자열
             *
             * Array.join(''): 배열 요소들을 하나의 문자열로 합칩니다.
             *   join() 없이 innerHTML에 배열을 넣으면 쉼표가 섞입니다.
             *
             * student.email || '-': email이 null/빈문자열이면 '-'를 표시합니다.
             */
            tbody.innerHTML = students.map(function(student) {
                return '<tr>'
                    + '<td>' + student.id + '</td>'
                    + '<td><strong>' + student.name + '</strong></td>'
                    + '<td>' + (student.email     || '-') + '</td>'
                    + '<td>' + (student.gradeYear ? student.gradeYear + '학년' : '-') + '</td>'
                    + '<td>'
                    +   '<button class="btn btn-danger"'
                    +           ' onclick="deleteStudent(' + student.id + ', \'' + student.name + '\')">'
                    +     '삭제'
                    +   '</button>'
                    + '</td>'
                    + '</tr>';
            }).join('');
        }


        /* ==============================================================
           2. POST /students - 학생 등록
           ==============================================================
           실행 시점: "등록" 버튼 클릭 시
           ============================================================== */

        /**
         * 폼 입력값을 읽어서 서버에 새 학생을 등록합니다.
         *
         * [POST 요청과 GET 요청의 차이]
         * GET:  URL에 파라미터가 포함됨 (?name=홍길동&email=...)
         *       데이터 조회에 사용
         * POST: 요청 본문(body)에 데이터를 JSON으로 담아 전송
         *       데이터 생성/변경에 사용, URL에 파라미터가 노출되지 않음
         */
        async function registerStudent() {
            // 입력 필드에서 값 읽기
            const nameInput      = document.getElementById('input-name');
            const emailInput     = document.getElementById('input-email');
            const gradeYearInput = document.getElementById('input-grade-year');

            /*
             * .value: input 요소의 현재 입력값을 읽습니다.
             * .trim(): 앞뒤 공백 제거 (사용자가 실수로 공백만 입력하는 경우 방지)
             */
            const name      = nameInput.value.trim();
            const email     = emailInput.value.trim();

            /*
             * parseInt(): 문자열을 정수로 변환합니다.
             * || 0: 값이 없거나 변환 실패 시(NaN) 0으로 대체합니다.
             */
            const gradeYear = parseInt(gradeYearInput.value) || 0;

            // 이름은 필수 입력 항목
            if (!name) {
                showAlert('register-alert', '이름을 입력해 주세요.', 'error');
                nameInput.focus(); // 이름 입력 필드로 커서 이동
                return;           // 함수 종료 (등록 중단)
            }

            /*
             * 서버로 보낼 데이터를 JavaScript 객체로 만듭니다.
             * 이 객체가 JSON.stringify()를 통해 JSON 문자열로 변환됩니다.
             *
             * 예) { name: "홍길동", email: "hong@example.com", gradeYear: 2 }
             *     → '{"name":"홍길동","email":"hong@example.com","gradeYear":2}'
             */
            var studentData = {
                name:      name,
                email:     email,
                gradeYear: gradeYear
            };

            try {
                /*
                 * fetch(url, options):
                 *   두 번째 인자로 옵션 객체를 전달해 GET 외 HTTP 메서드를 사용합니다.
                 *
                 *   method: 'POST'
                 *     → HTTP 메서드를 POST로 지정합니다.
                 *
                 *   headers: { 'Content-Type': 'application/json' }
                 *     → 요청 본문이 JSON 형식임을 서버에 알려줍니다.
                 *     → 이 헤더가 없으면 서버의 @RequestBody가 JSON을 읽지 못합니다.
                 *
                 *   body: JSON.stringify(studentData)
                 *     → JavaScript 객체를 JSON 문자열로 변환해 요청 본문에 담습니다.
                 *     → 서버의 StudentController @RequestBody Student student 가 이 JSON을 받습니다.
                 */
                const response = await fetch(BASE_URL + '/students', {
                    method:  'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify(studentData)
                });

                /*
                 * 서버 응답을 JSON으로 파싱합니다.
                 * StudentController가 반환하는 형태:
                 *   성공: { "success": true,  "message": "학생이 성공적으로 등록되었습니다." }
                 *   실패: { "success": false, "message": "학생 등록에 실패했습니다." }
                 */
                const result = await response.json();

                if (response.ok) {
                    // 성공 처리
                    showAlert('register-alert', result.message || '등록되었습니다.', 'success');

                    /*
                     * form.reset(): 폼 안의 모든 input 값을 초기화합니다.
                     * 등록 후 입력 필드를 비워 다음 학생을 바로 입력할 수 있게 합니다.
                     */
                    document.getElementById('student-form').reset();

                    // 등록 후 목록 자동 새로고침
                    loadStudents();
                } else {
                    showAlert('register-alert', result.message || '등록에 실패했습니다.', 'error');
                }

            } catch (error) {
                console.error('[registerStudent] 오류:', error);
                showAlert('register-alert', '서버와 통신 중 오류가 발생했습니다.', 'error');
            }
        }


        /* ==============================================================
           3. DELETE /students/{id} - 학생 삭제
           ==============================================================
           실행 시점: 테이블의 "삭제" 버튼 클릭 시
           ============================================================== */

        /**
         * 특정 학생을 DB에서 삭제합니다.
         *
         * @param {number} id   삭제할 학생의 고유 번호
         * @param {string} name 확인 대화상자에 표시할 학생 이름
         *
         * [DELETE 요청]
         * URL 경로에 삭제할 자원의 id를 포함시켜 보냅니다.
         * 예) DELETE /grade-system/students/3
         *     → StudentController의 @DeleteMapping("/{id}")가 처리
         *     → @PathVariable int id 로 3을 받음
         */
        async function deleteStudent(id, name) {
            /*
             * confirm(): 브라우저 기본 확인 대화상자를 띄웁니다.
             * 사용자가 "확인"을 누르면 true, "취소"를 누르면 false를 반환합니다.
             * 실수로 삭제하는 상황을 방지하기 위한 안전장치입니다.
             */
            if (!confirm('[' + name + '] 학생을 삭제하시겠습니까?\n삭제하면 복구할 수 없습니다.')) {
                return; // "취소" 클릭 시 함수 종료
            }

            try {
                const response = await fetch(BASE_URL + '/students/' + id, {
                    method: 'DELETE'
                    /*
                     * DELETE 요청은 본문(body)이 없어도 됩니다.
                     * 삭제할 대상은 URL의 id만으로 특정됩니다.
                     */
                });

                const result = await response.json();

                if (response.ok) {
                    // 성공: 알림 후 목록 새로고침
                    alert(result.message || '삭제되었습니다.');
                    loadStudents();
                } else {
                    // 실패: 예) 해당 id가 없는 경우 (HTTP 404)
                    alert(result.message || '삭제에 실패했습니다.');
                }

            } catch (error) {
                console.error('[deleteStudent] 오류:', error);
                alert('서버와 통신 중 오류가 발생했습니다.');
            }
        }


        /* ==============================================================
           유틸리티 함수
           ============================================================== */

        /**
         * 알림 메시지를 화면에 표시하고 3초 후 자동으로 숨깁니다.
         *
         * @param {string} elementId 알림을 표시할 div의 id 속성값
         * @param {string} message   표시할 메시지 내용
         * @param {string} type      'success'(초록) 또는 'error'(빨강)
         */
        function showAlert(elementId, message, type) {
            var alertDiv = document.getElementById(elementId);

            /*
             * className 설정으로 CSS 스타일을 전환합니다.
             * 예) type='success' → alertDiv.className = 'alert alert-success'
             *     → CSS의 .alert-success 규칙이 적용됩니다.
             */
            alertDiv.className      = 'alert alert-' + type;
            alertDiv.textContent    = message;   // 메시지 내용 설정
            alertDiv.style.display  = 'block';   // 숨김 → 보임

            /*
             * setTimeout(함수, 밀리초): 지정한 시간 후에 함수를 실행합니다.
             * 3000ms = 3초 후에 알림 메시지를 자동으로 숨깁니다.
             */
            setTimeout(function() {
                alertDiv.style.display = 'none';
            }, 3000);
        }


        /* ==============================================================
           페이지 초기화
           ==============================================================
           window.onload: HTML, CSS, 이미지, JS 등 모든 리소스가
                          완전히 로드된 후 실행됩니다.
           페이지를 열면 자동으로 학생 목록을 서버에서 가져와 테이블에 표시합니다.
           ============================================================== */
        window.onload = function() {
            loadStudents();
        };

    </script>

</body>
</html>
