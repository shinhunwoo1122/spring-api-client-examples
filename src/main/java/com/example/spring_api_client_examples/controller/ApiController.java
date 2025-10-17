package com.example.spring_api_client_examples.controller;

import com.example.spring_api_client_examples.dto.FileMetaData;
import com.example.spring_api_client_examples.dto.Post;
import com.example.spring_api_client_examples.dto.PostRequest;
import com.example.spring_api_client_examples.service.ApiService;
import com.example.spring_api_client_examples.utils.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    // --- 헬퍼 메서드 ---

    /** 동기 호출 결과를 ResponseEntity로 변환 */
    private <T> ResponseEntity<ApiResponse<T>> buildSyncResponse(ApiResponse<T> response) {
        return ResponseEntity.status(response.getHttpStatusCode()).body(response);
    }

    /** 비동기 호출(Mono) 결과를 Mono<ResponseEntity>로 변환 */
    private <T> Mono<ResponseEntity<ApiResponse<T>>> mapAsyncResponse(Mono<ApiResponse<T>> responseMono) {
        return responseMono.map(response -> ResponseEntity.status(response.getHttpStatusCode()).body(response))
                .onErrorResume(e -> {
                    // WebClient에서 발생할 수 있는 네트워크/타임아웃 에러 처리
                    ApiResponse<T> errorResponse = ApiResponse.fail(HttpStatus.SERVICE_UNAVAILABLE.value(), "Network Failure: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
                });
    }

    // =========================================================================
    // 1. GET 엔드포인트 (READ)
    // =========================================================================
    // GET /api/v1/test/get/httpurlconnection
    @GetMapping("/get/httpurlconnection")
    public ResponseEntity<ApiResponse<Post[]>> getHttpURLConnection() {
        return buildSyncResponse(apiService.callHttpUrlConnection());
    }

    // GET /api/v1/test/get/httpclient
    @GetMapping("/get/httpclient")
    public ResponseEntity<ApiResponse<Post[]>> getHttpClient() {
        return buildSyncResponse(apiService.callHttpClient());
    }

    // GET /api/v1/test/get/resttemplate
    @GetMapping("/get/resttemplate")
    public ResponseEntity<ApiResponse<Post[]>> getRestTemplate() {
        return buildSyncResponse(apiService.callRestTemplate());
    }

    // GET /api/v1/test/get/webclient
    @GetMapping("/get/webclient")
    public Mono<ResponseEntity<ApiResponse<Post[]>>> getWebClient() {
        return mapAsyncResponse(apiService.callWebClient());
    }


    // =========================================================================
    // 2. POST 엔드포인트 (CREATE)
    // =========================================================================
    @PostMapping("/post/httpurlconnection")
    public ResponseEntity<ApiResponse<Post>> postHttpURLConnection(@RequestBody PostRequest req) {
        log.info("req = {} ", req);
        return buildSyncResponse(apiService.createPostHttpUrlConnection(req));
    }

    @PostMapping("/post/httpclient")
    public ResponseEntity<ApiResponse<Post>> postHttpClient(@RequestBody PostRequest req) {
        return buildSyncResponse(apiService.createPostHttpClient(req));
    }

    @PostMapping("/post/resttemplate")
    public ResponseEntity<ApiResponse<Post>> postRestTemplate(@RequestBody PostRequest req) {
        return buildSyncResponse(apiService.createPostRestTemplate(req));
    }

    @PostMapping("/post/webclient")
    public Mono<ResponseEntity<ApiResponse<Post>>> postWebClient(@RequestBody PostRequest req) {
        return mapAsyncResponse(apiService.createPostWebClient(req));
    }

    // =========================================================================
    // 3. PUT 엔드포인트 (FULL UPDATE)
    // =========================================================================
    // {id}는 JSONPlaceholder 테스트를 위해 경로에 포함
    @PutMapping("/put/httpurlconnection/{id}")
    public ResponseEntity<ApiResponse<Post>> putHttpURLConnection(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPutHttpUrlConnection(req));
    }

    @PutMapping("/put/httpclient/{id}")
    public ResponseEntity<ApiResponse<Post>> putHttpClient(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPutHttpClient(req));
    }

    @PutMapping("/put/resttemplate/{id}")
    public ResponseEntity<ApiResponse<Post>> putRestTemplate(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPutRestTemplate(req));
    }

    @PutMapping("/put/webclient/{id}")
    public Mono<ResponseEntity<ApiResponse<Post>>> putWebClient(@PathVariable String id, @RequestBody PostRequest req) {
        return mapAsyncResponse(apiService.updatePostPutWebClient(req));
    }

    // =========================================================================
    // 4. PATCH 엔드포인트 (PARTIAL UPDATE)
    // =========================================================================
    @PatchMapping("/patch/httpurlconnection/{id}")
    public ResponseEntity<ApiResponse<Post>> patchHttpURLConnection(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPatchHttpUrlConnection(req));
    }

    @PatchMapping("/patch/httpclient/{id}")
    public ResponseEntity<ApiResponse<Post>> patchHttpClient(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPatchHttpClient(req));
    }

    @PatchMapping("/patch/resttemplate/{id}")
    public ResponseEntity<ApiResponse<Post>> patchRestTemplate(@PathVariable String id, @RequestBody PostRequest req) {
        return buildSyncResponse(apiService.updatePostPatchRestTemplate(req));
    }

    @PatchMapping("/patch/webclient/{id}")
    public Mono<ResponseEntity<ApiResponse<Post>>> patchWebClient(@PathVariable String id, @RequestBody PostRequest req) {
        return mapAsyncResponse(apiService.updatePostPatchWebClient(req));
    }

    // =========================================================================
    // 5. DELETE 엔드포인트 (DELETE)
    // =========================================================================
    @DeleteMapping("/delete/httpurlconnection/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHttpURLConnection(@PathVariable String id) {
        return buildSyncResponse(apiService.deletePostHttpUrlConnection());
    }

    @DeleteMapping("/delete/httpclient/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHttpClient(@PathVariable String id) {
        return buildSyncResponse(apiService.deletePostHttpClient());
    }

    @DeleteMapping("/delete/resttemplate/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestTemplate(@PathVariable String id) {
        return buildSyncResponse(apiService.deletePostRestTemplate());
    }

    @DeleteMapping("/delete/webclient/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteWebClient(@PathVariable String id) {
        return mapAsyncResponse(apiService.deletePostWebClient());
    }

// =========================================================================
    // 6. 파일 다운로드 엔드포인트 (DOWNLOAD) - 확장
    // =========================================================================

    /**
     * GET /api/v1/download/webclient/jpg
     */
    @GetMapping("/download/webclient/jpg")
    public Mono<ResponseEntity<FileMetaData>> downloadJpgFileWebClient() {
        return apiService.downloadJpgFile()
                .map(metaData -> ResponseEntity.ok(metaData))
                .onErrorResume(e -> {
                    log.error("파일 다운로드 처리 중 치명적인 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(null));
                });
    }

    /**
     * GET /api/v1/download/webclient/png
     */
    @GetMapping("/download/webclient/png")
    public Mono<ResponseEntity<FileMetaData>> downloadPngFileWebClient() {
        return apiService.downloadPngFile()
                .map(metaData -> ResponseEntity.ok(metaData))
                .onErrorResume(e -> {
                    log.error("PNG 파일 다운로드 처리 중 치명적인 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(null));
                });
    }

    /**
     * GET /api/v1/download/webclient/pdf
     */
    @GetMapping("/download/webclient/pdf")
    public Mono<ResponseEntity<FileMetaData>> downloadPdfFileWebClient() {
        return apiService.downloadPdfFile()
                .map(metaData -> ResponseEntity.ok(metaData))
                .onErrorResume(e -> {
                    log.error("PDF 파일 다운로드 처리 중 치명적인 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(null));
                });
    }

    /**
     * GET /api/v1/download/webclient/mp4
     */
    @GetMapping("/download/webclient/mp4")
    public Mono<ResponseEntity<FileMetaData>> downloadMp4FileWebClient() {
        return apiService.downloadMp4File()
                .map(metaData -> ResponseEntity.ok(metaData))
                .onErrorResume(e -> {
                    log.error("MP4 파일 다운로드 처리 중 치명적인 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(null));
                });
    }
}
