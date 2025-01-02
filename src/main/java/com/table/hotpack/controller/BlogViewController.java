package com.table.hotpack.controller;

import com.table.hotpack.domain.Article;
import com.table.hotpack.dto.ArticleListViewResponse;
import com.table.hotpack.dto.ArticleViewResponse;
import com.table.hotpack.post.controller.TripInfoController;
import com.table.hotpack.post.dto.TripInfoDto;
import com.table.hotpack.post.service.TripInfoService;
import com.table.hotpack.service.BlogService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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

        List<ArticleListViewResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleListViewResponse::new)
                .toList();
        model.addAttribute("articles", articles);

        return "articleList";
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable("id") Long id, Model model) {
        Article article = blogService.findById(id);
        model.addAttribute("article", new ArticleViewResponse(article));

        model.addAttribute("recommendCount", blogService.getRecommendCount(id));
        return "article";
    }


    @GetMapping("/new-article")
    public String newArticle(@RequestParam(value = "id", required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("article", new ArticleViewResponse());
        } else {
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article));
        }

        // 모든 TripInfo 목록을 가져와서 카테고리로 사용
        List<TripInfoDto> tripInfos = tripInfoService.getAllTripInfos().stream()
                .map(TripInfoDto::new)
                .toList();

        List<TripInfoDto> sortableList = new ArrayList<>(tripInfos);
        sortableList.sort((t1, t2) -> Long.compare(t2.getId(), t1.getId())); // ID 내림차순 정렬
        model.addAttribute("tripInfos", sortableList);

        return "newArticle";
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