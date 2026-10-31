
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Admin;
import com.smarthostel.hostelmanagement.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(
            @PathVariable Long id) {

        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<Admin> getAdminByEmail(
            @RequestParam String email) {

        return adminService.getAdminByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
