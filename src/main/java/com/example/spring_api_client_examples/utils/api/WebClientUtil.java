package com.example.spring_api_client_examples.utils.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse; // ClientResponse 임포트
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class WebClientUtil {

    // WebClient 인스턴스 생성 헬퍼 메서드 유지
    private static WebClient getWebClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(5))
                )).build();
    }

    // =========================================================================
    // Private: 응답 처리 (핵심 로직) - 변경 없음
    // =========================================================================
    /**
     * ClientResponse를 받아 성공/실패로 분기 처리하는 핵심 로직
     */
    private static <T> Mono<ApiResponse<T>> processClientResponse(ClientResponse clientResponse, Class<T> responseType) {

        int rawStatusCode = clientResponse.statusCode().value();
        HttpStatus status = HttpStatus.valueOf(rawStatusCode);

        if (status.is2xxSuccessful()) {
            // 2xx 성공 경로
            if (status == HttpStatus.NO_CONTENT || rawStatusCode == 204) {
                return Mono.just(ApiResponse.success(status.value(), null));
            }
            return clientResponse.bodyToMono(responseType)
                    .map(body -> ApiResponse.success(status.value(), body))
                    .onErrorResume(e -> Mono.just(ApiResponse.fail(500, "Internal Parsing Error: " + e.getMessage())));

        } else {
            // 4xx, 5xx 에러 경로
            return clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("No body available")
                    .map(body -> {
                        String details = String.format("API Error %d. Body: %s", rawStatusCode, body);
                        log.error(details);
                        return ApiResponse.<T>fail(rawStatusCode, details);
                    })
                    .onErrorResume(e -> Mono.just(ApiResponse.fail(rawStatusCode, "Failed to parse error response.")));
        }
    }

    // Private: 네트워크 에러 처리 (최종)
    private static <T> Mono<ApiResponse<T>> handleNetworkError(Throwable e) {
        log.error("WebClient Network Error: {}", e.getMessage());
        return Mono.just(ApiResponse.fail(503, "WebClient Network Failure or Timeout: " + e.getMessage()));
    }


    // =========================================================================
    // GET (데이터 조회) - 수정 완료
    // =========================================================================
    public static <T> Mono<ApiResponse<T>> get(String baseUrl, String path, Map<String, Object> params, Class<T> responseType){
        log.info("---- WebClient GET Util 호출 base: {} , Path: {} ----", baseUrl, path);
        WebClient webClient = getWebClient(baseUrl);

        // retrieve()를 사용하지 않고 exchangeToMono로 바로 연결
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if(params != null){
                        params.forEach((key, value) -> uriBuilder.queryParam(key, value));
                    }
                    return uriBuilder.build();
                })
                .exchangeToMono(clientResponse -> processClientResponse(clientResponse, responseType))
                .onErrorResume(WebClientUtil::handleNetworkError);
    }

    // =========================================================================
    // POST (데이터 생성) - 수정 완료
    // =========================================================================
    public static <T> Mono<ApiResponse<T>> post(String baseUrl, String path, Object requestBody, Class<T> responseType) {
        log.info("---- WebClient POST Util 호출 base: {} , Path: {} ----", baseUrl, path);
        WebClient webClient = getWebClient(baseUrl);

        // Body를 설정하고 exchangeToMono로 바로 연결
        return webClient.post()
                .uri(path)
                .bodyValue(requestBody)
                .exchangeToMono(clientResponse -> processClientResponse(clientResponse, responseType))
                .onErrorResume(WebClientUtil::handleNetworkError);
    }

    // =========================================================================
    // PUT (데이터 전체 업데이트) - 수정 완료
    // =========================================================================
    public static <T> Mono<ApiResponse<T>> put(String baseUrl, String path, Object requestBody, Class<T> responseType) {
        log.info("---- WebClient PUT Util 호출 base: {} , Path: {} ----", baseUrl, path);
        WebClient webClient = getWebClient(baseUrl);

        return webClient.put()
                .uri(path)
                .bodyValue(requestBody)
                .exchangeToMono(clientResponse -> processClientResponse(clientResponse, responseType))
                .onErrorResume(WebClientUtil::handleNetworkError);
    }

    // =========================================================================
    // PATCH (데이터 부분 업데이트) - 수정 완료
    // =========================================================================
    public static <T> Mono<ApiResponse<T>> patch(String baseUrl, String path, Object requestBody, Class<T> responseType) {
        log.info("---- WebClient PATCH Util 호출 base: {} , Path: {} ----", baseUrl, path);
        WebClient webClient = getWebClient(baseUrl);

        return webClient.patch()
                .uri(path)
                .bodyValue(requestBody)
                .exchangeToMono(clientResponse -> processClientResponse(clientResponse, responseType))
                .onErrorResume(WebClientUtil::handleNetworkError);
    }

    // =========================================================================
    // DELETE (데이터 삭제) - 수정 완료
    // =========================================================================
    public static Mono<ApiResponse<Void>> delete(String baseUrl, String path) {
        log.info("---- WebClient DELETE Util 호출 base: {} , Path: {} ----", baseUrl, path);
        WebClient webClient = getWebClient(baseUrl);

        return webClient.delete()
                .uri(path)
                .exchangeToMono(clientResponse -> processClientResponse(clientResponse, Void.class))
                .onErrorResume(WebClientUtil::handleNetworkError);
    }
}