package com.table.hotpack.post.dto;

import com.table.hotpack.post.domain.TripInfo;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripInfoDto {

    private Long id;  // 여행 정보 ID
    private String areaName;  // 여행 지역
    private LocalDate startDate;  // 시작 날짜
    private LocalDate endDate;  // 종료 날짜
    private Map<String, List<String>> contentIdsByDate;  // 날짜별 콘텐츠 ID 리스트

    // TripInfo 엔티티를 DTO로 변환하는 생성자
    public TripInfoDto(TripInfo tripInfo) {
        this.id = tripInfo.getId();
        this.areaName = tripInfo.getAreaName();
        this.startDate = tripInfo.getStartDate();
        this.endDate = tripInfo.getEndDate();

       /* // contentIds를 날짜별로 그룹화하여 Map 형태로 변환
        this.contentIdsByDate = tripInfo.getContentIds().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        contentId -> contentId.getDayIndex(),
                        java.util.stream.Collectors.mapping(contentId -> contentId.getContentId(), java.util.stream.Collectors.toList())
                ));*/

        // contentIds를 날짜별로 그룹화하고 중복을 제거한 dayIndex 생성
        this.contentIdsByDate = tripInfo.getContentIds().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        contentId -> contentId.getDayIndex(), // dayIndex를 키로 사용
                        java.util.stream.Collectors.mapping(
                                contentId -> contentId.getContentId(),
                                java.util.stream.Collectors.toList()
                        )
                ));


    }


    // getter 메소드
    public Map<String, List<String>> getContentIdsByDate() {
        return contentIdsByDate;
    }

}