
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}

