
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Room;
import com.smarthostel.hostelmanagement.service.RoomAllocationService;
import com.smarthostel.hostelmanagement.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;
    private final RoomAllocationService allocationService;

    public AdminRoomController(
            RoomService roomService,
            RoomAllocationService allocationService) {
        this.roomService = roomService;
        this.allocationService = allocationService;
    }

    // Data displayed on the admin room management page
    public record RoomInfo(
            Long id,
            String roomNumber,
            int capacity,
            int occupied,
            int available,
            String status
    ) {
    }

    public record CreateRoomRequest(String roomNumber) {
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("adminId") instanceof Long;
    }

    private RoomInfo toRoomInfo(Room room) {
        int occupied = allocationService.getOccupiedSlots(room.getId());
        int capacity = Math.min(room.getCapacity(), 4);
        int available = Math.max(0, capacity - occupied);

        String status = available == 0 ? "Full" : "Available";

        return new RoomInfo(
                room.getId(),
                room.getRoomNumber(),
                capacity,
                occupied,
                available,
                status
        );
    }

    // View all rooms and their current occupancy
    @GetMapping
    public ResponseEntity<List<RoomInfo>> getAllRooms(
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).build();
        }

        List<RoomInfo> rooms = roomService.getAllRooms()
                .stream()
                .map(this::toRoomInfo)
                .toList();

        return ResponseEntity.ok(rooms);
    }

    // Add a new room
    @PostMapping
    public ResponseEntity<?> createRoom(
            @RequestBody CreateRoomRequest request,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Admin login required.")
            );
        }

        if (request == null
                || request.roomNumber() == null
                || request.roomNumber().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Room number is required.")
            );
        }

        try {
            Room room = new Room();
            room.setRoomNumber(request.roomNumber().trim());

            Room savedRoom = roomService.createRoom(room);

            return ResponseEntity.ok(toRoomInfo(savedRoom));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }
}
