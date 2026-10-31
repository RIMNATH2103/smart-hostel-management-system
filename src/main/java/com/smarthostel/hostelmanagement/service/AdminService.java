
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

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> getAdminByEmail(String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return adminRepository.findByEmail(email.trim());
    }

    public Optional<Admin> authenticateAdmin(
            String email,
            String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<Admin> admin = getAdminByEmail(email);

        if (admin.isEmpty()) {
            return Optional.empty();
        }

        boolean passwordMatches = passwordEncoder.matches(
                password,
                admin.get().getPassword()
        );

        if (!passwordMatches) {
            return Optional.empty();
        }

        return admin;
    }

    public Admin registerAdmin(Admin admin) {

        if (admin == null) {
            throw new IllegalArgumentException(
                    "Administrator details are required."
            );
        }

        if (admin.getFullName() == null
                || admin.getFullName().isBlank()) {
            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (admin.getEmail() == null
                || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Email address is required."
            );
        }

        String email = admin.getEmail().trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        if (adminRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "This email address is already registered."
            );
        }

        if (admin.getPassword() == null
                || admin.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        String phoneNumber = admin.getPhoneNumber();

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            phoneNumber = phoneNumber.trim();

            if (!phoneNumber.matches("\\+?[0-9]{10,15}")) {
                throw new IllegalArgumentException(
                        "Phone number must contain 10 to 15 digits."
                );
            }
        }

        Admin newAdmin = new Admin();
        newAdmin.setFullName(admin.getFullName().trim());
        newAdmin.setEmail(email);
        newAdmin.setPhoneNumber(phoneNumber);

        newAdmin.setPassword(
                passwordEncoder.encode(admin.getPassword())
        );

        return adminRepository.save(newAdmin);
    }
}
