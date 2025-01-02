package com.table.hotpack.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ApiController {

    @Value("${API_KEY}")
    private String apiKey;
    @Value("${myapp.appkey}")
    private String appkey;

    private static final Map<Integer, String> areaCodeMap = new HashMap<>();
    static {
        areaCodeMap.put(1, "서울");
        areaCodeMap.put(2, "인천");
        areaCodeMap.put(3, "대전");
        areaCodeMap.put(4, "대구");
        areaCodeMap.put(5, "광주");
        areaCodeMap.put(6, "부산");
        areaCodeMap.put(7, "울산");
        areaCodeMap.put(8, "세종");
        areaCodeMap.put(31, "경기");
        areaCodeMap.put(32, "강원");
        areaCodeMap.put(33, "충북");
        areaCodeMap.put(34, "충남");
        areaCodeMap.put(35, "경북");
        areaCodeMap.put(36, "경남");
        areaCodeMap.put(37, "전북");
        areaCodeMap.put(38, "전남");
        areaCodeMap.put(39, "제주");
    }

    private final OkHttpClient client = new OkHttpClient();


    // 지역 목록을 가져오는 메서드
    @RequestMapping("/getAreaList")
    public String getAreaList(Model model) {

        // 인증된 사용자에게 지역 목록 전달
        List<Map<String, String>> areaList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : areaCodeMap.entrySet()) {
            areaList.add(Map.of("areaCode", String.valueOf(entry.getKey()), "areaName", entry.getValue()));
        }

        model.addAttribute("areaList", areaList);
        return "areaList"; // areaList.html 페이지로 이동
    }


    // 선택한 지역에 대한 관광지 목록을 가져오는 메서드
    @RequestMapping("/getTouristSpots")
    public String getTouristSpots(Model model, WebRequest webRequest) {
        // WebRequest에서 areaCode 가져오기
        String areaCode = webRequest.getParameter("areaCode");
        //날짜
        String startDate = webRequest.getParameter("startDate");
        String endDate = webRequest.getParameter("endDate");
        // 지역 코드에 해당하는 areaName을 찾기
        String areaName = areaCodeMap.get(Integer.parseInt(areaCode));
        //model.addAttribute("appkey", appkey);

        // 지역에 맞는 관광지 정보 API URL
        String url12 = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1?serviceKey=" + this.apiKey +
                "&MobileOS=ETC&MobileApp=test&_type=json&areaCode=" + areaCode + "&contentTypeId=12&numOfRows=50";
        String url39 = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1?serviceKey=" + this.apiKey +
                "&MobileApp=test&_type=json&areaCode=" + areaCode + "&contentTypeId=32&numOfRows=50";
        String url57 = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1?serviceKey=" + this.apiKey +
                "&MobileApp=test&_type=json&areaCode=" + areaCode + "&contentTypeId=39&numOfRows=50";

        // 각 API에 대해 관광지 정보 가져오기
        List<Map<String, String>> touristSpots = new ArrayList<>();
        try {
            touristSpots.addAll(getTouristDataFromApi(url12));
            touristSpots.addAll(getTouristDataFromApi(url39));
            touristSpots.addAll(getTouristDataFromApi(url57));
        } catch (IOException e) {
            model.addAttribute("error", "Error occurred while fetching tourist spots: " + e.getMessage());
            return "error";
        }

        model.addAttribute("areaName", areaName);
        model.addAttribute("touristSpots", touristSpots);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "touristSpots"; // touristSpots.html 페이지로 이동
    }

    // API에서 관광지 데이터를 가져오는 메서드
    private List<Map<String, String>> getTouristDataFromApi(String urlStr) throws IOException {
        List<Map<String, String>> touristList = new ArrayList<>();
        Request request = new Request.Builder().url(urlStr).get().build();

        try (Response response = this.client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonResponse = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode items = rootNode.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("title").asText();
                    String tel = item.path("tel").asText();
                    String addr1 = item.path("addr1").asText();
                    String firstimage = item.path("firstimage").asText();
                    String mapx = item.path("mapx").asText(); // mapx 값 추가
                    String mapy = item.path("mapy").asText();
                    String contentid = item.path("contentid").asText();
                    if (firstimage == null || firstimage.isEmpty()) {
                        firstimage = "/img/logo.png"; // 기본 이미지로 설정
                    }

                    touristList.add(Map.of(
                            "title", title,
                            "tel", tel,
                            "addr1", addr1,
                            "firstimage", firstimage,
                            "mapx", mapx, // mapx 추가
                                "mapy", mapy,
                            "contentid",contentid// mapy 추가
                    ));
                }
            }
        }

        return touristList;
    }
}
