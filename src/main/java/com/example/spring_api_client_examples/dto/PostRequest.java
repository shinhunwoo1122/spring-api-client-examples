package com.example.spring_api_client_examples.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private Integer userId;
    private String title;
    private String body;
    // PUT/PATCH 시 ID를 Body에 포함시키지 않으므로, 이 DTO만 사용합니다.
}
