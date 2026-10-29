
package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.RoomAllocation;
import com.smarthostel.hostelmanagement.entity.Student;
import com.smarthostel.hostelmanagement.entity.VacateRequest;
import com.smarthostel.hostelmanagement.repository.VacateRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VacateRequestService {

    private final VacateRequestRepository vacateRequestRepository;
    private final StudentService studentService;
    private final RoomAllocationService roomAllocationService;

    public VacateRequestService(
            VacateRequestRepository vacateRequestRepository,
            StudentService studentService,
            RoomAllocationService roomAllocationService) {

        this.vacateRequestRepository = vacateRequestRepository;
        this.studentService = studentService;
        this.roomAllocationService = roomAllocationService;
    }

    public List<VacateRequest> getAllRequests() {
        return vacateRequestRepository.findAll();
    }

    public Optional<VacateRequest> getRequestById(Long id) {
        return vacateRequestRepository.findById(id);
    }

    public List<VacateRequest> getRequestsByStudent(Long studentId) {
        return vacateRequestRepository
                .findByStudent_IdOrderByRequestedAtDesc(studentId);
    }

    public List<VacateRequest> getPendingRequests() {
        return vacateRequestRepository.findAll()
                .stream()
                .filter(request ->
                        request.getStatus()
                                == VacateRequest.RequestStatus.PENDING)
                .toList();
    }

    // Student submits a request, but cannot vacate themselves
    @Transactional
    public VacateRequest submitVacateRequest(
            Long studentId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Please provide a reason for vacating."
            );
        }

        String cleanReason = reason.trim();

        if (cleanReason.length() > 1000) {
            throw new IllegalArgumentException(
                    "Reason cannot exceed 1000 characters."
            );
        }

        Student student = studentService.getStudentById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found."
                ));

        RoomAllocation allocation =
                roomAllocationService.getCurrentAllocation(studentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "You do not have an assigned room."
                        ));

        boolean hasPendingRequest =
                vacateRequestRepository.existsByStudent_IdAndStatus(
                        studentId,
                        VacateRequest.RequestStatus.PENDING
                );

        if (hasPendingRequest) {
            throw new IllegalArgumentException(
                    "You already have a pending vacate request."
            );
        }

        VacateRequest request = new VacateRequest();

        request.setStudent(student);
        request.setRoomAllocation(allocation);
        request.setReason(cleanReason);
        request.setStatus(VacateRequest.RequestStatus.PENDING);

        return vacateRequestRepository.save(request);
    }

    // Administrator approves a request and vacates the student
    @Transactional
    public VacateRequest approveRequest(Long requestId, String remarks) {

        VacateRequest request = vacateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vacate request not found."
                ));

        if (request.getStatus() != VacateRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This request has already been reviewed."
            );
        }

        RoomAllocation allocation = request.getRoomAllocation();

        if (allocation == null || allocation.getVacatedAt() != null) {
            throw new IllegalArgumentException(
                    "The student's room allocation is no longer active."
            );
        }

        // Keep the allocation record for room occupancy history
        allocation.setVacatedAt(LocalDateTime.now());

        request.setStatus(VacateRequest.RequestStatus.APPROVED);
        request.setAdminRemarks(cleanRemarks(remarks));
        request.setReviewedAt(LocalDateTime.now());

        // Transaction saves changes to the managed entities
        return vacateRequestRepository.save(request);
    }

    // Administrator rejects a request without changing room allocation
    @Transactional
    public VacateRequest rejectRequest(Long requestId, String remarks) {

        VacateRequest request = vacateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vacate request not found."
                ));

        if (request.getStatus() != VacateRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This request has already been reviewed."
            );
        }

        request.setStatus(VacateRequest.RequestStatus.REJECTED);
        request.setAdminRemarks(cleanRemarks(remarks));
        request.setReviewedAt(LocalDateTime.now());

        return vacateRequestRepository.save(request);
    }

    // Clean optional administrator remarks
    private String cleanRemarks(String remarks) {

        if (remarks == null || remarks.isBlank()) {
            return null;
        }

        String cleaned = remarks.trim();

        if (cleaned.length() > 1000) {
            throw new IllegalArgumentException(
                    "Admin remarks cannot exceed 1000 characters."
            );
        }

        return cleaned;
    }
}
