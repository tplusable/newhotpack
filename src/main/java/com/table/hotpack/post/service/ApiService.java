package com.table.hotpack.post.service;

import com.table.hotpack.post.dto.OpenApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OpenApiResponse getContentInfo(String contentId) {
        String url = "https://apis.data.go.kr/B551011/KorService1/detailCommon1?serviceKey=3wUCY3%2BzuSmuvdaCcmZE2BJfyFndc9zOBvzh3YTFKUM0ZNQwLqW1HTRKNlFvn9Zuc3Y3c0eoFOQ6%2BB6wjNwWEg%3D%3D&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId="
                + contentId + "&contentTypeId=12&defaultYN=Y&firstImageYN=Y&areacodeYN=Y&catcodeYN=Y&addrinfoYN=Y&mapinfoYN=Y&overviewYN=Y&numOfRows=10";

        return restTemplate.getForObject(url, OpenApiResponse.class);
    }
}