package com.table.hotpack.post.service;

import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.domain.ContentId;
import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.repository.TripInfoRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class TripInfoService {

    private final TripInfoRepository tripInfoRepository;

    // 여행 정보 저장
    @Transactional
    public TripInfo saveTripInfoWithContentIds(TripInfoDto tripInfoDto) {
        TripInfo tripInfo = new TripInfo();
        tripInfo.setAreaName(tripInfoDto.getAreaName());
        tripInfo.setStartDate(tripInfoDto.getStartDate());
        tripInfo.setEndDate(tripInfoDto.getEndDate());

        // 날짜별 콘텐츠 ID 리스트를 가져옴
        Map<String, List<String>> contentIdsByDate = tripInfoDto.getContentIdsByDate();

        // ContentId 엔티티 목록 생성
        List<ContentId> contentIdEntities = new ArrayList<>();

        // contentIdsByDate를 순회하여 ContentId 객체 생성
        for (Map.Entry<String, List<String>> entry : contentIdsByDate.entrySet()) {
            String dayIndex = entry.getKey(); // 1일차, 2일차 등
            List<String> contentIds = entry.getValue(); // 해당 날짜의 콘텐츠 ID 목록

            // 각 콘텐츠 ID에 대해 ContentId 엔티티 생성
            for (String contentId : contentIds) {
                ContentId contentIdEntity = new ContentId();
                contentIdEntity.setContentId(contentId);
                contentIdEntity.setDayIndex(dayIndex);
                contentIdEntity.setTripInfo(tripInfo); // TripInfo와 ContentId 연결

                contentIdEntities.add(contentIdEntity); // 생성한 ContentId 객체를 리스트에 추가
            }
        }

        tripInfo.setContentIds(contentIdEntities);
        return tripInfoRepository.save(tripInfo);
    }

    // 모든 여행 정보 조회
    public List<TripInfo> getAllTripInfos() {
        return tripInfoRepository.findAll();
    }

    // 여행 정보 ID로 조회 (DTO 반환)
    public TripInfoDto getTripInfoDtoById(Long id) {
        TripInfo tripInfo = tripInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TripInfo with id " + id + " not found"));

        // TripInfo 엔티티를 TripInfoDto로 변환하여 반환
        return new TripInfoDto(tripInfo);
    }



}