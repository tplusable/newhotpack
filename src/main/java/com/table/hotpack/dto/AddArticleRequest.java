package com.table.hotpack.dto;

import com.table.hotpack.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddArticleRequest {
    private String title;

    private String content;

    public Article toEntity(String author, String nickname) {
        return Article.builder()
                .title(title)
                .content(content)
                .author(author)
                .nickname(nickname)
                .build();
    }
}
