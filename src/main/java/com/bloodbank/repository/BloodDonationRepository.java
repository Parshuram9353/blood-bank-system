package com.bloodbank.repository;

import com.bloodbank.entity.BloodDonation;
import com.bloodbank.entity.Donor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodDonationRepository extends JpaRepository<BloodDonation, Long> {
    List<BloodDonation> findByDonor(Donor donor);
    Page<BloodDonation> findByDonor(Donor donor, Pageable pageable);
    List<BloodDonation> findByStatus(String status);
    Page<BloodDonation> findByStatus(String status, Pageable pageable);

    @Query("SELECT bd FROM BloodDonation bd ORDER BY bd.donationDate DESC")
    List<BloodDonation> findRecentDonations(Pageable pageable);

    @Query("SELECT bd FROM BloodDonation bd WHERE bd.donationDate = :date")
    List<BloodDonation> findDonationsByDate(@Param("date") LocalDate date);

    @Query("SELECT bd FROM BloodDonation bd WHERE bd.donationDate BETWEEN :startDate AND :endDate")
    List<BloodDonation> findDonationsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT bd FROM BloodDonation bd WHERE " +
           "(LOWER(bd.donor.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(bd.donor.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(bd.bloodGroup) = LOWER(:query) OR LOWER(bd.status) = LOWER(:query))")
    Page<BloodDonation> searchDonations(@Param("query") String query, Pageable pageable);
}
