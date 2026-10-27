
package com.smarthostel.hostelmanagement.controller;

import com.smarthostel.hostelmanagement.entity.VacateRequest;
import com.smarthostel.hostelmanagement.service.VacateRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacate-requests")
public class VacateRequestController {

    private final VacateRequestService vacateRequestService;

    public VacateRequestController(
            VacateRequestService vacateRequestService) {
        this.vacateRequestService = vacateRequestService;
    }

    // Get all vacate requests
    @GetMapping
    public List<VacateRequest> getAllRequests() {
        return vacateRequestService.getAllRequests();
    }

    // Get a vacate request by ID
    @GetMapping("/{id}")
    public ResponseEntity<VacateRequest> getRequestById(
            @PathVariable Long id) {

        return vacateRequestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get requests submitted by a student
    @GetMapping("/student/{studentId}")
    public List<VacateRequest> getRequestsByStudent(
            @PathVariable Long studentId) {

        return vacateRequestService
                .getRequestsByStudent(studentId);
    }

    // Get requests waiting for admin approval
    @GetMapping("/pending")
    public List<VacateRequest> getPendingRequests() {
        return vacateRequestService.getPendingRequests();
    }
}
