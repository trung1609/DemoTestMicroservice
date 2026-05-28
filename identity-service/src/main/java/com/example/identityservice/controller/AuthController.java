package com.example.identityservice.controller;

import com.example.identityservice.dto.FormLogin;
import com.example.identityservice.dto.FormRegister;
import com.example.identityservice.dto.JwtResponse;
import com.example.identityservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody FormRegister formRegister){
        String response = authService.register(formRegister);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody FormLogin formLogin){
        return ResponseEntity.ok(authService.login(formLogin));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@RequestParam String refreshToken){
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @RequestParam String refreshToken){
        authService.logout(request, refreshToken);
        return ResponseEntity.ok("Logout successfully");
    }
}
