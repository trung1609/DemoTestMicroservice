package com.example.productservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String accessToken = authHeader.substring(7);

        String role = request.getHeader("X-Roles");
        String username = request.getHeader("X-Username");
        String permissions = request.getHeader("X-Permissions");

        if (role != null && username != null && permissions != null){
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (!role.trim().isEmpty()){
                authorities.add(new SimpleGrantedAuthority(role.toUpperCase()));
            }

            System.out.println("ROLE : " + role);

            if (!permissions.trim().isEmpty()){
                List<String> permissionList = objectMapper.readValue(permissions, new TypeReference<List<String>>() {});
                for (String permission : permissionList){
                    authorities.add(new SimpleGrantedAuthority(permission.toUpperCase()));
                }
            }
            System.out.println("PERMISSIONS : " + permissions);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username, null, authorities
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
