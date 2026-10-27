
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.repository.RoomAllocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomAllocationService {

    private final RoomAllocationRepository allocationRepository;

    public RoomAllocationService(
            RoomAllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    // Get all room allocations, including past allocations
    public List<RoomAllocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    // Find an allocation by its ID
    public Optional<RoomAllocation> getAllocationById(Long id) {
        return allocationRepository.findById(id);
    }

    // Get active allocations for a room
    public List<RoomAllocation> getActiveAllocations(Long roomId) {
        return allocationRepository.findAll()
                .stream()
                .filter(allocation ->
                        allocation.getRoom().getId().equals(roomId)
                                && allocation.getVacatedAt() == null)
                .toList();
    }

    // Count students currently occupying a room
    public int getOccupiedSlots(Long roomId) {
        return getActiveAllocations(roomId).size();
    }

    // Check whether a room has an available slot
    public boolean hasAvailableSlot(Long roomId) {
        return getOccupiedSlots(roomId) < 4;
    }
}
