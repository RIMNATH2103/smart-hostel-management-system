package com.smarthostel.hostelmanagement.service;

import com.smarthostel.hostelmanagement.entity.VacateRequest;
import com.smarthostel.hostelmanagement.repository.VacateRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VacateRequestService {

    private final VacateRequestRepository vacateRequestRepository;

    public VacateRequestService(
            VacateRequestRepository vacateRequestRepository) {
        this.vacateRequestRepository = vacateRequestRepository;
    }

    // Get all vacate requests
    public List<VacateRequest> getAllRequests() {
        return vacateRequestRepository.findAll();
    }

    // Find a vacate request by ID
    public Optional<VacateRequest> getRequestById(Long id) {
        return vacateRequestRepository.findById(id);
    }

    // Get requests submitted by a student
    public List<VacateRequest> getRequestsByStudent(Long studentId) {
        return vacateRequestRepository.findAll()
                .stream()
                .filter(request ->
                        request.getStudent().getId().equals(studentId))
                .toList();
    }

    // Get requests waiting for admin approval
    public List<VacateRequest> getPendingRequests() {
        return vacateRequestRepository.findAll()
                .stream()
                .filter(request ->
                        request.getStatus() != null
                                && request.getStatus().name().equals("PENDING"))
                .toList();
    }
 }

