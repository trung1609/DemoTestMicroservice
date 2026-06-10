package com.example.identityservice.config;

import com.example.identityservice.entity.Users;
import com.example.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceCustom implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );
        return new UserPrincipal(users);
    }
}
