package com.table.hotpack.controller;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.ArticleListViewResponse;
import com.table.hotpack.dto.ArticleViewResponse;
import com.table.hotpack.post.controller.TripInfoController;
import com.table.hotpack.post.domain.TripInfo;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.service.TripInfoService;
import com.table.hotpack.service.BlogService;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;
    private final TripInfoService tripInfoService;

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/articles")
    public String getArticles(Model model) {
        Optional<Article> topArticleOpt = blogService.findBestRecommendedArticle();

        if (topArticleOpt.isPresent()) {
            Article toparticle = topArticleOpt.get();
            model.addAttribute("topArticle", toparticle);
            model.addAttribute("topArticleRecommendCount", blogService.getRecommendCount(toparticle.getId()));

            List<Article> otherArticles = blogService.findOtherArticleExcluding(toparticle.getId());
            model.addAttribute("articles", otherArticles);

            // 나머지 글들의 추천 수를 맵으로 추가
            Map<Long, Long> recommendCounts = otherArticles.stream()
                    .collect(Collectors.toMap(
                            Article::getId,
                            article -> blogService.getRecommendCount(article.getId())
                    ));
            model.addAttribute("recommendCounts", recommendCounts);
        } else {
            List<Article> articles = blogService.findAllByOrderByCreatedDateDesc();
            model.addAttribute("articles", articles);

            // 모든 글들의 추천 수를 맵으로 추가
            Map<Long, Long> recommendCounts = articles.stream()
                    .collect(Collectors.toMap(
                            Article::getId,
                            article -> blogService.getRecommendCount(article.getId())
                    ));
            model.addAttribute("recommendCounts", recommendCounts);
        }

        return "articleList";
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable("id") Long id, Model model) {
        Article article = blogService.findById(id);

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String currentUsername= authentication.getName();
            log.info("Current user: {}", currentUsername);
            model.addAttribute("currentUser", currentUsername);
        } else {
            log.info("User is not logged in.");
        }

        model.addAttribute("article", new ArticleViewResponse(article));

        model.addAttribute("recommendCount", blogService.getRecommendCount(id));
        return "article";
    }



    @GetMapping("/new-article")
    public String newArticle(@RequestParam(value = "id", required = false) Long id, Model model) {
        // id가 null이면 새 글 작성, id가 있으면 기존 글 수정
        if (id == null) {
            model.addAttribute("article", new ArticleViewResponse());
        } else {
            // id가 null이 아니면 해당 ID로 게시글 조회
            Article article = blogService.findById(id);  // id로 게시글을 찾음
            if (article != null) {
                model.addAttribute("article", new ArticleViewResponse(article)); // ArticleViewResponse 객체에 넣어 모델에 추가
            } else {
                // 만약 해당 게시글이 존재하지 않으면 오류 페이지로 리다이렉트 또는 에러 메시지 처리
                model.addAttribute("error", "게시글을 찾을 수 없습니다.");
                return "error";  // error 페이지로 리다이렉트
            }
        }

        return "newArticle"; // 새 글 작성 페이지로 이동
    }

    @GetMapping("/myArticles")
    public String getMyArticles() {
        return "myArticleList";
    }

    @GetMapping("myRecommends")
    public String getMyRecommends() {
        return "myRecommendsList";
    }

}