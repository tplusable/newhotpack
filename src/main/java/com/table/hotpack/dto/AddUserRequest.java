package com.table.hotpack.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class AddUserRequest {
    private String email;
    private String name;
    private String nickname;
    private String password;
}