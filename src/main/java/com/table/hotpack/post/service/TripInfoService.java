package com.table.hotpack.post.service;

import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.domain.ContentId;
import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.repository.TripInfoRepository;
import lombok.RequiredArgsConstructor;
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

        List<ContentId> contentIdEntities = new ArrayList<>();
        Map<String, List<String>> contentIdsByDate = tripInfoDto.getContentIdsByDate();

        for (Map.Entry<String, List<String>> entry : contentIdsByDate.entrySet()) {
            String dayIndex = entry.getKey();
            List<String> contentIds = entry.getValue();

            for (String contentId : contentIds) {
                ContentId contentIdEntity = new ContentId();
                contentIdEntity.setContentId(contentId);
                contentIdEntity.setDayIndex(dayIndex);
                contentIdEntity.setTripInfo(tripInfo);

                contentIdEntities.add(contentIdEntity);
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