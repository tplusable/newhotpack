package com.table.hotpack.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyLikeResponse {
    private Long replyId;
    private int totalLikes; // 총추천수
    private boolean liked; // 현재 사용자가 좋아요를 눌렀는지 여부
}
