
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
public class RoomAllocationController {

    private final RoomAllocationService allocationService;

    public RoomAllocationController(
            RoomAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    public record AllocationInfo(
            Long id,
            StudentInfo student,
            RoomInfo room,
            LocalDateTime allocatedAt,
            LocalDateTime vacatedAt
    ) {
    }

    public record StudentInfo(
            String studentId,
            String fullName
    ) {
    }

    public record RoomInfo(
            String roomNumber
    ) {
    }

    private AllocationInfo toAllocationInfo(RoomAllocation allocation) {
        return new AllocationInfo(
                allocation.getId(),
                new StudentInfo(
                        allocation.getStudent().getStudentId(),
                        allocation.getStudent().getFullName()
                ),
                new RoomInfo(
                        allocation.getRoom().getRoomNumber()
                ),
                allocation.getAllocatedAt(),
                allocation.getVacatedAt()
        );
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("adminId") instanceof Long;
    }

    @GetMapping
    public ResponseEntity<List<AllocationInfo>> getAllAllocations(
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        List<AllocationInfo> allocations =
                allocationService.getAllAllocations()
                        .stream()
                        .map(this::toAllocationInfo)
                        .toList();

        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationInfo> getAllocationById(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        return allocationService.getAllocationById(id)
                .map(this::toAllocationInfo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<AllocationInfo>> getActiveAllocations(
            @PathVariable Long roomId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        List<AllocationInfo> allocations =
                allocationService.getActiveAllocations(roomId)
                        .stream()
                        .map(this::toAllocationInfo)
                        .toList();

        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/room/{roomId}/occupied")
    public ResponseEntity<Integer> getOccupiedSlots(
            @PathVariable Long roomId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(
                allocationService.getOccupiedSlots(roomId)
        );
    }

    @GetMapping("/room/{roomId}/available")
    public ResponseEntity<Boolean> hasAvailableSlot(
            @PathVariable Long roomId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(
                allocationService.hasAvailableSlot(roomId)
        );
    }

    @PostMapping("/admin/auto-allocate")
    public ResponseEntity<Map<String, Object>> autoAllocate(
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Admin login required")
            );
        }

        int allocatedCount =
                allocationService.allocateStudentsAutomatically();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Automatic allocation completed",
                        "allocatedCount", allocatedCount
                )
        );
    }

    @PostMapping("/admin/vacate-student/{studentId}")
    public ResponseEntity<Map<String, String>> vacateStudent(
            @PathVariable Long studentId,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Admin login required")
            );
        }

        try {
            allocationService.vacateStudentByAdmin(studentId);

            return ResponseEntity.ok(
                    Map.of("message", "Student vacated successfully")
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

}
