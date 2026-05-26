package com.example.identityservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormLogin {
    private String username;
    private String password;
}
