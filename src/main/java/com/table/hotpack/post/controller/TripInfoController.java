package com.table.hotpack.post.controller;

import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.service.TripInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trip")
public class TripInfoController {

    private final TripInfoService tripInfoService;

    // 여행 정보 저장
    @PostMapping("/save")
    public ResponseEntity<TripInfoDto> saveTripInfo(@RequestBody TripInfoDto tripInfoDto, Principal principal) {
        String userEmail = principal.getName();

        TripInfo tripInfo = tripInfoService.saveTripInfoWithContentIds(tripInfoDto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TripInfoDto(tripInfo));
    }

    // 여행 정보 조회 (ID 기준)
    @GetMapping("/{id}")
    public ResponseEntity<TripInfoDto> getTripInfo(@PathVariable("id") Long id) {
        TripInfoDto tripInfoDto = tripInfoService.getTripInfoDtoById(id);

        if (tripInfoDto == null) {
            return ResponseEntity.notFound().build();  // 404 Not Found 반환
        }

        return ResponseEntity.ok(tripInfoDto);  // 200 OK와 함께 반환
    }

    // 나의 모든 여행 정보 조회
    @GetMapping("/myTrip")
    public ResponseEntity<List<TripInfoDto>> getAllTripInfos(Principal principal) {
        List<TripInfoDto> tripInfoDtos = tripInfoService.getMyTripInfos(principal.getName()).stream()
                .map(TripInfoDto::new)
                .sorted((t1, t2) -> Long.compare(t2.getId(), t1.getId()))
                .toList();
        return ResponseEntity.ok(tripInfoDtos);
    }
}