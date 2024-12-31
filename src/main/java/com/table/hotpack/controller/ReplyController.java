package com.table.hotpack.controller;

import com.table.hotpack.dto.AddReplyRequest;
import com.table.hotpack.dto.ReplyResponse;
import com.table.hotpack.dto.UpdateReplyRequest;
import com.table.hotpack.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    @GetMapping("/article/{articleId}")
    public ResponseEntity<Page<ReplyResponse>> getRepliesByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue ="0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReplyResponse> replies = replyService.findRepliesByArticleId(articleId, PageRequest.of(page, size));
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<ReplyResponse> addReply(@RequestBody AddReplyRequest request) {
        ReplyResponse reply = replyService.addReply(request);
        return ResponseEntity.ok(reply);
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @PathVariable Long replyId,
            @RequestBody UpdateReplyRequest request) {
        ReplyResponse reply=replyService.updateReply(replyId, request);
        return ResponseEntity.ok(reply);
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        replyService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }

}
