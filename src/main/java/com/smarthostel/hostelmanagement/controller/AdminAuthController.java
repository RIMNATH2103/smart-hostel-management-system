
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
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

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        Optional<Admin> admin = adminService.authenticateAdmin(
                loginRequest.email(),
                loginRequest.password()
        );

        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        HttpSession existingSession = request.getSession(false);

        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("adminId", admin.get().getId());

        return ResponseEntity.ok(
                Map.of("message", "Admin login successful.")
        );
    }

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

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody AdminRegistrationRequest registration,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("adminId") instanceof Long adminId)
                || adminService.getAdminById(adminId).isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Administrator login required.")
            );
        }

        try {
            Admin newAdmin = new Admin();
            newAdmin.setFullName(registration.fullName());
            newAdmin.setEmail(registration.email());
            newAdmin.setPhoneNumber(registration.phoneNumber());
            newAdmin.setPassword(registration.password());

            adminService.registerAdmin(newAdmin);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("message", "Administrator registered successfully.")
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

    public record LoginRequest(String email, String password) {
    }

    public record AdminRegistrationRequest(
            String fullName,
            String email,
            String phoneNumber,
            String password
    ) {
    }
}
