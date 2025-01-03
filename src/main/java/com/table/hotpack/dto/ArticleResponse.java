package com.table.hotpack.dto;

import com.table.hotpack.domain.Article;
import lombok.Getter;

@Getter
public class ArticleResponse {
    private final Long id;
    private final String title;
    private final String content;

    private final Long recommendCount;
    private final boolean recommended;

    public ArticleResponse(Article article, Long recommendCount, boolean recommended) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.recommendCount = recommendCount;
        this.recommended = recommended;
    }
}
