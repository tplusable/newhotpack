package com.table.hotpack.post.repository;

import com.table.hotpack.post.dto.ContentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class ContentRepositoryImpl implements ContentRepository {

    @Value("${API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client = new OkHttpClient();

    public ContentRepositoryImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ContentDto findContentById(String contentId) {
        String apiUrl = "https://apis.data.go.kr/B551011/KorService1/detailCommon1?serviceKey=" + this.apiKey +
                "&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=" + contentId +
                "&contentTypeId=12&defaultYN=Y&firstImageYN=Y&areacodeYN=Y&catcodeYN=Y&addrinfoYN=Y&mapinfoYN=Y&overviewYN=Y&numOfRows=10";

        System.out.println("API 호출 URL: " + apiUrl);

        Request request = new Request.Builder().url(apiUrl).get().build();
        try (Response response = this.client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("API 호출 실패: " + response.code() + " - " + response.message());
                return null;
            }

            String jsonResponse = response.body().string();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

            // itemNode가 배열인지 확인하고 첫 번째 항목을 가져옴
            if (itemNode.isArray() && itemNode.size() > 0) {
                JsonNode item = itemNode.get(0);
                String title = item.path("title").asText();
                String tel = item.path("tel").asText();
                String addr1 = item.path("addr1").asText();
                String firstimage = item.path("firstimage").asText();
                String mapx = item.path("mapx").asText();
                String mapy = item.path("mapy").asText();
                String homepage = item.path("homepage").asText();
                String overview = item.path("overview").asText();

                if (firstimage == null || firstimage.isEmpty()) {
                    firstimage = "/img/logo.png";  // 기본 이미지 설정
                }

                String homepageLink = "";
                if (homepage != null && !homepage.isEmpty()) {
                    // <a> 태그에서 href 속성 값을 추출하는 정규식
                    String urlRegex = "href=\"(https?://[^\"]+)\"";
                    Pattern pattern = Pattern.compile(urlRegex);
                    Matcher matcher = pattern.matcher(homepage);

                    if (matcher.find()) {
                        homepageLink = matcher.group(1);  // URL만 추출
                    } else {
                        homepageLink = "정보 없음";  // URL을 찾지 못한 경우 "정보 없음"
                    }
                } else {
                    homepageLink = "정보 없음";  // homepage 값이 없으면 "정보 없음"
                }

                return new ContentDto(title, tel, addr1, firstimage, mapx, mapy, contentId, homepageLink, overview);
            } else {
                System.err.println("API 응답에서 item이 비어있거나 잘못된 형식입니다.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("API 호출 중 예외 발생: " + e.getMessage());
            return null; // API 호출 실패 시 null 반환
        }
    }
}
