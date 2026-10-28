
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {

    // Check whether the student has any allocation history
    boolean existsByStudent_Id(Long studentId);

    // Find the student's current room allocation
    Optional<RoomAllocation> findByStudent_IdAndVacatedAtIsNull(Long studentId);
}
