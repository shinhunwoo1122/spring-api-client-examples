package com.example.spring_api_client_examples.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@ToString
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 추가
@AllArgsConstructor
public class Post {

    // 이 게시글을 작성한 사용자의 ID
    private Integer userId;

    // 게시글의 고유 ID
    private Integer id;

    // 게시글의 제목
    private String title;

    // 게시글의 본문
    private String body;


}