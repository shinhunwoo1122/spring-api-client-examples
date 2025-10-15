package com.example.spring_api_client_examples.utils.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlBuilder {

    /**
     * 기본 URL과 파라미터를 조합하여 쿼리 문자열이 붙은 최종 URL을 생성합니다.
     * 파라미터 값은 UTF-8로 인코딩됩니다.
     * @param baseUrl 기본 API URL (예: https://jsonplaceholder.typicode.com/posts)
     * @param params 쿼리 파라미터 Map
     * @return 파라미터가 포함된 최종 URL
     */
    public static String buildUrlWithParams(String baseUrl, Map<String, Object> params){
        if(params == null || params.isEmpty()){
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        try {
            for(Map.Entry<String, Object> entry : params.entrySet()){
                // 1. Object를 String으로 안전하게 변환 (null도 "null" 문자열로 변환됨)
                String valueAsString = String.valueOf(entry.getValue());
                // 2. 값(value)을 UTF-8로 인코딩
                String encodedValue = URLEncoder.encode(valueAsString, "UTF-8");

                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(encodedValue)
                        .append("&");
            }
        }catch (UnsupportedEncodingException e){
            //UTF-8 인코딩 실패 했을시 발생
            throw new RuntimeException("URL encoding error: " + e.getMessage());
        }
        
        //마지막 '&'제거 해줌
        urlBuilder.setLength(urlBuilder.length() - 1);

        return urlBuilder.toString();
    }

}
