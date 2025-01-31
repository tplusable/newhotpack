package com.table.hotpack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.table.hotpack.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddArticleRequest {
    private String title;

    private String content;

    private Long tripInfoId;


//    public Article toEntity(String author, String nickname) {
//        return Article.builder()
//                .title(title)
//                .content(content)
//                .author(author)
//                .nickname(nickname)
//                .build();
//    }
}
