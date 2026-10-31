
package com.smarthostel.hostelmanagement.repository;

import com.smarthostel.hostelmanagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomNumberIgnoreCase(String roomNumber);
}
