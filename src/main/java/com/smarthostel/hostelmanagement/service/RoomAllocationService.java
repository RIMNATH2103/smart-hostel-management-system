
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.Room;
import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.entity.RoommatePreference;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.repository.RoomAllocationRepository;
import com.smarthostel.hostelmanagement.repository.RoommatePreferenceRepository;
import com.smarthostel.hostelmanagement.repository.RoomRepository;
import com.smarthostel.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoomAllocationService {

    private static final int MAX_ROOM_CAPACITY = 4;

    private final RoomAllocationRepository allocationRepository;
    private final RoommatePreferenceRepository preferenceRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;

    public RoomAllocationService(
            RoomAllocationRepository allocationRepository,
            RoommatePreferenceRepository preferenceRepository,
            StudentRepository studentRepository,
            RoomRepository roomRepository) {

        this.allocationRepository = allocationRepository;
        this.preferenceRepository = preferenceRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
    }

    // Include both active and previous allocations
    public List<RoomAllocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public Optional<RoomAllocation> getAllocationById(Long id) {
        return allocationRepository.findById(id);
    }

    public Optional<RoomAllocation> getCurrentAllocation(Long studentId) {
        return allocationRepository
                .findByStudent_IdAndVacatedAtIsNull(studentId);
    }

    public List<RoomAllocation> getActiveAllocations(Long roomId) {
        return allocationRepository.findByRoom_IdAndVacatedAtIsNull(roomId);
    }

    public int getOccupiedSlots(Long roomId) {
        return Math.toIntExact(
                allocationRepository.countByRoom_IdAndVacatedAtIsNull(roomId)
        );
    }

    public boolean hasAvailableSlot(Long roomId) {
        return getOccupiedSlots(roomId) < MAX_ROOM_CAPACITY;
    }

    // Run only from the admin-controlled allocation endpoint
    @Transactional
    public int allocateStudentsAutomatically() {

        // Load all existing allocations once
        List<RoomAllocation> existingAllocations =
                allocationRepository.findAll();

        Set<Long> allocatedStudentIds = new HashSet<>();
        Map<Long, List<Long>> roomMembers = new HashMap<>();

        for (RoomAllocation allocation : existingAllocations) {
            if (allocation.getVacatedAt() == null) {

                allocatedStudentIds.add(
                        allocation.getStudent().getId()
                );

                roomMembers
                        .computeIfAbsent(
                                allocation.getRoom().getId(),
                                id -> new ArrayList<>()
                        )
                        .add(allocation.getStudent().getId());
            }
        }

        // Find students who do not currently have a room
        Map<Long, Student> waitingStudents = new LinkedHashMap<>();

        for (Student student : studentRepository.findAll()) {
            if (!allocatedStudentIds.contains(student.getId())) {
                waitingStudents.put(student.getId(), student);
            }
        }

        if (waitingStudents.isEmpty()) {
            return 0;
        }

        // Store all roommate preferences for faster matching
        Map<Long, Set<Long>> preferences = new HashMap<>();

        for (RoommatePreference preference : preferenceRepository.findAll()) {
            Long studentId = preference.getStudent().getId();
            Long preferredId = preference.getPreferredStudent().getId();

            preferences
                    .computeIfAbsent(studentId, id -> new HashSet<>())
                    .add(preferredId);
        }

        List<Room> rooms = new ArrayList<>(roomRepository.findAll());
        Collections.shuffle(rooms);

        int totalAllocated = 0;

        for (Room room : rooms) {

            Long roomId = room.getId();

            List<Long> members = roomMembers.computeIfAbsent(
                    roomId,
                    id -> new ArrayList<>()
            );

            // Never exceed either the room's configured capacity or 4
            int roomLimit = Math.min(
                    room.getCapacity(),
                    MAX_ROOM_CAPACITY
            );

            while (members.size() < roomLimit && !waitingStudents.isEmpty()) {

                Student selectedStudent = chooseNextStudent(
                        members,
                        waitingStudents,
                        preferences
                );

                if (selectedStudent == null) {
                    break;
                }

                // Remove immediately to prevent duplicate allocation
                waitingStudents.remove(selectedStudent.getId());

                RoomAllocation allocation = new RoomAllocation();
                allocation.setStudent(selectedStudent);
                allocation.setRoom(room);

                allocationRepository.save(allocation);

                members.add(selectedStudent.getId());
                totalAllocated++;
            }
        }

        return totalAllocated;
    }

    // Choose a student based on existing roommates' preferences
    private Student chooseNextStudent(
            List<Long> currentMembers,
            Map<Long, Student> waitingStudents,
            Map<Long, Set<Long>> preferences) {

        List<Student> candidates =
                new ArrayList<>(waitingStudents.values());

        if (candidates.isEmpty()) {
            return null;
        }

        Collections.shuffle(candidates);

        // Empty rooms start with a random student
        if (currentMembers.isEmpty()) {
            return candidates.get(0);
        }

        Student bestCandidate = null;
        int highestScore = 0;

        for (Student candidate : candidates) {

            int score = 0;
            Long candidateId = candidate.getId();

            for (Long memberId : currentMembers) {

                boolean memberSelectedCandidate =
                        preferences
                                .getOrDefault(memberId, Collections.emptySet())
                                .contains(candidateId);

                boolean candidateSelectedMember =
                        preferences
                                .getOrDefault(candidateId, Collections.emptySet())
                                .contains(memberId);

                // Mutual preference gets the highest priority
                if (memberSelectedCandidate && candidateSelectedMember) {
                    score += 3;
                } else if (memberSelectedCandidate
                        || candidateSelectedMember) {
                    score += 1;
                }
            }

            if (score > highestScore) {
                highestScore = score;
                bestCandidate = candidate;
            }
        }

        // If no preferences match, fill the space randomly
        return bestCandidate != null
                ? bestCandidate
                : candidates.get(0);
    }
}
