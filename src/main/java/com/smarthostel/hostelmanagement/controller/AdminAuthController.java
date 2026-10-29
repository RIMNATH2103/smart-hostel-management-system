
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin-auth")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Admin login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        Optional<Admin> admin = adminService.authenticateAdmin(
                loginRequest.email(),
                loginRequest.password()
        );

        if (admin.isEmpty()) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        // Invalidate any previous session
        HttpSession existingSession = request.getSession(false);

        if (existingSession != null) {
            existingSession.invalidate();
        }

        // Create a new session for the administrator
        HttpSession session = request.getSession(true);
        session.setAttribute("adminId", admin.get().getId());

        return ResponseEntity.ok(
                Map.of("message", "Admin login successful.")
        );
    }

    // Admin logout
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of("message", "Admin logged out successfully.")
        );
    }

    // Login form data
    public record LoginRequest(String email, String password) {
    }
}
