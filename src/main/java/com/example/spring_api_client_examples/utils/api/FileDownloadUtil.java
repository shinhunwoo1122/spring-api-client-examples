package com.example.spring_api_client_examples.utils.api;


import com.example.spring_api_client_examples.dto.FileMetaData;
import com.example.spring_api_client_examples.utils.file.FileMetaDataExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * WebClient를 사용하여 외부 API에서 파일을 다운로드하고 로컬 디스크에 저장하는 유틸리티입니다.
 * I/O 작업(스트리밍 및 저장)에 집중하며, 메타데이터 추출은 FileMetadataExtractor에 위임합니다.
 */
@Slf4j
public class FileDownloadUtil {

    //파일 저장 기본 경로 설정: 운영체제의 임시 데렉토리 아래 'app_downloads' 폴더를 사용
    //지금은 sample이지만 디렉토리내용은 OS에 의해 정기적으로 정리, 파일을 영구 저장소 (FFP/S3)등 이동시킨 후 삭제 해아 함
    private static final Path STORAGE_ROOT_DIR = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "downloads"
    );

    // FileDownloadResult record는 별도의 파일로 분리되었습니다.

    // =========================================================================
    // 핵심 다운로드 및 저장 로직
    // =========================================================================

    /**
     * 파일을 다운로드하고 로컬 디스크에 저장합니다.
     * @param webClient WebClient 인스턴스 (Base URL이 이미 설정되어 있어야 합니다.)
     * @param path 다운로드할 파일 경로
     * @return 파일 메타데이터와 최종 저장 경로를 담은 Mono
     */
    public static Mono<FileMetaData> downloadFile(WebClient webClient, String path){
        // 1. 저장 디렉토리가 존재하는지 확인하고 없으면 생성하는 비동기 작업 정의
        Mono<Void> ensureDirMono = Mono.fromRunnable(() -> {
            try {
                if(!STORAGE_ROOT_DIR.toFile().exists()){ // 디렉토리가 비어있는지 확인
                    STORAGE_ROOT_DIR.toFile().mkdirs(); // 디렉토리 생성
                    log.info("저장 디렉토리 생성 완료 : {}", STORAGE_ROOT_DIR.toAbsolutePath());
                }
            }catch (Exception e){
                // 디렉토리 생성 실패 시, 전체 프로세스를 에러로 종료합니다.
                throw new RuntimeException("저장 디렉토리 생성 실패", e);
            }
        });
        // 2. 디렉토리 생성 완료 후, 다음 Mono체인을 실행
        return ensureDirMono.then(
            // WebClient 호출부터 응답 처리까지 모든 로직을 downloadAndHandleResponse 메서드에 위임합니다.
            downloadFileWithHandling(webClient, path)
        );
    }

    /**
     * WebClient 호출, 응답 처리 및 네트워크 오류를 처리하는 통합 파이프라인입니다.
     */
    private static Mono<FileMetaData> downloadFileWithHandling(WebClient webClient, String path){
       return webClient.get()
                .uri(path)
                .exchangeToMono(response -> {
                    if(!response.statusCode().is2xxSuccessful()){
                        log.error("파일 다운로드 실패. HTTP 상태 : {}", response.statusCode());
                        // response.createException(): HTTP 상태 코드에 맞는 WebClientException (예: HttpClientErrorException)을 생성합니다.
                        return response.createException().flatMap(Mono::error);
                    }
                    //2. 스트리밍 및 저장 로직을 다움 메서드로 위임함
                    return processStreamingAndSave(response, path);
                })
                .onErrorResume(e -> {
                    log.error("네트워크 오류 또는 예상치 못한 예외 발생: {}", e.getMessage());
                    // 새로운 RuntimeException을 Mono에 담아 상위 호출자에게 실패를 알립니다.
                    return Mono.error(new RuntimeException("파일 다운로드 실패: " + e.getMessage()));
                });
    }

    private static Mono<FileMetaData> processStreamingAndSave(ClientResponse response, String path) {

        //1. 메타 데이터 추출
        HttpHeaders headers = response.headers().asHttpHeaders(); // 응답 헤더 객체를 Spring HttpHeaders로 변환
        FileMetaData metaData = FileMetaDataExtractor.extract(headers, path);

        //2.최종 저장 경로 결정 Path.resolve()로 경로 결합
        Path finalPath = STORAGE_ROOT_DIR.resolve(metaData.getSavedFileName());

        //3.응답 본문을 DataBuffer 스트림으로 가져옴
        Flux<DataBuffer> dataBufferFlux = response.bodyToFlux(DataBuffer.class);

        // DataBufferUtils.write(...)로 시작하는 I/O 체이닝을 .
        // 4. 추출된 Mono<FileMetaData>를 리턴합니다.
        return DataBufferUtils.write(
                        dataBufferFlux, //다운로드된 데이터 스트림(파일)
                        finalPath, //데이터 저장 경로
                        StandardOpenOption.CREATE, //파일 없을시 생성
                        StandardOpenOption.TRUNCATE_EXISTING //파일이 있으면 내용 덮어씀
                )// .then(): 이전 I/O 작업(Mono<Void>) 반환
                // .then(): 이전 I/O 작업(Mono<Void>) 완료 후 다음 Mono(Mono<FileMetaData>)를 실행합니다.
                .then(buildFileDownloadResultMono(finalPath, metaData))
                .onErrorResume(e -> {
                    log.error("스트리밍 저장 중 오류 발생: {}", e.getMessage());
                    return Mono.error(e);
                });
    }

    /**
     * 파일 쓰기(I/O)가 완료된 후, 파일 크기를 측정하고 최종 FileMetaData DTO를 완성하는 Mono를 만듭니다.
     */
    private static Mono<FileMetaData> buildFileDownloadResultMono(Path finalPath, FileMetaData metaData){
        // Mono.defer: I/O가 완료될 때까지 Files.size() 호출을 지연시킵니다.
        return Mono.defer(() -> {
            try {
                //1. 파일크기 측정 경로에있는 파일을 통해 측정함.
                long fileSize = Files.size(finalPath);
                log.info("파일 다운로드 및 저장 완료 크기: {} bytes", fileSize);

                //2. DTO에 최종 정보(savePath와 fileSize)를 Setter로 채워 넣음
                metaData.setSavedPath(finalPath);
                metaData.setFileSize(fileSize);

                //3. 완성된 DTO를 Mono에 담아 반환
                return Mono.just(metaData);
            } catch (IOException e){
                //파일 크기 측정 실패는 중요 오류 체인을 종료
                log.error("파일 크기 측정 실패 : {}", finalPath, e);
                return Mono.error(new RuntimeException("파일 크기 측정 실패"));
            }
        });
    }

}
