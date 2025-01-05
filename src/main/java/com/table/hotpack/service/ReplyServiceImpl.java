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
import org.springframework.security.core.Authentication;
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

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    private ReplyResponse createReplyResponse(Reply reply, String currentUsername) {
        ReplyLikeResponse replyLikeResponse = generateReplyLikeResponse(reply, currentUsername);
        boolean isAuthor = currentUsername != null && currentUsername.equals(reply.getAuthor());
        return ReplyResponse.fromEntity(reply, replyLikeResponse.isLiked(), replyLikeResponse.getTotalLikes(), isAuthor);
    }

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
        String currentUsername = getCurrentUsername();

        return replyRepository.findByArticleIdOrderByCreatedAtDesc(articleId, pageable)
                .map(reply ->  createReplyResponse(reply, currentUsername));
    }

    @Override
    public ReplyResponse findReplyById(Long replyId) {
        String currentUsername = getCurrentUsername();


        Reply reply= replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        return createReplyResponse(reply, currentUsername);
    }

    @Override
    public ReplyResponse addReply(AddReplyRequest request, String username) {
        Article article=articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user=userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Reply reply=Reply.builder()
                .replyer(user.getNickname())
                .reply(request.getReply())
                .article(article)
                .user(user)
                .author(user.getEmail())
                .build();

        replyRepository.save(reply);
        return createReplyResponse(reply, username);
    }

    @Override
    public ReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        Reply reply=replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        authorizeReplyAuthor(reply);
        reply.update(request.getReply());

        String currentUsername = getCurrentUsername();
        return createReplyResponse(reply, currentUsername);
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
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reply.getAuthor().equals(currentUsername)) {
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

        return generateReplyLikeResponse(reply, username);
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

        String currentUsername = getCurrentUsername();

        return topReplies.stream()
                .map(reply -> createReplyResponse(reply,  currentUsername))
                .collect(Collectors.toList());
    }

    public Page<ReplyResponse> findRepliesByArticleIdWithAuthorCheck(Long articleId, PageRequest pageRequest, String currentUsername) {
        Page<Reply> replies = replyRepository.findByArticleIdWithPaging(articleId, pageRequest);


        return replies.map(reply -> createReplyResponse(reply, currentUsername));
    }

    @Override
    public List<ReplyResponse> findMyRepliesByUserId(Long userId) {
        List<Reply> replies = replyRepository.findMyRepliesByUserId(userId);

        String currentUsername = getCurrentUsername();

        return replies.stream()
                .map(reply -> createReplyResponse(reply, currentUsername))
                        .collect(Collectors.toList());

    }

}
