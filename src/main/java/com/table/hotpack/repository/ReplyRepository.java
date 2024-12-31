package com.table.hotpack.repository;

import com.table.hotpack.domain.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByArticleId(Long articleId);
    Page<Reply> findByArticleIdWithPaging(Long articleId, Pageable pageable);
    List<Reply> findByUser(Long userId);
}
