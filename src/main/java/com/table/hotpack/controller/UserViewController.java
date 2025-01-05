package com.table.hotpack.controller;

import com.table.hotpack.dto.AddUserRequest;
import com.table.hotpack.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@RequiredArgsConstructor
@Controller
public class UserViewController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("addUserRequest", new AddUserRequest());
        return "signup";
    }

//    @GetMapping("/mypage")
//    public String mypage() {
//        return "mypage";
//    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            // 인증된 사용자의 정보 처리
            String username = authentication.getName();
            log.info("Current authenticated user: {}", username);
            model.addAttribute("username", username); // 인증된 사용자 이름을 모델에 추가
        } else {
            // 인증되지 않은 사용자
            log.info("Anonymous user accessing mypage.");
        }

        return "mypage"; // 인증 여부와 상관없이 mypage.html 반환
    }

//    @GetMapping("/mypage")
//    public String mypage(Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        // 인증 객체 디버깅 로그 추가
//        if (authentication != null) {
//            log.info("Authentication object: {}", authentication);
//            log.info("Principal: {}", authentication.getPrincipal());
//            log.info("Authorities: {}", authentication.getAuthorities());
//        } else {
//            log.info("Authentication is null");
//        }
//
//        if (authentication == null || !authentication.isAuthenticated() ||
//                "anonymousUser".equals(authentication.getPrincipal())) {
//            return "redirect:/login";
//        }
//
//        String username = authentication.getName();
//        model.addAttribute("username", username);
//
//        return "mypage";
//    }

    @GetMapping("/updateUser")
    public String getUser() {
        return "updateUser";
    }
}