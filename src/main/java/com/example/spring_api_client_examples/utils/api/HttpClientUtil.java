package com.example.spring_api_client_examples.utils.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    // [핵심 설정] Jackson ObjectMapper 사용 및 설정
    private static final ObjectMapper OBJECT_MAPPER;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    static {
        OBJECT_MAPPER = new ObjectMapper();
        // DTO 접근 권한 강제 설정 (null 문제 최종 해결)
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
    }

    // =========================================================================
    // 1. GET (데이터 조회)
    // =========================================================================
    public static <T> ApiResponse<T> get(String baseUrl, Map<String, Object> params, Class<T> responseType){
        String finalUrl = UrlBuilder.buildUrlWithParams(baseUrl, params);
        log.info("--- HttpClient GET 호출 URL: {} ----", finalUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .GET()
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return sendAndHandleResponse(request, responseType);
        } catch (IOException e){
            String details = "Connection or IO Error: " + e.getMessage();
            log.error(details);
            return ApiResponse.fail(500, details);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            String details = "Request Interrupted: " + e.getMessage();
            log.error(details);
            return ApiResponse.fail(500, details);
        }
    }

    // =========================================================================
    // 2. POST (데이터 생성)
    // =========================================================================
    public static <T> ApiResponse<T> post(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpClient POST 호출 URL: {} ----", fullUrl);
        try {
            String jsonInputString = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return sendAndHandleResponse(request, responseType);
        } catch (IOException e) {
            log.error("POST Client Error: {}", e.getMessage());
            return ApiResponse.fail(500, "POST Client Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("POST Client Interrupted: {}", e.getMessage());
            return ApiResponse.fail(500, "POST Request Interrupted.");
        }
    }

    // =========================================================================
    // 3. PUT (데이터 전체 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> put(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpClient PUT 호출 URL: {} ----", fullUrl);
        try {
            String jsonInputString = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return sendAndHandleResponse(request, responseType);
        } catch (IOException e) {
            log.error("PUT Client Error: {}", e.getMessage());
            return ApiResponse.fail(500, "PUT Client Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PUT Client Interrupted: {}", e.getMessage());
            return ApiResponse.fail(500, "PUT Request Interrupted.");
        }
    }

    // =========================================================================
    // 4. PATCH (데이터 부분 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> patch(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("--- HttpClient PATCH 호출 URL: {} ----", fullUrl);
        try {
            String jsonInputString = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return sendAndHandleResponse(request, responseType);
        } catch (IOException e) {
            log.error("PATCH Client Error: {}", e.getMessage());
            return ApiResponse.fail(500, "PATCH Client Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PATCH Client Interrupted: {}", e.getMessage());
            return ApiResponse.fail(500, "PATCH Request Interrupted.");
        }
    }

    // =========================================================================
    // 5. DELETE (데이터 삭제)
    // =========================================================================
    public static ApiResponse<Void> delete(String fullUrl) {
        log.info("--- HttpClient DELETE 호출 URL: {} ----", fullUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .DELETE()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            return sendAndHandleResponse(request, Void.class);
        } catch (IOException e) {
            log.error("DELETE Client Error: {}", e.getMessage());
            return ApiResponse.fail(500, "DELETE Client Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("DELETE Client Interrupted: {}", e.getMessage());
            return ApiResponse.fail(500, "DELETE Request Interrupted.");
        }
    }

    // =========================================================================
    // Private: 요청 전송 및 응답 처리 (공통 로직)
    // =========================================================================
    private static <T> ApiResponse<T> sendAndHandleResponse(HttpRequest request, Class<T> responseType)
            throws IOException, InterruptedException {

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            if (responseType == Void.class || responseBody == null || responseBody.isEmpty()) {
                return ApiResponse.success(statusCode, null);
            }
            try {
                T data = OBJECT_MAPPER.readValue(responseBody, responseType);
                return ApiResponse.success(statusCode, data);
            } catch (IOException e) {
                log.error("JSON Parsing Error for successful response: {}", e.getMessage());
                return ApiResponse.fail(500, "JSON Parsing Error: " + e.getMessage());
            }
        } else {
            String details = String.format("API Error %d. Body: %s", statusCode, responseBody);
            return ApiResponse.fail(statusCode, details);
        }
    }
}