package com.table.hotpack.dto;

import com.table.hotpack.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String email;
    private final String name;
    private final String nickname;
    private final String password;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.password = user.getPassword();
    }
}
