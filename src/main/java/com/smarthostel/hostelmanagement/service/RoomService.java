
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Room;
import com.smarthostel.hostelmanagement.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // Get all hostel rooms
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Find a room by its database ID
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    // Create a new hostel room
    public Room createRoom(Room room) {

        if (room == null) {
            throw new IllegalArgumentException(
                    "Room information is required."
            );
        }

        if (room.getRoomNumber() == null
                || room.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Room number is required."
            );
        }

        return roomRepository.save(room);
    }

    // Get the maximum capacity of a room
    public int getRoomCapacity() {
        return 4;
    }
}
