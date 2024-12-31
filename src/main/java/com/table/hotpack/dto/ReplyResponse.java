package com.table.hotpack.dto;

import com.table.hotpack.domain.Reply;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReplyResponse {
    private Long replyId;
    private String reply;
    private String replyer;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static ReplyResponse fromEntity(Reply reply) {
        return new ReplyResponse(
                reply.getReplyId(),
                reply.getReply(),
                reply.getReplyer(),
                reply.getCreatedAt(),
                reply.getUpdateAt()
                );
    }
}
