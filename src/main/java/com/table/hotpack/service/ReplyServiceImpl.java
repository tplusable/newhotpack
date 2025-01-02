package com.table.hotpack.service;

import com.table.hotpack.domain.Article;
import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.ReplyLike;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.*;
import com.table.hotpack.repository.ArticleRepository;
import com.table.hotpack.repository.ReplyLikeRepository;
import com.table.hotpack.repository.ReplyRepository;
import com.table.hotpack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService{

    private final ReplyRepository replyRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ReplyLikeRepository replyLikeRepository;

    private ReplyLikeResponse generateReplyLikeResponse(Reply reply) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean liked = replyLikeRepository.existsByReplyAndUser(reply, user);
        int totalLikeds=replyLikeRepository.countByReply(reply);

        return ReplyLikeResponse.builder()
                .replyId(reply.getReplyId())
                .liked(liked)
                .totalLikes(totalLikeds)
                .build();
    }

    @Override
    public Page<ReplyResponse> findRepliesByArticleId(Long articleId, Pageable pageable) {
        return replyRepository.findByArticleIdOrderByCreatedAtDesc(articleId, pageable)
                .map(reply -> {
                    ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply);
                    return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes());
                });
    }

    @Override
    public ReplyResponse findReplyById(Long replyId) {
        Reply reply= replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes());
    }

    @Override
    public ReplyResponse addReply(AddReplyRequest request, String userName) {
        Article article=articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user=userRepository.findByEmail(userName)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Reply reply=Reply.builder()
                .replyer(user.getNickname())
                .reply(request.getReply())
                .article(article)
                .user(user)
                .author(user.getEmail())
                .build();

        replyRepository.save(reply);
        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes());
    }

    @Override
    public ReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        authorizeReplyAuthor(reply);
        reply.update(request.getReply());

        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes());
    }

    @Override
    public void deleteReply(Long replyId) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        authorizeReplyAuthor(reply);
        replyRepository.delete(reply);
    }


    // 댓글을 작성한 유저인지 확인
    private static void authorizeReplyAuthor(Reply reply) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reply.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    // 추천 토글
    public ReplyLikeResponse toggleLike(Long replyId, String username) {
        User user =userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found"));

        Optional<ReplyLike> existingLike = replyLikeRepository.findByReplyAndUser(reply, user);
        if (existingLike.isPresent()) {
            // 추천 취소
            replyLikeRepository.delete(existingLike.get());
        } else {
            //추천 추가
            ReplyLike newLike= ReplyLike.builder()
                    .reply(reply)
                    .user(user)
                    .build();
            replyLikeRepository.save(newLike);
        }

        int totalLikes=replyLikeRepository.countByReply(reply);
        boolean liked=replyLikeRepository.existsByReplyAndUser(reply, user);

        return ReplyLikeResponse.builder()
                .replyId(replyId)
                .totalLikes(totalLikes)
                .liked(liked)
                .build();
    }

    public List<String> getLikers(Long replyId) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found"));
        return replyLikeRepository.findAllReply(reply).stream()
                .map(rl->rl.getUser().getNickname())
                .collect(Collectors.toList());
    }

}
