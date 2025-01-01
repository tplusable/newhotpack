package com.table.hotpack.post.controller;

import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.service.TripInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/trip/view")  // 웹 뷰 관련 경로
public class TripViewController {

    private final TripInfoService tripInfoService;

    // 모든 여행 정보 리스트 페이지
    @GetMapping("/all")
    public String viewAllTripInfos(Model model) {
        List<TripInfoDto> tripInfos = tripInfoService.getAllTripInfos().stream()
                .map(TripInfoDto::new)
                .toList();
        model.addAttribute("tripInfos", tripInfos);
        return "tripInfoList"; // 여행 정보 목록 뷰 페이지
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
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("date", entry.getKey());
                    map.put("contentIds", entry.getValue());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());

        contentByDateList.sort((entry1, entry2) -> {
            String date1 = (String) entry1.get("date");
            String date2 = (String) entry2.get("date");
            int day1 = Integer.parseInt(date1.replaceAll("\\D", "")); // 숫자만 추출
            int day2 = Integer.parseInt(date2.replaceAll("\\D", "")); // 숫자만 추출
            return Integer.compare(day1, day2); // 숫자 비교
        });
        // 예시 contentId


        // Model에 추가
        model.addAttribute("tripInfo", tripInfoDto);
        model.addAttribute("areaName", tripInfoDto.getAreaName() != null ? tripInfoDto.getAreaName() : "정보 없음");
        model.addAttribute("contentByDateList", contentByDateList);


        // "tripInfoDetails" 페이지로 이동
        return "tripInfoDetails";
    }

}