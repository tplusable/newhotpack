package com.table.hotpack.post.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class TripInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 여행 정보 ID

    @Column(name = "author", nullable = false)
    private String author;

    private String areaName;  // 여행 지역

    private LocalDate startDate;  // 시작 날짜

    private LocalDate endDate;  // 종료 날짜

    @OneToMany(mappedBy = "tripInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentId> contentIds;  // 해당 여행에 포함된 콘텐츠 ID들

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}