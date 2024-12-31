package com.table.hotpack.service;

import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReplyService {
    Page<ReplyResponse> findRepliesByArticleId(Long articleId, Pageable pageable);
    ReplyResponse findReplyById(Long replyId);
    ReplyResponse addReply(AddReplyRequest request);
    ReplyResponse updateReply(Long replyId, UpdateReplyRequest request);
    void deleteReply(Long replyId);
}
