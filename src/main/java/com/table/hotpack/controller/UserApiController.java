package com.table.hotpack.controller;

import com.table.hotpack.dto.AddUserRequest;
import com.table.hotpack.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UserApiController {
    private final UserService userService;

    @PostMapping("/user")
    public String signup(@Valid AddUserRequest request, BindingResult bindingResult) {

        // 유효성 검사 에러가 있으면 다시 회원가입 페이지로
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        // 비밀번호 확인 일치 여부 검증
        if (!request.getPassword1().equals(request.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 비밀번호가 일치하지 않습니다.");
            return "signup";
        }

        // 사용자 저장 로직
        try {
            userService.save(request);
        } catch (IllegalArgumentException e) {
            // 중복 이메일/닉네임 등의 사용자 에러 처리
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup";
        } catch (Exception e) {
            // 기타 예상하지 못한 에러 처리
            bindingResult.reject("signupFailed", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "signup";
        }

        // 회원가입 성공 시 로그인 페이지로 리다이렉트
        return "redirect:/login";
    }

    @PostMapping("/update-user")
    public String updateUser(@AuthenticationPrincipal UserDetails userDetails, @Valid AddUserRequest request, BindingResult bindingResult) {

        if (userDetails == null) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }

        if (bindingResult.hasErrors()) {
            return "updateUser";
        }

        if (!request.getPassword1().equals(request.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 비밀번호가 일치하지 않습니다.");
            return "updateUser";
        }

        try {
            userService.update(request);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateUserFailed", e.getMessage());
            return "updateUser";
        } catch (Exception e) {
            bindingResult.reject("updateUserFailed", "회원정보 수정 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "updateUser";
        }
        String username = userDetails.getUsername();
        return "redirect:/mypage";
    }

    // 이메일 중복 확인 API
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean exists = userService.checkEmailExists(email);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam("nickname") String nickname) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = userService.checkNicknameExists(nickname);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }
}
