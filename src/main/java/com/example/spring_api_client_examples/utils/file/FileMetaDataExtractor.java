package com.example.spring_api_client_examples.utils.file;

import com.example.spring_api_client_examples.dto.FileMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;


/**
 * HTTP 응답 헤더 및 URL 경로를 분석하여 파일 메타데이터를 추출하는 유틸리티입니다.
 * 추출된 정보를 최종 DTO인 FileMetaData에 담아 반환하며, 다운로드 후 정보(경로, 크기)는 비워둡니다.
 */
@Slf4j
public class FileMetaDataExtractor {

    /**
     * HTTP 헤더와 URL 경로를 분석하여 파일 메타데이터를 추출합니다.
     * @param headers HTTP 응답 헤더
     * @param path 다운로드 URL 경로
     * @return 추출된 정보(파일명, 확장자 등)를 담은 FileMetaData DTO 객체
     */
    public static FileMetaData extract(HttpHeaders headers, String path){

        // 1. Content-Type 헤더에서 파일의 MIME 타입 추출
        // Spring의 HttpHeaders가 제공하는 메서드로, Content-Type 헤더 값을 MediaType 객체로 안전하게 변환합니다.
        MediaType contentType = headers.getContentType();

        // 2. 파일명 추출 (Content-Disposition 헤더가 최우선 순위)
        // Content-Disposition 헤더는 서버가 파일명을 클라이언트에게 알릴 때 사용하는 표준 헤더입니다. inline화면 출력, attachment download
        String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        String originalFileName = null;

        if(contentDisposition != null) {
            try {
                // 2. Spring의 ContentDisposition.parse() 메서드로 객체화합니다.
                ContentDisposition cd = ContentDisposition.parse(contentDisposition);

                // 3. .getFilename() 메서드로 최종 파일명을 가져옵니다.
                originalFileName = cd.getFilename();

            } catch (IllegalArgumentException e) {
                // Content-Disposition 헤더 파싱 오류 시 (헤더 형식이 잘못된 경우)
                log.warn("Contnet-Dispostion 헤더 파싱 오류: {}", e.getMessage());
                // 파싱 실패 시 파일명은 null로 설정
                originalFileName = null;
            }

        }

        //3. 대체 로직: 파일명이 추출되지 않거나 비어있는 경우, URL경로에서 파일명 추출함
        if(originalFileName == null || originalFileName.isEmpty()){
            try {
                int lastSlashIndex = path.lastIndexOf('/'); // 경로에서 마지막 '/'의 위치를 찾습니다.
                // URL의 마지막 슬래시(/) 이후 부분을 파일명으로 간주합니다. 경로체크해서 path경로와 비교해서 파일명있을시 저장
                originalFileName = (lastSlashIndex != -1 && lastSlashIndex < path.length() - 1)
                        ? path.substring(lastSlashIndex + 1)
                        : "downloaded_file"; // URL 분석 실패 시 최후의 기본값 설정
            } catch (Exception e){
                originalFileName = "downloaded_file";
            }
        }

        //4. 확장자 추출 및 추정
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf("."); //파일명에서 마지막 "." 위치 찾기

        if(lastDotIndex > 0 && lastDotIndex < originalFileName.length() -1){
            //'.'이 유효한 위치라면 확장자 추출
            extension = originalFileName.substring(lastDotIndex + 1);
        } else if (contentType != null) {
            // 파일명에서 확장자를 찾지 못했지만 Content-Type 정보가 있다면 확장자를 추정합니다.
            String subtype = contentType.getSubtype(); // MIME 타입의 서브타입 (예: image/jpeg에서 jpeg)을 가져옵니다.
            if(subtype != null && !subtype.isEmpty() && !subtype.contains("/") && subtype.length() < 10){
                extension = subtype;
            }
        }
        // 5. 중복 방지를 위해 UUID와 확장자를 결합하여 저장 파일명 (랜덤 파일명)을 생성합니다.
        // 확장자에 점이 없으므로, 파일명을 만들 때 점을 수동으로 추가합니다.
        String extensionWithDot = extension.isEmpty() ? "" : "." + extension;
        String randomFileName = UUID.randomUUID().toString().replace("-","") + extensionWithDot;

        return FileMetaData.builder().originalFileName(originalFileName).extension(extension).savedFileName(randomFileName).build();
    }
}
