package com.table.hotpack.service;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Recommend;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.AddArticleRequest;
import com.table.hotpack.dto.UpdateArticleRequest;
import com.table.hotpack.repository.ArticleRepository;
import com.table.hotpack.repository.RecommendRepository;
import com.table.hotpack.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository; // 사용자 조회를 위해 필요
    private final RecommendRepository recommendRepository;


    public Article save(AddArticleRequest request, String userName) {
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userName));

        String nickname = user.getNickname(); // nickname 가져오기
        return articleRepository.save(request.toEntity(userName, nickname));
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public Article findById(long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));
    }

    public void delete(long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        articleRepository.delete(article);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    public List<Article> getUserArticles(String email) {
        return articleRepository.findByAuthor(email);
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    /** 이미 추천했으면 취소, 안했으면 추천 등록 */
    public void toggleRecommend(Long id, User user) {
        Article article = findById(id);

        Optional<Recommend> recommendOpt = recommendRepository.findByArticleAndUser(article, user);
        if (recommendOpt.isPresent()) {
            // 이미 추천한 상태 → 추천 취소
            recommendRepository.delete(recommendOpt.get());
        } else {
            // 추천 안 한 상태 → 추천 등록
            Recommend recommend = new Recommend(article, user);
            recommendRepository.save(recommend);
        }
    }

    // 해당 게시글의 추천수
    public Long getRecommendCount(Long id) {
        Article article = findById(id);
        return recommendRepository.countByArticle(article);
    }

    // 추천수 가장 높은 글 조회
    public Optional<Article> findBestRecommendedArticle() {
        return articleRepository.findAll().stream()
                .max(Comparator.comparingLong(article -> recommendRepository.countByArticleId(article.getId())));
    }

    // 추천수가 가장 높은 글을 제외한 나머지 글 최신순으로 조회
    public List<Article> findOtherArticleExcluding(Long topArticleId) {
        return articleRepository.findAll().stream()
                .filter(article -> !article.getId().equals(topArticleId))
                .sorted(Comparator.comparing(Article::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    // 추천수가 높은 글이 없을 경우, 모든 글을 최신순으로 조회
    public List<Article> findAllByOrderByCreatedDateDesc() {
        return articleRepository.findAll().stream()
                .sorted(Comparator.comparing(Article::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    // 특정 유저가 해당 게시글을 추천했는지 여부
    public boolean isRecommended(Long id, User user) {
        Article article = findById(id);
        return recommendRepository.findByArticleAndUser(article, user).isPresent();
    }

    // 특정 유저가 추천한 게시글 목록 조회
    public List<Article> getUserRecommendedArticles(String email) {
        // 유저 엔티티를 찾음
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다: " + email));

        // 해당 유저가 추천한 Recommend 레코드 목록
        List<Recommend> recommends = recommendRepository.findAllByUser(user);

        // Recommend -> Article 매핑
        return recommends.stream()
                .map(Recommend::getArticle)
                .collect(Collectors.toList());
    }

}