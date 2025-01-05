package com.table.hotpack.controller;

import com.table.hotpack.domain.Reply;
import com.table.hotpack.domain.User;
import com.table.hotpack.dto.ReplyLikeResponse;
import com.table.hotpack.service.UserService;
import lombok.extern.log4j.Log4j2;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import com.table.hotpack.service.ReplyService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

@Log4j2
@RestController
//@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;
    private final UserService userService;

    @GetMapping("/replies/article/{articleId}")
    public ResponseEntity<Page<ReplyResponse>> getRepliesByArticleId(
            @PathVariable("articleId") Long articleId,
            @RequestParam(name = "page", defaultValue ="0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            currentUsername=authentication.getName();
            log.info("Current User: {}", currentUsername);
        } else {
            log.info("Anonymous User is accessing replies");
        }

        log.info("Article ID: {}", articleId);
        log.info("Page: {}, Size: {}", page, size);
        log.info("Current User: {}", currentUsername != null ? currentUsername : "Anonymous");

        Page<ReplyResponse> replies = replyService.findRepliesByArticleIdWithAuthorCheck(articleId, PageRequest.of(page, size), currentUsername);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/api/replies/article/{articleId}")
    public ResponseEntity<ReplyResponse> addReply(@RequestBody AddReplyRequest request, Principal principal) {
        log.info(request.getArticleId());
        log.info(request.getUserId());
        log.info(principal);
        log.info(principal.getName());
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("User authentication information is missing.");
        }

        if (request.getUserId() == null ) {
            Long userId= userService.findUserIdByUsername(principal.getName());
            if (userId == null) {
                throw new IllegalArgumentException("User not found for the given authentication.");
            }
        }

        if (request.getArticleId() == null) {
            throw new IllegalArgumentException("articleId and userId must not be null");
        }


        ReplyResponse reply = replyService.addReply(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @PutMapping("/api/replies/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @PathVariable("replyId") Long replyId,
            @RequestBody UpdateReplyRequest request) {
        ReplyResponse reply=replyService.updateReply(replyId, request);
        return ResponseEntity.ok(reply);
    }

    @DeleteMapping("/api/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable("replyId") Long replyId) {
        replyService.deleteReply(replyId);
            return ResponseEntity.noContent().build();
    }

    // 댓글 추천 토글
    @PostMapping("/api/replies/{replyId}/like")
    public ResponseEntity<ReplyLikeResponse> toggleLike(
            @PathVariable("replyId") Long replyId,
            Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        ReplyLikeResponse response=replyService.toggleLike(replyId, principal.getName());
        return ResponseEntity.ok(response); // 업데이트된 추천 수 반환
    }

    @GetMapping("/replies/{replyId}/likers")
    public ResponseEntity<List<String>> getLikers(@PathVariable("replyId") Long replyId) {
        List<String> likers=replyService.getLikers(replyId);
        return ResponseEntity.ok(likers);
    }

    @GetMapping("/replies/article/{articleId}/top-replies")
    public ResponseEntity<List<ReplyResponse>> getTopRepliesByLikes(
            @PathVariable Long articleId,
            @RequestParam(defaultValue= "1") int limit) {


        List<ReplyResponse> topReplies=replyService.findTopRepliesByLikes(articleId, limit);

        //디버깅로그
        topReplies.forEach(reply -> {
            System.out.println("Reply Id: "+reply.getReplyId());
            System.out.println("Total Likes: "+reply.getTotalLikes());
        });

        return ResponseEntity.ok(topReplies);
    }

//    @GetMapping("/api/user/my-replies")
//    public ResponseEntity<List<ReplyResponse>> getUserReplies(Principal principal) {
//
//        Long userId =null;
//
//        log.info("@@@@@@@principal:",principal);
//
//        if (principal == null || principal.getName() == null) {
//            throw new IllegalArgumentException("User authentication information is missing.");
//        }
//
//        log.info("@@@@@@@@email:",principal.getName());
//
//        if (userService.findUserIdByUsername(principal.getName()) == null ) {
//            userId= userService.findUserIdByUsername(principal.getName());
//            if (userId == null) {
//                throw new IllegalArgumentException("User not found for the given authentication.");
//            }
//        }
//
//        log.info("@@@@@@@@@@@@userid:",userId);
//
//        List<ReplyResponse> replies= replyService.findMyRepliesByUserId(userId);
//        return ResponseEntity.ok(replies);
//    }

//    @GetMapping("/api/user/my-replies")
//    public ResponseEntity<List<ReplyResponse>> getUserReplies(@AuthenticationPrincipal User user) {
//        if (user == null) {
//            throw new IllegalArgumentException("로그인이 필요합니다.");
//        }
//
//        String username = user.getUsername();
//        Long userId = userService.findUserIdByUsername(username);
//        if (userId == null) {
//            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
//        }
//
//        List<ReplyResponse> replies = replyService.findMyRepliesByUserId(userId);
//        return ResponseEntity.ok(replies);
//    }

    @GetMapping("/api/user/my-replies")
    public ResponseEntity<List<ReplyResponse>> getUserReplies() {
        // SecurityContextHolder를 통해 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User authentication information is missing.");
        }

        String username = authentication.getName();

        log.info("@@@@@@@@ 현재 인증된 사용자 이메일: {}", username);

        // 사용자 ID 조회
        Long userId = userService.findUserIdByUsername(username);
        if (userId == null) {
            throw new IllegalArgumentException("User not found for the given authentication.");
        }

        log.info("@@@@@@@@@@@@ 사용자 ID: {}", userId);

        // 댓글 조회
        List<ReplyResponse> replies = replyService.findMyRepliesByUserId(userId);

        if (replies.isEmpty()) {
            log.info("@@@@@@@@@@@@ 사용자가 작성한 댓글이 없습니다.");
        }

        return ResponseEntity.ok(replies);
    }

}
