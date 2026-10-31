
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
import java.time.LocalDateTime;

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

    @Transactional
    public int allocateStudentsAutomatically() {

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

        Map<Long, Student> waitingStudents = new LinkedHashMap<>();

        for (Student student : studentRepository.findAll()) {
            if (!allocatedStudentIds.contains(student.getId())) {
                waitingStudents.put(student.getId(), student);
            }
        }

        if (waitingStudents.isEmpty()) {
            return 0;
        }

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

        rooms.sort(Comparator.comparingInt(
                (Room room) -> roomMembers
                        .getOrDefault(room.getId(), Collections.emptyList())
                        .size()
        ).reversed());


        int totalAllocated = 0;

        for (Room room : rooms) {

            Long roomId = room.getId();

            List<Long> members = roomMembers.computeIfAbsent(
                    roomId,
                    id -> new ArrayList<>()
            );

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


        Student bestCandidate = null;
        int highestScore = -1;

        if (currentMembers.isEmpty()) {

            for (Student candidate : candidates) {

                int totalScore = 0;

                for (Student otherStudent : candidates) {

                    if (candidate.getId().equals(otherStudent.getId())) {
                        continue;
                    }

                    totalScore += calculatePreferenceScore(
                            candidate.getId(),
                            otherStudent.getId(),
                            preferences
                    );
                }

                if (totalScore > highestScore) {
                    highestScore = totalScore;
                    bestCandidate = candidate;
                }
            }

            return bestCandidate;
        }


        for (Student candidate : candidates) {

            int totalScore = 0;

            for (Long memberId : currentMembers) {
                totalScore += calculatePreferenceScore(
                        candidate.getId(),
                        memberId,
                        preferences
                );
            }

            if (totalScore > highestScore) {
                highestScore = totalScore;
                bestCandidate = candidate;
            }
        }

        return bestCandidate;
    }

    @Transactional
    public void vacateStudentByAdmin(Long studentId) {

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }

        RoomAllocation allocation = allocationRepository
                .findByStudent_IdAndVacatedAtIsNull(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "This student does not have an active room allocation."
                ));

        allocation.setVacatedAt(LocalDateTime.now());

        allocationRepository.save(allocation);
    }

    private int calculatePreferenceScore(
            Long firstStudentId,
            Long secondStudentId,
            Map<Long, Set<Long>> preferences) {

        boolean firstSelectedSecond = preferences
                .getOrDefault(firstStudentId, Collections.emptySet())
                .contains(secondStudentId);

        boolean secondSelectedFirst = preferences
                .getOrDefault(secondStudentId, Collections.emptySet())
                .contains(firstStudentId);

        if (firstSelectedSecond && secondSelectedFirst) {
            return 3;
        }

        if (firstSelectedSecond || secondSelectedFirst) {
            return 1;
        }

        return 0;
    }


}
