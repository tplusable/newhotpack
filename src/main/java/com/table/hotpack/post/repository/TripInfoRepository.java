package com.table.hotpack.post.repository;


import com.table.hotpack.post.domain.TripInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripInfoRepository extends JpaRepository<TripInfo, Long> {
    List<TripInfo> findByAuthor(String author);
}

