package com.example.spring_api_client_examples.utils.api;

import com.google.gson.Gson; // Gson 임포트
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpUrlConnectionUtil {

    private static final Gson GSON = new Gson();

    // =========================================================================
    // 1. GET (데이터 조회)
    // =========================================================================
    /**
     * @param fullUrl 최종적으로 파라미터까지 붙은 URL
     * @param responseType 응답 데이터의 클래스 타입
     */
    public static <T> ApiResponse<T> get(String fullUrl, Class<T> responseType){
        log.info("--- HttpURLConnection GET 호출 URL: {} ----", fullUrl);

        HttpURLConnection con = null;

        try {
            URL url = new URL(fullUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            return handleResponse(con, responseType);
        }catch (IOException e){
            String details = "Connection or IO Error: " + e.getMessage();
            log.error(details);
            return ApiResponse.fail(500, details);
        }finally {
            if(con != null){
                con.disconnect();
            }
        }
    }

    // =========================================================================
    // 2. POST (데이터 생성)
    // =========================================================================
    public static <T> ApiResponse<T> post(String baseUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpURLConnection POST Util 호출 URL: {} ----", baseUrl);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(baseUrl).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            writeBody(con, requestBody);
            return handleResponse(con, responseType);
        } catch (IOException e) {
            log.error("POST Connection or IO Error: {}", e.getMessage());
            return ApiResponse.fail(500, "POST Connection or IO Error: " + e.getMessage());
        } finally {
            if (con != null) con.disconnect();
        }
    }

    // =========================================================================
    // 3. PUT (데이터 전체 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> put(String baseUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpURLConnection PUT Util 호출 URL: {} ----", baseUrl);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(baseUrl).openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Accept", "application/json");
            writeBody(con, requestBody);
            return handleResponse(con, responseType);
        } catch (IOException e) {
            log.error("PUT Connection or IO Error: {}", e.getMessage());
            return ApiResponse.fail(500, "PUT Connection or IO Error: " + e.getMessage());
        } finally {
            if (con != null) con.disconnect();
        }
    }

    // =========================================================================
    // 4. PATCH (데이터 부분 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> patch(String baseUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpURLConnection PATCH Util 호출 URL: {} ----", baseUrl);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(baseUrl).openConnection();
            con.setRequestMethod("PATCH"); // PATCH 명시
            con.setRequestProperty("Accept", "application/json");
            writeBody(con, requestBody);
            return handleResponse(con, responseType);
        } catch (IOException e) {
            log.error("PATCH Connection or IO Error: {}", e.getMessage());
            return ApiResponse.fail(500, "PATCH Connection or IO Error: " + e.getMessage());
        } finally {
            if (con != null) con.disconnect();
        }
    }

    // =========================================================================
    // 5. DELETE (데이터 삭제)
    // =========================================================================
    public static ApiResponse<Void> delete(String baseUrl) {
        log.info("--- HttpURLConnection DELETE Util 호출 URL: {} ----", baseUrl);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(baseUrl).openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Accept", "application/json");
            con.connect(); // 연결만 수행 (본문 없음)
            return handleResponse(con, Void.class);
        } catch (IOException e) {
            log.error("DELETE Connection or IO Error: {}", e.getMessage());
            return ApiResponse.fail(500, "DELETE Connection or IO Error: " + e.getMessage());
        } finally {
            if (con != null) con.disconnect();
        }
    }

    // =========================================================================
    // Private 헬퍼 메서드: 요청 본문 작성 (Gson 사용)
    // =========================================================================
    private static void writeBody(HttpURLConnection connection, Object requestBody) throws IOException {
        if (requestBody != null) {
            String jsonInputString = GSON.toJson(requestBody);

            log.info("jsonInputString = {} ",jsonInputString);

            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(input, 0, input.length);
                log.info("os={}", os);
            }
        }
    }

    // =========================================================================
    // Private 헬퍼 메서드: 응답 받고 ApiResponse로 변환 (Gson 사용)
    // =========================================================================
    private static <T> ApiResponse<T> handleResponse(HttpURLConnection connection, Class<T> responseType) throws IOException {
        int responseCode = connection.getResponseCode();
        String jsonString = "";

        // 1. 응답 스트림 가져오기
        InputStream stream = null;
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            stream = connection.getErrorStream();
        } else if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            stream = connection.getInputStream();
        }

        // 2. JSON 문자열 읽기
        if (stream != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    log.info("line={}",line);
                }
                jsonString = content.toString();
                log.info("jsonString1={}", jsonString);
            }
        }

        // 3. 응답 코드 및 JSON 내용에 따라 최종 결과 반환 (단일 블록)
        if (responseCode >= 200 && responseCode < 300) {
            if (responseType == Void.class || responseCode == HttpURLConnection.HTTP_NO_CONTENT || jsonString.isEmpty()) {
                return ApiResponse.success(responseCode, null);
            }

            try {
                T data = GSON.fromJson(jsonString, responseType);
                return ApiResponse.success(responseCode, data);
            } catch (Exception e) {
                log.error("JSON Parsing Error for successful response (Gson): {}", e.getMessage());
                return ApiResponse.fail(500, "JSON Parsing Error (Gson): " + e.getMessage());
            }
        } else {
            // 4xx, 5xx 실패 응답
            String details = String.format("HTTP Error %d. Body: %s", responseCode, jsonString);
            log.error("HttpURLConnection Error: {}", details);
            return ApiResponse.fail(responseCode, details);
        }
    }
}