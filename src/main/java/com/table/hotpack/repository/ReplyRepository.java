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
    @Query("SELECT r FROM Reply r WHERE r.article.id= :articleId ORDER BY r.createdAt DESC")
    Page<Reply> findByArticleIdWithPaging(@Param("articleId")Long articleId, Pageable pageable);

    Page<Reply> findByArticleIdOrderByCreatedAtDesc(Long articleId, Pageable pagealbe);

    @Query("SELECT r FROM Reply r WHERE r.user.id= :userId")
    List<Reply> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reply r LEFT JOIN r.replyLikes rl "+
            "WHERE r.article.id = :articleId " +
            "GROUP BY r " +
            "ORDER BY COUNT(rl) DESC, r.createdAt DESC")
    List<Reply> findTopRepliesByLikes(@Param("articleId") Long article, Pageable pageable);

    @Query("SELECT r FROM Reply r WHERE r.user =:userId")
    List<Reply> findMyRepliesByUserId(@Param("userId") Long userId);
}
