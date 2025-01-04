package com.table.hotpack.service;

import com.table.hotpack.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface ReplyService {
    Page<ReplyResponse> findRepliesByArticleId(Long articleId, Pageable pageable);
    ReplyResponse findReplyById(Long replyId);
    ReplyResponse addReply(AddReplyRequest request, String userName);
    ReplyResponse updateReply(Long replyId, UpdateReplyRequest request);
    void deleteReply(Long replyId);
    ReplyLikeResponse toggleLike(Long replyId, String username);
    List<String> getLikers(Long replyId);
    List<ReplyResponse> findTopRepliesByLikes(Long articleId, int limit);
    Page<ReplyResponse> findRepliesByArticleIdWithAuthorCheck(Long articleId, PageRequest pageRequest, String currentUsername);
}
