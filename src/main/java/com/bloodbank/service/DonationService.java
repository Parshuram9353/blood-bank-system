package com.bloodbank.service;

import com.bloodbank.entity.BloodDonation;
import com.bloodbank.entity.Donor;
import com.bloodbank.repository.BloodDonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final BloodDonationRepository donationRepository;
    private final DonorService donorService;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    public Page<BloodDonation> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("donationDate").descending());
        return donationRepository.findAll(pageable);
    }

    public Page<BloodDonation> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("donationDate").descending());
        if (query == null || query.trim().isEmpty()) {
            return donationRepository.findAll(pageable);
        }
        return donationRepository.searchDonations(query, pageable);
    }

    public Page<BloodDonation> findByDonor(Donor donor, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("donationDate").descending());
        return donationRepository.findByDonor(donor, pageable);
    }

    public List<BloodDonation> findByDonor(Donor donor) {
        return donationRepository.findByDonor(donor);
    }

    public Optional<BloodDonation> findById(Long id) {
        return donationRepository.findById(id);
    }

    @Transactional
    public BloodDonation registerDonation(BloodDonation donation) {
        // Validate donor eligibility
        Donor donor = donorService.findById(donation.getDonor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        if (!donorService.isEligibleForDonation(donor) && "PENDING".equals(donation.getStatus())) {
            log.warn("Donor {} is currently marked ineligible, register attempt continues as PENDING", donor.getFullName());
        }

        donation.setBloodGroup(donor.getBloodGroup());
        donation.setEligibilityChecked(true);

        BloodDonation saved = donationRepository.save(donation);
        auditService.log("DONATION_REGISTERED", String.format("Registered donation appointment for donor: %s, Status: %s", donor.getFullName(), donation.getStatus()));

        return saved;
    }

    @Transactional
    public BloodDonation completeDonation(Long donationId, String status, String remarks) {
        BloodDonation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation record not found"));

        if (!"PENDING".equals(donation.getStatus())) {
            throw new IllegalStateException("Only pending donations can be finalized.");
        }

        donation.setStatus(status);
        donation.setRemarks(remarks);

        Donor donor = donation.getDonor();

        if ("COMPLETED".equals(status)) {
            // Update donor history
            donor.setLastDonationDate(donation.getDonationDate());
            donorService.save(donor); // This automatically recalculates and sets status to DEFERRED

            // Add to Blood Inventory
            inventoryService.addBloodUnits(donation.getBloodGroup(), donation.getUnits(), donation.getDonationDate(), donor);
            
            auditService.log("DONATION_COMPLETED", String.format("Completed blood donation for: %s. Added %d units of %s.", donor.getFullName(), donation.getUnits(), donation.getBloodGroup()));
        } else if ("REJECTED".equals(status)) {
            // Mark donor as deferred/ineligible for now if needed, or leave it
            auditService.log("DONATION_REJECTED", String.format("Rejected blood donation for: %s. Reason: %s", donor.getFullName(), remarks));
        }

        return donationRepository.save(donation);
    }

    public long countTotalDonations() {
        return donationRepository.count();
    }

    public List<BloodDonation> findRecentDonations(int limit) {
        return donationRepository.findRecentDonations(PageRequest.of(0, limit));
    }
}
