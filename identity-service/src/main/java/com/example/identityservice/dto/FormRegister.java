package com.example.identityservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormRegister {
    private String username;
    private String password;
    private List<String> roles;
    private List<String> permissions;
    private String email;
}
