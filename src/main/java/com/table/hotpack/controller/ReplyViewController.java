//package com.table.hotpack.controller;
//
//import com.table.hotpack.dto.ReplyResponse;
//import com.table.hotpack.service.ReplyService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequiredArgsConstructor
//public class ReplyViewController {
//    private final ReplyService replyService;
//
//    @GetMapping("/article/{articleId}/replies")
//    public String getRepliesByArticleId(
//            @PathVariable Long articleId,
//            @RequestParam(defaultValue="0") int page,
//            @RequestParam(defaultValue="10") int size,
//            Model model) {
//        Page<ReplyResponse> replies=replyService.findRepliesByArticleId(articleId, PageRequest.of(page, size));
//        model.addAttribute("replies", replies.getContent());
//        model.addAttribute("totalPages", replies.getTotalPages());
//        return "replies"; //replies.html 파일 반환
//    }
//
//    @GetMapping("/article/{articleId}/top-replies")
//    public String getTopRepliesByLikes(
//            @PathVariable Long articleId,
//            @RequestParam(defaultValue = "3") int limit,
//            Model model) {
//        model.addAttribute("topReplies", replyService.findTopRepliesByLikes(articleId, limit));
//        return "top-replies"; // top-replies.html 파일 반환
//    }
//
//}
