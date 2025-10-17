package com.example.spring_api_client_examples.dto;

import lombok.*;

import java.nio.file.Path;

@Getter
@Setter
@ToString
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 추가
@AllArgsConstructor
@Builder
public class FileMetaData {
    private String originalFileName;
    private String extension;
    private String savedFileName;
    private Path savedPath;
    private String contentType;
    private Long fileSize;
}
