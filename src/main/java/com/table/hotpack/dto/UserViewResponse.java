package com.table.hotpack.dto;

import com.table.hotpack.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserViewResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String password;

    public UserViewResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.password = user.getPassword();
    }
}
