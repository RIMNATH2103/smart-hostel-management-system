
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
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
        return adminRepository.findAll()
                .stream()
                .filter(admin -> admin.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
