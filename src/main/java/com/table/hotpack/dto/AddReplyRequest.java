package com.table.hotpack.dto;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddReplyRequest {
    private Long articleId;
    private Long userId;
    private String reply;

    public Reply toEntity(String author, String nickname, Article article, User user) {
        return Reply.builder()
                .reply(reply)
                .replyer(nickname)
                .author(author)
                .article(article)
                .user(user)
                .build();
    }
}


