//package com.table.hotpack.controller;
//
//import com.table.hotpack.dto.AddReplyRequest;
//import com.table.hotpack.dto.ReplyLikeResponse;
//import com.table.hotpack.dto.ReplyResponse;
//import com.table.hotpack.dto.UpdateReplyRequest;
//import com.table.hotpack.service.ReplyService;
//import com.table.hotpack.service.UserService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@Log4j2
//@RestController
//@RequestMapping("/api/replies")
//@RequiredArgsConstructor
//public class ReplyControllerOrigin {
//    private final ReplyService replyService;
//    private final UserService userService;
//
//    @GetMapping("/article/{articleId}")
//    public ResponseEntity<Page<ReplyResponse>> getRepliesByArticleId(
//            @PathVariable Long articleId,
//            @RequestParam(defaultValue ="0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<ReplyResponse> replies = replyService.findRepliesByArticleId(articleId, PageRequest.of(page, size));
//        return ResponseEntity.ok(replies);
//    }
//
//    @PostMapping("/article/{articleId}")
//    public ResponseEntity<ReplyResponse> addReply(@RequestBody AddReplyRequest request, Principal principal) {
//        log.info(request.getArticleId());
//        log.info(request.getUserId());
//        log.info(principal);
//        log.info(principal.getName());
//        if (principal == null || principal.getName() == null) {
//            throw new IllegalArgumentException("User authentication information is missing.");
//        }
//
//        if (request.getUserId() == null ) {
//            Long userId= userService.findUserIdByUsername(principal.getName());
//            if (userId == null) {
//                throw new IllegalArgumentException("User not found for the given authentication.");
//            }
//        }
//
//        if (request.getArticleId() == null) {
//            throw new IllegalArgumentException("articleId and userId must not be null");
//        }
//
//
//        ReplyResponse reply = replyService.addReply(request, principal.getName());
//        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
//    }
//
//    @PutMapping("/{replyId}")
//    public ResponseEntity<ReplyResponse> updateReply(
//            @PathVariable Long replyId,
//            @RequestBody UpdateReplyRequest request) {
//        ReplyResponse reply=replyService.updateReply(replyId, request);
//        return ResponseEntity.ok(reply);
//    }
//
//    @DeleteMapping("/{replyId}")
//    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
//        replyService.deleteReply(replyId);
//            return ResponseEntity.noContent().build();
//    }
//
//    // 댓글 추천 토글
//    @PostMapping("/{replyId}/like")
//    public ResponseEntity<ReplyLikeResponse> toggleLike(
//            @PathVariable Long replyId,
//            Principal principal) {
//
//        if (principal == null || principal.getName() == null) {
//            throw new IllegalArgumentException("로그인이 필요합니다.");
//        }
//        ReplyLikeResponse response=replyService.toggleLike(replyId, principal.getName());
//        return ResponseEntity.ok(response); // 업데이트된 추천 수 반환
//    }
//
//    @GetMapping("/{replyId}/likers")
//    public ResponseEntity<List<String>> getLikers(@PathVariable Long replyId) {
//        List<String> likers=replyService.getLikers(replyId);
//        return ResponseEntity.ok(likers);
//    }
//
//    @GetMapping("/article/{articleId}/top-replies")
//    public ResponseEntity<List<ReplyResponse>> getTopRepliesByLikes(
//            @PathVariable Long articleId,
//            @RequestParam(defaultValue= "3") int limit) {
//        List<ReplyResponse> topReplies=replyService.findTopRepliesByLikes(articleId, limit);
//
//        //디버깅로그
//        topReplies.forEach(reply -> {
//            System.out.println("Reply Id: "+reply.getReplyId());
//            System.out.println("Total Likes: "+reply.getTotalLikes());
//        });
//
//        return ResponseEntity.ok(topReplies);
//    }
//
//}
