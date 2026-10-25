
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {
}
