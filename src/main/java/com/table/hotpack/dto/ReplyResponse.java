package com.table.hotpack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.table.hotpack.domain.Reply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReplyResponse {
    private Long replyId;
    private String reply;
    private String replyer;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private boolean liked;
    private int totalLikes;

    public static ReplyResponse fromEntity(Reply reply, boolean liked, int totalLikes) {
        return new ReplyResponse(
                reply.getReplyId(),
                reply.getReply(),
                reply.getReplyer(),
                reply.getCreatedAt(),
                reply.getUpdatedAt(),
                liked,
                totalLikes
                );
    }
}
