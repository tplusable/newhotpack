package com.table.hotpack.controller;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.*;
import com.table.hotpack.service.BlogService;
import com.table.hotpack.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class BlogApiController {

    private final BlogService blogService;
    private final UserService userService;

    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleRequest request, Principal principal) {
        Article savedArticle = blogService.save(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();

        return ResponseEntity.ok()
                .body(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") long id) {
        Article article = blogService.findById(id);

        return ResponseEntity.ok()
                .body(new ArticleResponse(article));
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
        int newCount = blogService.getRecommendCount(id);
        boolean isNowRecommended = blogService.isRecommended(id, user);

        return new ToggleRecommendResponse(newCount, isNowRecommended);
    }

    // 응답용 DTO
    record ToggleRecommendResponse(int recommendCount, boolean recommended) {}

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
