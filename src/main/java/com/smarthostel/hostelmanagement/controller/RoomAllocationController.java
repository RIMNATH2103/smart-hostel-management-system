
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
public class RoomAllocationController {

    private final RoomAllocationService allocationService;

    public RoomAllocationController(
            RoomAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    // Get all room allocations
    @GetMapping
    public List<RoomAllocation> getAllAllocations() {
        return allocationService.getAllAllocations();
    }

    // Get a room allocation by ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomAllocation> getAllocationById(
            @PathVariable Long id) {

        return allocationService.getAllocationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get active allocations for a room
    @GetMapping("/room/{roomId}")
    public List<RoomAllocation> getActiveAllocations(
            @PathVariable Long roomId) {

        return allocationService.getActiveAllocations(roomId);
    }

    // Get the number of occupied slots in a room
    @GetMapping("/room/{roomId}/occupied")
    public int getOccupiedSlots(
            @PathVariable Long roomId) {

        return allocationService.getOccupiedSlots(roomId);
    }

    // Check whether a room has available space
    @GetMapping("/room/{roomId}/available")
    public boolean hasAvailableSlot(
            @PathVariable Long roomId) {

        return allocationService.hasAvailableSlot(roomId);
    }
}
