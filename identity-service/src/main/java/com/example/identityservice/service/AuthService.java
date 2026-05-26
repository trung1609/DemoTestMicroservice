package com.example.identityservice.service;

import com.example.identityservice.config.JwtProvider;
import com.example.identityservice.dto.FormLogin;
import com.example.identityservice.dto.FormRegister;
import com.example.identityservice.dto.JwtResponse;
import com.example.identityservice.entity.Users;
import com.example.identityservice.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration_refresh_token}")
    private long expirationRefreshToken;

    @Value("${jwt.expiration_access_token}")
    private long expirationAccessToken;

    public String register(FormRegister formRegister){
        Users users = userRepository.findByUsername(formRegister.getUsername()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        users.setUsername(formRegister.getUsername());
        users.setPassword(passwordEncoder.encode(formRegister.getPassword()));
        users.setRoles(formRegister.getRoles());
        users.setPermissions(formRegister.getPermissions());
        userRepository.save(users);
        return "User registered successfully";
    }

    public JwtResponse login(FormLogin formLogin){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        formLogin.getUsername(),
                        formLogin.getPassword()
                )
        );
        Users users = (Users) authentication.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(users);
        String refreshToken = jwtProvider.generateRefreshToken(users);

        redisTemplate.opsForValue().set("refreshToken:" + refreshToken, users.getUsername(), expirationRefreshToken, TimeUnit.MILLISECONDS);
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiration(new Date(System.currentTimeMillis() + expirationAccessToken))
                .refreshTokenExpiration(new Date(System.currentTimeMillis() + expirationRefreshToken))
                .build();
    }

    public JwtResponse refresh(String refreshToken){
        if (!redisTemplate.hasKey("refreshToken:" + refreshToken)){
            throw new JwtException("Refresh token not found");
        }
        long expiration = jwtProvider.extractAllClaims(refreshToken).getExpiration().getTime();
        long ttl = expiration - System.currentTimeMillis();
        if (ttl < 0){
            throw new JwtException("Refresh token has expired");
        }
        String username = jwtProvider.extractAllClaims(refreshToken).getSubject();
        Users users = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        String accessToken = jwtProvider.generateAccessToken(users);
        String refreshTokenNew = jwtProvider.generateRefreshToken(users);

        redisTemplate.delete("refreshToken:" + refreshToken);
        redisTemplate.opsForValue().set("refreshToken:" + refreshTokenNew, users.getUsername(), expirationRefreshToken, TimeUnit.MILLISECONDS);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenNew)
                .accessTokenExpiration(new Date(System.currentTimeMillis() + expirationAccessToken))
                .refreshTokenExpiration(new Date(System.currentTimeMillis() + expirationRefreshToken))
                .build();
    }

    public void logout(HttpServletRequest request, String refreshToken){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new RuntimeException("Invalid access token");
        }
        String accessToken = authHeader.substring(7);
        try {
            long expiration = jwtProvider.extractAllClaims(accessToken).getExpiration().getTime();
            long ttl = expiration - System.currentTimeMillis();
            if (ttl > 0){
                redisTemplate.opsForValue().set("blacklist:"+accessToken, accessToken, expirationAccessToken, TimeUnit.MILLISECONDS);
            }
        }catch (ExpiredJwtException e){

        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
        redisTemplate.delete("refreshToken:" + refreshToken);
    }
}
