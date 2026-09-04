package com.sivamachineworks.platform.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/roles")
public class RoleTestController {

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Admin Access Granted");
    }

    @GetMapping("/sales-only")
    @PreAuthorize("hasRole('SALES')")
    public ResponseEntity<String> salesOnly() {
        return ResponseEntity.ok("Sales Access Granted");
    }

    @GetMapping("/engineering-only")
    @PreAuthorize("hasRole('ENGINEERING')")
    public ResponseEntity<String> engineeringOnly() {
        return ResponseEntity.ok("Engineering Access Granted");
    }
}
