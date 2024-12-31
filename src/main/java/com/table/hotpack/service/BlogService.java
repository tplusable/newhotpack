package com.table.hotpack.service;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Recommend;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.AddArticleRequest;
import com.table.hotpack.dto.UpdateArticleRequest;
import com.table.hotpack.repository.BlogRepository;
import com.table.hotpack.repository.RecommendRepository;
import com.table.hotpack.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository; // 사용자 조회를 위해 필요
    private final RecommendRepository recommendRepository;


    public Article save(AddArticleRequest request, String userName) {
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userName));

        String nickname = user.getNickname(); // nickname 가져오기
        return blogRepository.save(request.toEntity(userName, nickname));
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));
    }

    public void delete(long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    public List<Article> getUserArticles(String email) {
        return blogRepository.findByAuthor(email);
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
    public int getRecommendCount(Long id) {
        Article article = findById(id);
        return recommendRepository.countByArticle(article);
    }

    // 특정 유저가 해당 게시글을 추천했는지 여부
    public boolean isRecommended(Long id, User user) {
        Article article = findById(id);
        return recommendRepository.findByArticleAndUser(article, user).isPresent();
    }

}