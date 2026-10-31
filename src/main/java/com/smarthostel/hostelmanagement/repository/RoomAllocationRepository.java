
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {

    boolean existsByStudent_Id(Long studentId);

    Optional<RoomAllocation> findByStudent_IdAndVacatedAtIsNull(Long studentId);

    List<RoomAllocation> findByRoom_IdAndVacatedAtIsNull(Long roomId);

    long countByRoom_IdAndVacatedAtIsNull(Long roomId);

    boolean existsByStudent_IdAndVacatedAtIsNull(Long studentId);
}
