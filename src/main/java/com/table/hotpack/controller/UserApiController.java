package com.table.hotpack.controller;

import com.table.hotpack.dto.AddUserRequest;
import com.table.hotpack.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UserApiController {
    private final UserService userService;

    @PostMapping("/user")
    public String signup(AddUserRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        if (!request.getPassword1().equals(request.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordIncorrect",
                    "2개의 비밀번호가 일치하지 않습니다.");
            return "signup";
        }

        try {
            userService.save(request);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup";
        } catch (Exception e) {
            bindingResult.reject("signupFailed", "회원가입 중 오류가 발생했습니다.");
            return "signup";
        }
        return "redirect:/login";
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
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

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }
}
