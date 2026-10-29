
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.VacateRequest;
import com.smarthostel.hostelmanagement.service.VacateRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacate-requests")
public class VacateRequestController {

    private final VacateRequestService vacateRequestService;

    public VacateRequestController(
            VacateRequestService vacateRequestService) {
        this.vacateRequestService = vacateRequestService;
    }

    // Student: view their own requests
    @GetMapping("/my")
    public ResponseEntity<?> getMyRequests(HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        List<VacateRequestInfo> requests =
                vacateRequestService.getRequestsByStudent((Long) studentId)
                        .stream()
                        .map(request -> new VacateRequestInfo(
                                request.getId(),
                                request.getReason(),
                                request.getStatus().name(),
                                request.getRequestedAt() == null
                                        ? null
                                        : request.getRequestedAt().toString(),
                                request.getAdminRemarks()
                        ))
                        .toList();

        return ResponseEntity.ok(requests);
    }

    // Student: submit a new vacate request
    @PostMapping("/my")
    public ResponseEntity<?> submitMyRequest(
            @RequestBody VacateRequestInput input,
            HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        if (!(studentId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        try {
            vacateRequestService.submitVacateRequest(
                    (Long) studentId,
                    input.reason()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Vacate request submitted successfully."
                    ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    // Admin: view pending requests
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingRequests(HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return adminLoginRequired();
        }

        List<AdminVacateRequestInfo> pendingRequests =
                vacateRequestService.getPendingRequests()
                        .stream()
                        .map(request -> new AdminVacateRequestInfo(
                                request.getId(),
                                request.getStudent().getId(),
                                request.getStudent().getFullName(),
                                request.getStudent().getStudentId(),
                                request.getRoomAllocation()
                                        .getRoom().getRoomNumber(),
                                request.getReason(),
                                request.getStatus().name(),
                                request.getRequestedAt() == null
                                        ? null
                                        : request.getRequestedAt().toString()
                        ))
                        .toList();

        return ResponseEntity.ok(pendingRequests);
    }

    // Admin: approve a request and vacate the student
    @PostMapping("/admin/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long requestId,
            @RequestBody ReviewInput input,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return adminLoginRequired();
        }

        try {
            vacateRequestService.approveRequest(
                    requestId,
                    input.remarks()
            );

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Vacate request approved. Student vacated successfully."
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    // Admin: reject a request without vacating the student
    @PostMapping("/admin/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody ReviewInput input,
            HttpSession session) {

        if (!isAdminLoggedIn(session)) {
            return adminLoginRequired();
        }

        try {
            vacateRequestService.rejectRequest(
                    requestId,
                    input.remarks()
            );

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Vacate request rejected successfully."
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("adminId") instanceof Long;
    }

    private ResponseEntity<?> adminLoginRequired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message",
                        "Administrator login required."
                ));
    }

    // Student request input
    public record VacateRequestInput(String reason) {
    }

    // Admin review input
    public record ReviewInput(String remarks) {
    }

    // Student response
    public record VacateRequestInfo(
            Long id,
            String reason,
            String status,
            String requestedAt,
            String adminRemarks
    ) {
    }

    // Admin response
    public record AdminVacateRequestInfo(
            Long id,
            Long studentId,
            String studentName,
            String studentRegistrationNumber,
            String roomNumber,
            String reason,
            String status,
            String requestedAt
    ) {
    }
}
