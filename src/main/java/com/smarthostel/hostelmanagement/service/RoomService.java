
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

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

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

        roomNumber = roomNumber.trim();

        if (roomRepository.existsByRoomNumberIgnoreCase(roomNumber)) {
            throw new IllegalArgumentException(
                    "A room with this number already exists."
            );
        }

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

    public int getRoomCapacity() {
        return 4;
    }
}
