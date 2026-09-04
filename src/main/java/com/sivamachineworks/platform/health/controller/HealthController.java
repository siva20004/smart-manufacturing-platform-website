package com.sivamachineworks.platform.health.controller;

import com.sivamachineworks.platform.health.dto.HealthResponse;
import com.sivamachineworks.platform.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<HealthResponse> getHealth() {
        return ApiResponse.success(new HealthResponse("UP", "1.0.0", Instant.now()));
    }
}
