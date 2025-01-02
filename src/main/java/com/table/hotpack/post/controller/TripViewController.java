package com.table.hotpack.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.table.hotpack.post.domain.ContentId;
import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.dto.ApiResponse;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.service.ApiService;
import com.table.hotpack.post.service.TripInfoService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    // 모든 여행 정보 리스트 페이지
//    @GetMapping("/all")
//    public String viewAllTripInfos(Model model) {
//        List<TripInfoDto> tripInfos = tripInfoService.getAllTripInfos().stream()
//                .map(TripInfoDto::new)
//                .toList();
//        // 수정 가능한 리스트로 복사하여 정렬
//        List<TripInfoDto> sortableList = new ArrayList<>(tripInfos);
//        sortableList.sort((t1, t2) -> Long.compare(t2.getId(), t1.getId())); // ID 내림차순 정렬
//        model.addAttribute("tripInfos", sortableList);
//        return "tripInfoList"; // 여행 정보 목록 뷰 페이지
//    }

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

                // Map으로 반환
                return Map.of(
                        "title", title,
                        "tel", tel,
                        "addr1", addr1,
                        "firstimage", firstimage,
                        "mapx", mapx,
                        "mapy", mapy,
                        "contentid", contentId,
                        "homepage", homepage,
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