package com.bloodbank.service;

import com.bloodbank.entity.BloodRequest;
import com.bloodbank.entity.User;
import com.bloodbank.repository.BloodRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final BloodRequestRepository requestRepository;
    private final InventoryService inventoryService;
    private final UserService userService;
    private final EmailService emailService;
    private final AuditService auditService;

    public Page<BloodRequest> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return requestRepository.findAll(pageable);
    }

    public Page<BloodRequest> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (query == null || query.trim().isEmpty()) {
            return requestRepository.findAll(pageable);
        }
        return requestRepository.searchRequests(query, pageable);
    }

    public Optional<BloodRequest> findById(Long id) {
        return requestRepository.findById(id);
    }

    @Transactional
    public BloodRequest createRequest(BloodRequest request) {
        request.setStatus("PENDING");
        BloodRequest saved = requestRepository.save(request);
        auditService.log("REQUEST_CREATED", String.format("Blood request created for %s, Group: %s, Units: %d", 
                request.getPatientName(), request.getBloodGroup(), request.getUnitsRequired()));
        
        // Notify staff via email
        emailService.sendEmail("staff@bloodbank.org", "New Blood Request Created", 
                String.format("A new %s request has been created for patient %s. Blood Group required: %s (%d units). Please review the request.",
                        request.getUrgency(), request.getPatientName(), request.getBloodGroup(), request.getUnitsRequired()));

        return saved;
    }

    @Transactional
    public BloodRequest approveAndFulfill(Long requestId) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Blood request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }

        // Check and use units from inventory
        boolean success = inventoryService.useBloodUnits(request.getBloodGroup(), request.getUnitsRequired());
        if (!success) {
            throw new IllegalStateException("Insufficient stock of blood group " + request.getBloodGroup() + " to fulfill this request.");
        }

        User handler = getCurrentUser();
        request.setHandledBy(handler);
        request.setStatus("FULFILLED");
        BloodRequest saved = requestRepository.save(request);

        auditService.log("REQUEST_FULFILLED", String.format("Approved and fulfilled request ID #%d for patient %s", 
                request.getId(), request.getPatientName()));

        // Send email notification to hospital or contact
        if (request.getHospital() != null && request.getHospital().getEmail() != null) {
            emailService.sendRequestStatusAlert(request.getHospital().getEmail(), request.getPatientName(), request.getBloodGroup(), "FULFILLED");
        }

        return saved;
    }

    @Transactional
    public BloodRequest rejectRequest(Long requestId, String notes) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Blood request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }

        User handler = getCurrentUser();
        request.setHandledBy(handler);
        request.setStatus("REJECTED");
        request.setNotes(notes);
        
        BloodRequest saved = requestRepository.save(request);
        auditService.log("REQUEST_REJECTED", String.format("Rejected request ID #%d for patient %s. Reason: %s", 
                request.getId(), request.getPatientName(), notes));

        // Send email notification to hospital or contact
        if (request.getHospital() != null && request.getHospital().getEmail() != null) {
            emailService.sendRequestStatusAlert(request.getHospital().getEmail(), request.getPatientName(), request.getBloodGroup(), "REJECTED (Reason: " + notes + ")");
        }

        return saved;
    }

    public long countPendingRequests() {
        return requestRepository.countByStatus("PENDING");
    }

    public long countTotalRequests() {
        return requestRepository.count();
    }

    public List<BloodRequest> findRecentRequests(int limit) {
        return requestRepository.findRecentRequests(PageRequest.of(0, limit));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username).orElse(null);
    }
}
