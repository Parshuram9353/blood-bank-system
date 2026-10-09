package com.bloodbank.service;

import com.bloodbank.entity.Donor;
import com.bloodbank.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
    private final AuditService auditService;

    public Page<Donor> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return donorRepository.findAll(pageable);
    }

    public List<Donor> findAllList() {
        return donorRepository.findAll();
    }

    public Page<Donor> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        if (query == null || query.trim().isEmpty()) {
            return donorRepository.findAll(pageable);
        }
        return donorRepository.searchDonors(query, pageable);
    }

    public List<Donor> searchList(String query) {
        if (query == null || query.trim().isEmpty()) {
            return donorRepository.findAll();
        }
        return donorRepository.searchDonorsList(query);
    }

    public Optional<Donor> findById(Long id) {
        return donorRepository.findById(id);
    }

    public Optional<Donor> findByUser(com.bloodbank.entity.User user) {
        return donorRepository.findByUser(user);
    }

    @Transactional
    public Donor save(Donor donor) {
        boolean isNew = donor.getId() == null;
        
        // Calculate/re-evaluate eligibility based on fields
        updateEligibilityStatus(donor);

        Donor savedDonor = donorRepository.save(donor);

        if (isNew) {
            auditService.log("DONOR_CREATED", "Added new donor: " + donor.getFullName());
        } else {
            auditService.log("DONOR_UPDATED", "Updated donor details: " + donor.getFullName());
        }

        return savedDonor;
    }

    @Transactional
    public void deleteById(Long id) {
        donorRepository.findById(id).ifPresent(donor -> {
            donorRepository.deleteById(id);
            auditService.log("DONOR_DELETED", "Deleted donor profile: " + donor.getFullName());
        });
    }

    public void updateEligibilityStatus(Donor donor) {
        if (donor.getDateOfBirth() != null) {
            int age = Period.between(donor.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 18) {
                donor.setEligibilityStatus("UNDERAGE");
                return;
            } else if (age > 65) {
                donor.setEligibilityStatus("OVERAGE");
                return;
            }
        }

        if (donor.getLastDonationDate() != null) {
            long daysSinceLast = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
            if (daysSinceLast < 90) {
                donor.setEligibilityStatus("DEFERRED");
                return;
            }
        }

        // If not forced deferred/ineligible, set eligible
        if (!"INELIGIBLE".equals(donor.getEligibilityStatus())) {
            donor.setEligibilityStatus("ELIGIBLE");
        }
    }

    public boolean isEligibleForDonation(Donor donor) {
        updateEligibilityStatus(donor);
        return "ELIGIBLE".equals(donor.getEligibilityStatus());
    }

    public long getDaysUntilEligible(Donor donor) {
        if (donor.getLastDonationDate() == null) {
            return 0;
        }
        long daysSince = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
        long remaining = 90 - daysSince;
        return Math.max(0, remaining);
    }

    public long countByBloodGroup(String bloodGroup) {
        return donorRepository.countByBloodGroup(bloodGroup);
    }

    public long countTotalDonors() {
        return donorRepository.count();
    }
}
