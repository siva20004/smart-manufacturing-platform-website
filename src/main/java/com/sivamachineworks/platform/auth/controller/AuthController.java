package com.sivamachineworks.platform.auth.controller;

import com.sivamachineworks.platform.auth.dto.CurrentUserResponse;
import com.sivamachineworks.platform.auth.dto.LoginRequest;
import com.sivamachineworks.platform.auth.dto.LoginResponse;
import com.sivamachineworks.platform.auth.service.AuthService;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        CurrentUserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
