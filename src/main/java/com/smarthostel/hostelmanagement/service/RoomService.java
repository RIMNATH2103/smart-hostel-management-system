
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Room;
import com.smarthostel.hostelmanagement.repository.RoomRepository;
import org.springframework.dao.DataIntegrityViolationException;
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

        String roomNumber = room.getRoomNumber();

        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Room number is required."
            );
        }

        // Remove unnecessary spaces
        roomNumber = roomNumber.trim();

        // Prevent duplicate room numbers
        if (roomRepository.existsByRoomNumberIgnoreCase(roomNumber)) {
            throw new IllegalArgumentException(
                    "A room with this number already exists."
            );
        }

        // Save the cleaned room number
        room.setRoomNumber(roomNumber);

        try {
            return roomRepository.saveAndFlush(room);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "Unable to create room. The room number may already exist.",
                    exception
            );
        }
    }

    // Get the maximum capacity of a room
    public int getRoomCapacity() {
        return 4;
    }
}
