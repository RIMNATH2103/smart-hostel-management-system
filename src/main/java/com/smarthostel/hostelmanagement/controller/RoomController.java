
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.Room;
import com.smarthostel.hostelmanagement.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Get all rooms
    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    // Get a room by ID
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(
            @PathVariable Long id) {

        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new room
    @PostMapping
    public ResponseEntity<?> createRoom(
            @RequestBody Room room) {

        try {
            Room savedRoom = roomService.createRoom(room);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedRoom);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // Get the maximum room capacity
    @GetMapping("/capacity")
    public int getRoomCapacity() {
        return roomService.getRoomCapacity();
    }
}
