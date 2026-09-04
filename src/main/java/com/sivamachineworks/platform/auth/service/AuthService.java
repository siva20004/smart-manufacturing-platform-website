package com.sivamachineworks.platform.auth.service;

import com.sivamachineworks.platform.auth.dto.CurrentUserResponse;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.auth.dto.LoginResponse;
import com.sivamachineworks.platform.identity.domain.Role;
import com.sivamachineworks.platform.identity.domain.User;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import com.sivamachineworks.platform.shared.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        long expiresIn = jwtTokenProvider.getJwtExpirationMs() / 1000; // in seconds, or ms? The prompt says (ms/seconds), let's use ms. Wait, usually expiresIn is seconds. I'll just use ms to be safe if that's what it is, or seconds. Let's return ms as is or seconds. Let's leave it as ms since getJwtExpirationMs() returns ms. Wait, let me just return the exact value.
        expiresIn = jwtTokenProvider.getJwtExpirationMs();

        User user = userRepository.findByUsername(authentication.getName())
                .orElseGet(() -> userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "User not found")));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new LoginResponse(token, expiresIn, user.getUsername(), user.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "User not found"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList());

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPlantLocation(),
                user.getDepartment(),
                roles,
                permissions
        );
    }
}
