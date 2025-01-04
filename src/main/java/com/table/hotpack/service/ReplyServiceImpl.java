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
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService{

    private final ReplyRepository replyRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ReplyLikeRepository replyLikeRepository;

    private ReplyLikeResponse generateReplyLikeResponse(Reply reply, String currentUsername) {
        boolean liked=false;

        if (currentUsername != null) {
            User user = userRepository.findByEmail(currentUsername).orElse(null);
            liked = user!=null && replyLikeRepository.existsByReplyAndUser(reply, user);
        }

        // 디버깅 로그 추가
        log.info("Reply ID: " + reply.getReplyId());
        log.info("Liked: " + liked);

        int totalLikes = replyLikeRepository.countByReply(reply);

        log.info("Total Likes: " + totalLikes);

        return ReplyLikeResponse.builder()
                .replyId(reply.getReplyId())
                .liked(liked)
                .totalLikes(totalLikes)
                .build();

    }

    @Override
    public Page<ReplyResponse> findRepliesByArticleId(Long articleId, Pageable pageable) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        return replyRepository.findByArticleIdOrderByCreatedAtDesc(articleId, pageable)
                .map(reply -> {
                    ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, currentUsername);
                    boolean isAuthor = currentUsername != null && reply.getAuthor().equals(currentUsername);
                    return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), isAuthor);
                });
    }

    @Override
    public ReplyResponse findReplyById(Long replyId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;


        Reply reply= replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, currentUsername);
        boolean isAuthor = currentUsername != null && reply.getAuthor().equals(currentUsername);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), isAuthor);
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
        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, userName);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), true);
    }

    @Override
    public ReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        authorizeReplyAuthor(reply);
        reply.update(request.getReply());

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, currentUsername);
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), true);
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

    public List<ReplyResponse> findTopRepliesByLikes(Long articleId, int limit) {
        Pageable pageable= PageRequest.of(0, limit); //limit 수 만큼 페이징
        List<Reply> topReplies=replyRepository.findTopRepliesByLikes(articleId, pageable);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        return topReplies.stream()
                .map(reply -> {
                    ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, currentUsername);
                    boolean isAuthor = currentUsername != null && reply.getAuthor().equals(currentUsername);
                    return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), isAuthor);
                })
                .collect(Collectors.toList());
    }

    public Page<ReplyResponse> findRepliesByArticleIdWithAuthorCheck(Long articleId, PageRequest pageRequest, String currentUsername) {
        Page<Reply> replies = replyRepository.findByArticleIdWithPaging(articleId, pageRequest);


        return replies.map(reply -> {
            ReplyLikeResponse replyLikeResponse=generateReplyLikeResponse(reply, currentUsername);
            boolean isAuthor = currentUsername != null && reply.getReplyer().equals(currentUsername);
            return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), isAuthor);
        });
    }

}
