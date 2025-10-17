package com.example.spring_api_client_examples.service;

import com.example.spring_api_client_examples.dto.FileMetaData;
import com.example.spring_api_client_examples.dto.Post;
import com.example.spring_api_client_examples.dto.PostRequest;
import com.example.spring_api_client_examples.utils.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class ApiService {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String RESOURCE_PATH_ALL = "/posts";
    private static final String RESOURCE_PATH_SINGLE = "/posts/1";
    private static final Map<String, Object> COMMON_GET_PARAMS = Map.of("userId", 1);

    // 파일 다운로드를 위한 외부 API (테스트 서버 주소 변경)
    // JPG/PNG/PDF는 W3C에서, MP4는 Big Buck Bunny 테스트 파일 서버에서 가져옵니다.
    private static final String W3C_BASE_URL = "https://placehold.co";
    private static final String MP4_TEST_BASE_URL = "http://commondatastorage.googleapis.com";
    private static final String MOZILLA_BASE_URL = "https://mozilla.github.io"; // 새로운 PDF Base URL 추가
    private static final String STABLE_IMG_BASE_URL = "https://pngimg.com";


    // W3C 테스트 샘플
    private static final String JPG_PATH = "/600x400/000000/FFFFFF/jpg";
    private static final String PNG_PATH = "/uploads/butterfly/butterfly_PNG1000.png";
    // PDF: Mozilla의 공개 테스트용 PDF 문서 경로로 변경
    private static final String PDF_PATH = "/pdf.js/web/compressed.tracemonkey-pldi-09.pdf";

    // 영상 샘플: Google Cloud Storage에 호스팅된 표준 Big Buck Bunny MP4 파일 경로로 변경
    private static final String MP4_PATH = "/gtv-videos-bucket/sample/BigBuckBunny.mp4";


    // =========================================================================
    // 1. GET 메서드 (Read) - 모든 동기 클라이언트 3인수로 복구
    // =========================================================================

    public ApiResponse<Post[]> callHttpUrlConnection() {
        String fullUrl = UrlBuilder.buildUrlWithParams(BASE_URL + RESOURCE_PATH_ALL, COMMON_GET_PARAMS);
        return HttpUrlConnectionUtil.get(fullUrl, Post[].class);
    }
    public ApiResponse<Post[]> callHttpClient() {
        String fullUrl = UrlBuilder.buildUrlWithParams(BASE_URL + RESOURCE_PATH_ALL, COMMON_GET_PARAMS);
        // [수정]: 3개 인수로 복구 (Map 전달)
        return HttpClientUtil.get(fullUrl, Collections.emptyMap(), Post[].class);
    }
    public ApiResponse<Post[]> callRestTemplate() {
        String fullUrl = UrlBuilder.buildUrlWithParams(BASE_URL + RESOURCE_PATH_ALL, COMMON_GET_PARAMS);
        // [수정]: 3개 인수로 복구 (Map 전달)
        return RestTemplateUtil.get(fullUrl, Collections.emptyMap(), Post[].class);
    }
    public Mono<ApiResponse<Post[]>> callWebClient() {
        // WebClient는 Base URL, Path, Map, Class (4개 인수로 유지)
        return WebClientUtil.get(BASE_URL, RESOURCE_PATH_ALL, COMMON_GET_PARAMS, Post[].class);
    }

    // =========================================================================
    // 2. POST 메서드 (Create) - (유지)
    // =========================================================================

    public ApiResponse<Post> createPostHttpUrlConnection(PostRequest req) {
        return HttpUrlConnectionUtil.post(BASE_URL + RESOURCE_PATH_ALL, req, Post.class);
    }
    public ApiResponse<Post> createPostHttpClient(PostRequest req) {
        return HttpClientUtil.post(BASE_URL + RESOURCE_PATH_ALL, req, Post.class);
    }
    public ApiResponse<Post> createPostRestTemplate(PostRequest req) {
        return RestTemplateUtil.post(BASE_URL + RESOURCE_PATH_ALL, req, Post.class);
    }
    public Mono<ApiResponse<Post>> createPostWebClient(PostRequest req) {
        return WebClientUtil.post(BASE_URL, RESOURCE_PATH_ALL, req, Post.class);
    }

    // =========================================================================
    // 3. PUT 메서드 (Full Update) - (유지)
    // =========================================================================

    public ApiResponse<Post> updatePostPutHttpUrlConnection(PostRequest req) {
        return HttpUrlConnectionUtil.put(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public ApiResponse<Post> updatePostPutHttpClient(PostRequest req) {
        return HttpClientUtil.put(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public ApiResponse<Post> updatePostPutRestTemplate(PostRequest req) {
        return RestTemplateUtil.put(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public Mono<ApiResponse<Post>> updatePostPutWebClient(PostRequest req) {
        return WebClientUtil.put(BASE_URL, RESOURCE_PATH_SINGLE, req, Post.class);
    }

    // =========================================================================
    // 4. PATCH 메서드 (Partial Update) - (유지)
    // =========================================================================

    public ApiResponse<Post> updatePostPatchHttpUrlConnection(PostRequest req) {
        return HttpUrlConnectionUtil.patch(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public ApiResponse<Post> updatePostPatchHttpClient(PostRequest req) {
        return HttpClientUtil.patch(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public ApiResponse<Post> updatePostPatchRestTemplate(PostRequest req) {
        return RestTemplateUtil.patch(BASE_URL + RESOURCE_PATH_SINGLE, req, Post.class);
    }
    public Mono<ApiResponse<Post>> updatePostPatchWebClient(PostRequest req) {
        return WebClientUtil.patch(BASE_URL, RESOURCE_PATH_SINGLE, req, Post.class);
    }

    // =========================================================================
    // 5. DELETE 메서드 (Delete) - (유지)
    // =========================================================================

    public ApiResponse<Void> deletePostHttpUrlConnection() {
        return HttpUrlConnectionUtil.delete(BASE_URL + RESOURCE_PATH_SINGLE);
    }
    public ApiResponse<Void> deletePostHttpClient() {
        return HttpClientUtil.delete(BASE_URL + RESOURCE_PATH_SINGLE);
    }
    public ApiResponse<Void> deletePostRestTemplate() {
        return RestTemplateUtil.delete(BASE_URL + RESOURCE_PATH_SINGLE);
    }
    public Mono<ApiResponse<Void>> deletePostWebClient() {
        return WebClientUtil.delete(BASE_URL, RESOURCE_PATH_SINGLE);
    }
    // =========================================================================
    // 6. 파일 다운로드 메서드 (Download File) - Base URL 변경 적용
    // =========================================================================

    public Mono<FileMetaData> downloadJpgFile() {
        // W3C Base URL 사용
        return WebClientUtil.downloadFile(W3C_BASE_URL, JPG_PATH);
    }

    public Mono<FileMetaData> downloadPngFile() {
        // W3C Base URL 사용
        return WebClientUtil.downloadFile(STABLE_IMG_BASE_URL, PNG_PATH);
    }

    public Mono<FileMetaData> downloadPdfFile() {
        // W3C Base URL 사용
        return WebClientUtil.downloadFile(MOZILLA_BASE_URL, PDF_PATH);
    }

    public Mono<FileMetaData> downloadMp4File() {
        // MP4 파일 전용 Base URL 사용
        return WebClientUtil.downloadFile(MP4_TEST_BASE_URL, MP4_PATH);
    }
}