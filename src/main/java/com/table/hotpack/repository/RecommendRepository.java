package com.table.hotpack.repository;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Recommend;
import com.table.hotpack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    // 유저와 게시글로 추천정보를 조회
    Optional<Recommend> findByArticleAndUser(Article article, User user);

    // 게시글의 전체 추천수 세기
    int countByArticle(Article article);
}
