package com.table.hotpack.controller;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.*;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.repository.RecommendRepository;
import com.table.hotpack.service.BlogService;
import com.table.hotpack.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class BlogApiController {

    private final BlogService blogService;
    private final UserService userService;
    private final RecommendRepository recommendRepository;

    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleRequest request, Principal principal) {
        Article savedArticle = blogService.save(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") long id, Principal principal) {
        Article article = blogService.findById(id);

        Long recommendCount = recommendRepository.countByArticle(article);

        boolean recommended = false;
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                recommended = recommendRepository.findByArticleAndUser(article, user).isPresent();
            }
        }

        return ResponseEntity.ok()
                .body(new ArticleResponse(article, recommendCount, recommended));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("id") long id) {
        blogService.delete(id);

        return ResponseEntity.ok()
                .build();
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable("id") long id,
                                                 @RequestBody UpdateArticleRequest request) {
        Article updatedArticle = blogService.update(id, request);

        return ResponseEntity.ok()
                .body(updatedArticle);
    }

    // 추천/취소 토글
    @PostMapping("/api/articles/{id}/recommend")
    public ToggleRecommendResponse toggleRecommendResponse(@PathVariable("id") Long id,
                                                           Principal principal) {
        User user = userService.findByEmail(principal.getName());

        blogService.toggleRecommend(id, user);
        Long newCount = blogService.getRecommendCount(id);
        boolean isNowRecommended = blogService.isRecommended(id, user);

        return new ToggleRecommendResponse(newCount, isNowRecommended);
    }

    // 응답용 DTO
    record ToggleRecommendResponse(Long recommendCount, boolean recommended) {}

    // 추천한 글 목록 조회
    @GetMapping("/api/user/recommended-articles")
    public ResponseEntity<List<ArticleListViewResponse>> getUserRecommendedArticles(Principal principal) {
        // 내가 추천한 Article 목록
        List<ArticleListViewResponse> articles = blogService.getUserRecommendedArticles(principal.getName())
                .stream()
                .map(ArticleListViewResponse::new)
                .toList();

        return ResponseEntity.ok(articles);
    }

    @GetMapping("/api/user/articles")
    public ResponseEntity<List<ArticleListViewResponse>> getUserArticles(Principal principal) {
        List<ArticleListViewResponse> articles = blogService.getUserArticles(principal.getName())
                .stream()
                .map(ArticleListViewResponse::new)
                .toList();

        return ResponseEntity.ok()
                .body(articles);
    }
}
