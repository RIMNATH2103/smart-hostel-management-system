
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Find an admin using their email address during login
    Optional<Admin> findByEmail(String email);
}
