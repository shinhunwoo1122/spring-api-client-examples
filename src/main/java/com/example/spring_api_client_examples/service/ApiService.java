package com.example.spring_api_client_examples.service;

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
}