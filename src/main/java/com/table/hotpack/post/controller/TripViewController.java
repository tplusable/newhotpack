package com.table.hotpack.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.service.ApiService;
import com.table.hotpack.post.service.TripInfoService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/trip/view")  // 웹 뷰 관련 경로
public class TripViewController {

    @Value("${API_KEY}")
    private String apiKey;

    private final TripInfoService tripInfoService;
    private final ApiService apiService;
    private final OkHttpClient client = new OkHttpClient();

    // 내 여행 정보 리스트 페이지
    @GetMapping("/myTrip")
    public String viewMyTrip() {
        return "mytour";
    }

    // 특정 여행 정보 페이지
    @GetMapping("/{id}")
    public String viewTripInfo(@PathVariable("id") Long id, Model model) {
        TripInfoDto tripInfoDto = tripInfoService.getTripInfoDtoById(id);

        // 만약 tripInfoDto가 null이면 404 오류를 반환하거나 다른 처리
        if (tripInfoDto == null) {
            // 예를 들어, null일 경우 404 오류 페이지로 리다이렉트
            return "error/404";
        }

        // areaName이 null일 경우 기본값 설정
        if (tripInfoDto.getAreaName() == null) {
            tripInfoDto.setAreaName("정보 없음");
        }

        List<Map<String, Object>> contentByDateList = tripInfoDto.getContentIdsByDate().entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey()); // 날짜 설정

                    List<String> contentIdList = entry.getValue(); // 날짜별 contentId 리스트

                    // 각 contentId에 대해 상세 정보를 가져옴
                    List<Map<String, Object>> contentDetails = contentIdList.stream()
                            .map(contentId -> {
                                // API 호출
                                Map<String, Object> contentInfo = getContentInfoFromApi(contentId);

                                // 콘솔에 contentInfo 출력 (디버깅용)
                                System.out.println("contentInfo for contentId " + contentId + ": " + contentInfo);

                                // API 호출 결과가 없을 경우 기본값 설정
                                if (contentInfo == null) {
                                    contentInfo = Map.of(
                                            "title", "정보 없음",
                                            "tel", "정보 없음",
                                            "addr1", "정보 없음",
                                            "firstimage", "/img/logo.png",
                                            "mapx", "정보 없음",
                                            "mapy", "정보 없음",
                                            "contentid", contentId,
                                            "homepage", "정보없음",
                                            "overview", "정보없음"
                                    );
                                }
                                return contentInfo; // 콘텐츠 상세 정보 반환
                            })
                            .collect(Collectors.toList());

                    map.put("contentDetails", contentDetails); // contentDetails에 상세 정보 추가
                    return map;
                })
                .collect(Collectors.toList());

        // 날짜별 정렬 (숫자 비교)
        contentByDateList.sort((entry1, entry2) -> {
            String date1 = (String) entry1.get("date");
            String date2 = (String) entry2.get("date");
            int day1 = Integer.parseInt(date1.replaceAll("\\D", "")); // 숫자만 추출
            int day2 = Integer.parseInt(date2.replaceAll("\\D", "")); // 숫자만 추출
            return Integer.compare(day1, day2); // 숫자 비교
        });

        // Model에 추가
        model.addAttribute("tripInfo", tripInfoDto);
        model.addAttribute("areaName", tripInfoDto.getAreaName() != null ? tripInfoDto.getAreaName() : "정보 없음");
        model.addAttribute("contentByDateList", contentByDateList);

        // "tripInfoDetails" 페이지로 이동
        return "tripInfoDetails";
    }

    private Map<String, Object> getContentInfoFromApi(String contentId) {
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
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

            // 수정된 부분: itemNode가 배열일 경우 처리
            if (itemNode.isArray() && itemNode.size() > 0) {
                JsonNode item = itemNode.get(0);  // 배열의 첫 번째 항목을 가져옴
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


                /*String homepageLink = "";
                if (homepage != null && !homepage.isEmpty()) {
                    // homepage가 이미 <a> 태그 형식일 경우 그대로 사용
                    if (homepage.contains("<a ") && homepage.contains("</a>")) {
                        homepageLink = homepage;  // 이미 <a> 태그 형식이면 그대로 사용
                    } else {
                        // URL 형식인지 체크 (http:// 또는 https://로 시작하고 뒤에 경로도 허용)
                        if (homepage.matches("^https?://[a-zA-Z0-9.-]+(?:/[^\s]*)?$")) {
                            homepageLink = "<a href=\"" + homepage + "\" target=\"_self\">" + homepage + "</a>";
                        }
                    }
                } else {
                    homepageLink = "정보없음";  // homepage 값이 없으면 "정보없음" 출력
                }*/

                String homepageLink = "";
                if (homepage != null && !homepage.isEmpty()) {
                    // <a> 태그 안에서 href 속성에 있는 URL만 추출
                    String urlRegex = "https?://[a-zA-Z0-9.-]+(?:/[^\"]*)?";
                    Pattern pattern = Pattern.compile(urlRegex);
                    Matcher matcher = pattern.matcher(homepage);

                    if (matcher.find()) {
                        homepageLink = "<a href=\"" + matcher.group() + "\" target=\"_blank\">홈페이지</a>";
                    } else {
                        homepageLink = "정보 없음";  // URL을 찾지 못한 경우 "정보 없음"
                    }
                } else {
                    homepageLink = "정보 없음";  // homepage 값이 없으면 "정보 없음"
                }




                // homepageLink가 제대로 생성되었는지 확인
                System.out.println("Processed homepage link: " + homepageLink);  // 가공된 homepage 값을 콘솔에 출력

                // Map으로 반환
                return Map.of(
                        "title", title,
                        "tel", tel,
                        "addr1", addr1,
                        "firstimage", firstimage,
                        "mapx", mapx,
                        "mapy", mapy,
                        "contentid", contentId,
                        "homepage",  homepageLink,
                        "overview", overview
                );
            } else {
                System.err.println("API 응답에서 item이 비어있거나 잘못된 형식입니다.");
            }
        } catch (IOException e) {
            System.err.println("API 호출 중 예외 발생: " + e.getMessage());
        }
        return null; // API 호출 실패 시 null 반환
    }

}