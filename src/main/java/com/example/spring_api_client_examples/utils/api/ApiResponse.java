package com.example.spring_api_client_examples.utils.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@ToString
public class ApiResponse<T> {

    //실제 HTTP 응답 상탵 코드 (예: 200, 404, 500)
    private final int httpStatusCode;

    //서비스 레벨의 성공/ 실패 여부
    private final String serviceCode;
    private final String message;
    private final T data;
    private final ErrorDetail error;

    // --- 생성자 ---
    // 1. 성공 응답 생성자
    protected ApiResponse(int httpStatusCode, T data) {
        this.httpStatusCode = httpStatusCode;
        this.serviceCode = "SUCCESS";
        this.message = "API call succeeded.";
        this.data = data;
        this.error = null;
    }

    // 2. 실패 응답 생성자
    protected ApiResponse(int httpStatusCode, ErrorDetail error) {
        this.httpStatusCode = httpStatusCode;
        this.serviceCode = "FAIL";
        this.message = "API call failed.";
        this.data = null;
        this.error = error;
    }

    // --- 정적 팩토리 메서드 ---
    public static <T> ApiResponse<T> success(int httpStatusCode, T data) {
        return new ApiResponse<>(httpStatusCode, data);
    }

    public static <T> ApiResponse<T> fail(int httpStatusCode, String details) {
        // 실패 시, 에러 코드(HTTP_404)와 상세 메시지를 포함
        String errorCode = "HTTP_" + httpStatusCode;
        ErrorDetail errorDetail = new ErrorDetail(errorCode, details);
        return new ApiResponse<>(httpStatusCode, errorDetail);
    }

    // --- 내부 ErrorDetail 클래스 ---
    @Getter
    @ToString
    public static class ErrorDetail {
        private final String code;
        private final String details;

        public ErrorDetail(String code, String details) {
            this.code = code;
            this.details = details;
        }
    }
}
