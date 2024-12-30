package com.table.hotpack.controller;

import com.table.hotpack.domain.Article;
import com.table.hotpack.dto.ArticleListViewResponse;
import com.table.hotpack.dto.ArticleViewResponse;
import com.table.hotpack.service.BlogService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;

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

        return "newArticle";
    }

    @GetMapping("/myArticles")
    public String getMyArticles() {
        return "myArticleList";
    }

}