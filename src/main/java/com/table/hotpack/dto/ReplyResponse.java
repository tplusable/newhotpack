package com.table.hotpack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.table.hotpack.domain.Reply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyResponse {
    private Long replyId;
    private String reply;
    private String replyer;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    @JsonProperty("liked")
    private boolean liked;
    private int totalLikes;
    private String author;
    @JsonProperty("isAuthor")
    private boolean isAuthor;
    

    public static ReplyResponse fromEntity(Reply reply, boolean liked, int totalLikes, boolean isAuthor) {
        return ReplyResponse.builder()
                .replyId(reply.getReplyId())
                .reply(reply.getReply())
                .replyer(reply.getReplyer())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .author(reply.getAuthor())
                .liked(liked)
                .totalLikes(totalLikes)
                .isAuthor(isAuthor)
                .build();
    }

//    public static ReplyResponse fromEntity(Reply reply, boolean liked, int totalLikes, boolean isAuthor) {
//        return new ReplyResponse(
//                reply.getReplyId(),
//                reply.getReply(),
//                reply.getReplyer(),
//                reply.getCreatedAt(),
//                reply.getUpdatedAt(),
//                liked,
//                totalLikes,
//                isAuthor(isAuthor)
//                );
//    }
}
