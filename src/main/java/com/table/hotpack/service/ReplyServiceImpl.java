package com.table.hotpack.service;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import com.table.hotpack.repository.ArticleRepository;
import com.table.hotpack.repository.ReplyRepository;
import com.table.hotpack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService{

    private final ReplyRepository replyRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public Page<ReplyResponse> findRepliesByArticleId(Long articleId, Pageable pageable) {
        return replyRepository.findByArticleIdWithPaging(articleId, pageable)
                .map(ReplyResponse::fromEntity);
    }

    @Override
    public ReplyResponse findReplyById(Long replyId) {
        Reply reply= replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        return ReplyResponse.fromEntity(reply);
    }

    @Override
    public ReplyResponse addReply(AddReplyRequest request) {
        Article article=articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user=userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Reply reply=Reply.builder()
                .replyer(user.getNickname())
                .reply(request.getReply())
                .article(article)
                .user(user)
                .build();

        replyRepository.save(reply);
        return ReplyResponse.fromEntity(reply);
    }

    @Override
    public ReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        reply.update(request.getReply());
        return ReplyResponse.fromEntity(reply);
    }

    @Override
    public void deleteReply(Long replyId) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        replyRepository.delete(reply);
    }
}
