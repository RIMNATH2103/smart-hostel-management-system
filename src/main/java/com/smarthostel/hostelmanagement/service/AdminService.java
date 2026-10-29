
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all administrators
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // Find an administrator by ID
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    // Find an administrator by email
    public Optional<Admin> getAdminByEmail(String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return adminRepository.findByEmail(email.trim());
    }

    // Authenticate an administrator using email and password
    public Optional<Admin> authenticateAdmin(
            String email,
            String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<Admin> admin =
                getAdminByEmail(email);

        if (admin.isEmpty()) {
            return Optional.empty();
        }

        // Compare the entered password with the stored BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(
                password,
                admin.get().getPassword()
        );

        if (!passwordMatches) {
            return Optional.empty();
        }

        return admin;
    }
}
