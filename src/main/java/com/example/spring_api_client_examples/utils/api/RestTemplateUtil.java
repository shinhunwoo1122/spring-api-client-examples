package com.example.spring_api_client_examples.utils.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException; // 공통 부모 예외 임포트

import java.util.Map;

@Slf4j
public class RestTemplateUtil {
    private static final RestTemplate REST_TEMPLATE;
    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        REST_TEMPLATE = new RestTemplate(factory);
    }

    // =========================================================================
    // 1. GET (데이터 조회)
    // =========================================================================
    public static <T> ApiResponse<T> get(String baseUrl, Map<String, Object> params, Class<T> responseType){
        String finalUrl = UrlBuilder.buildUrlWithParams(baseUrl, params);
        log.info("---- RestTemplate GET 호출 URL: {} -----", finalUrl);

        try {
            ResponseEntity<T> responseEntity = REST_TEMPLATE.getForEntity(finalUrl, responseType);
            return handleResponse(responseEntity);
        }catch (HttpClientErrorException e){
            return handleHttpError(e, responseType);
        }catch (HttpServerErrorException e){
            return handleHttpError(e, responseType);
        }catch (ResourceAccessException e) {
            return ApiResponse.fail(503, "Resource Access Error (Timeout/Connection Refused): " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Unexpected error: " + e.getMessage());
        }
    }



    // =========================================================================
    // 2. POST (데이터 생성)
    // =========================================================================
    public static <T> ApiResponse<T> post(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("---- RestTemplate POST 호출 URL: {} -----", fullUrl);

        try {
            ResponseEntity<T> responseEntity = REST_TEMPLATE.postForEntity(fullUrl, requestBody, responseType);
            return handleResponse(responseEntity);
        }catch (HttpClientErrorException | HttpServerErrorException e){
            return handleHttpError(e, responseType);
        }catch (ResourceAccessException e) {
            return ApiResponse.fail(503, "Resource Access Error (Timeout/Connection Refused): " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. PUT (데이터 전체 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> put(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("---- RestTemplate PUT 호출 URL: {} -----", fullUrl);

        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
            ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                    fullUrl, HttpMethod.PUT, requestEntity, responseType);

            return handleResponse(responseEntity);
        }catch (HttpClientErrorException | HttpServerErrorException e){
            return handleHttpError(e, responseType);
        }catch (ResourceAccessException e) {
            return ApiResponse.fail(503, "Resource Access Error (Timeout/Connection Refused): " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. PATCH (데이터 부분 업데이트)
    // =========================================================================
    public static <T> ApiResponse<T> patch(String fullUrl, Object requestBody, Class<T> responseType) {
        log.info("---- RestTemplate PATCH 호출 URL: {} -----", fullUrl);

        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
            ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                    fullUrl, HttpMethod.PATCH, requestEntity, responseType);

            return handleResponse(responseEntity);
        }catch (HttpClientErrorException | HttpServerErrorException e){
            return handleHttpError(e, responseType);
        }catch (ResourceAccessException e) {
            return ApiResponse.fail(503, "Resource Access Error (Timeout/Connection Refused): " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. DELETE (데이터 삭제)
    // =========================================================================
    public static ApiResponse<Void> delete(String fullUrl) {
        log.info("---- RestTemplate DELETE 호출 URL: {} -----", fullUrl);

        try {
            REST_TEMPLATE.delete(fullUrl);
            return ApiResponse.success(204, null); // 204 No Content
        }catch (HttpClientErrorException | HttpServerErrorException e){
            // T가 Void이므로, Void.class를 명시적으로 전달
            return handleHttpError(e, Void.class);
        }catch (ResourceAccessException e) {
            return ApiResponse.fail(503, "Resource Access Error (Timeout/Connection Refused): " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "Unexpected error: " + e.getMessage());
        }
    }

    // =========================================================================
    // Private 헬퍼 메서드: 응답 및 에러 처리
    // =========================================================================

    // ResponseEntity를 ApiResponse로 변환하는 공통 로직
    private static <T> ApiResponse<T> handleResponse(ResponseEntity<T> responseEntity) {
        int statusCode = responseEntity.getStatusCode().value();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (statusCode == 204 || responseEntity.getBody() == null) {
                return ApiResponse.success(statusCode, null);
            }
            return ApiResponse.success(statusCode, responseEntity.getBody());
        } else {
            String details = "Non-2xx status: " + statusCode;
            return ApiResponse.fail(statusCode, details);
        }
    }

    // HttpStatusCodeException (4xx 또는 5xx)을 ApiResponse.fail로 변환하는 공통 로직
    // [최종 수정]: T 타입을 명시적으로 받아 타입 추론 오류 해결
    private static <T> ApiResponse<T> handleHttpError(HttpStatusCodeException e, Class<T> responseType) {
        String body = e.getResponseBodyAsString();
        String details = String.format("HTTP Error %d. Body: %s", e.getStatusCode().value(), body);
        log.error("RestTemplate HTTP Error: {}", details);

        // T 타입이 무엇이든 (데이터는 없지만) T 타입의 실패 ApiResponse를 반환
        return ApiResponse.<T>fail(e.getStatusCode().value(), details);
    }
}