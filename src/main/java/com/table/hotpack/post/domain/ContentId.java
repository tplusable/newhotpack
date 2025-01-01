package com.table.hotpack.post.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ContentId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 콘텐츠 ID 엔티티 ID

    private String contentId;  // 콘텐츠의 고유 ID

    private String dayIndex;  // 해당 콘텐츠가 포함된 날짜 (예: 1일차, 2일차 등)

    @ManyToOne
    @JoinColumn(name = "trip_info_id")
    private TripInfo tripInfo;  // 해당 콘텐츠가 포함된 여행 정보

    public ContentId(String contentId, String dayIndex, TripInfo tripInfo) {
        this.contentId = contentId;
        this.dayIndex = dayIndex;
        this.tripInfo = tripInfo;
    }
}