
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {

    // Check whether a student has any previous allocation
    boolean existsByStudent_Id(Long studentId);

    // Find the student's current room
    Optional<RoomAllocation> findByStudent_IdAndVacatedAtIsNull(Long studentId);

    // Find all students currently staying in a room
    List<RoomAllocation> findByRoom_IdAndVacatedAtIsNull(Long roomId);

    // Count occupied spaces in a room
    long countByRoom_IdAndVacatedAtIsNull(Long roomId);

    // Check whether a student already has an active room
    boolean existsByStudent_IdAndVacatedAtIsNull(Long studentId);
}
