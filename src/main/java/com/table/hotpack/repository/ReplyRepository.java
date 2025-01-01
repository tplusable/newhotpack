package com.table.hotpack.repository;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByArticle(Article article);
    @Query("SELECT r FROM Reply r WHERE r.user.id= :userId")
    List<Reply> findByArticleId(@Param("articleId") Long articleId);


//    Page<Reply> findByArticleId(Long articleId, Pageable pageable);
    @Query("SELECT r FROM Reply r WHERE r.article.id= :articleId")
    Page<Reply> findByArticleIdWithPaging(@Param("articleId")Long articleId, Pageable pageable);

    Page<Reply> findByArticleIdOrderByCreatedAtDesc(Long articleId, Pageable pagealbe);

    List<Reply> findByUser(User user);
    @Query("SELECT r FROM Reply r WHERE r.user.id= :userId")
    List<Reply> findByUserId(@Param("userId") Long userId);
}
