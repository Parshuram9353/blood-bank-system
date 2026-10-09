package com.bloodbank.repository;

import com.bloodbank.entity.BloodRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByStatus(String status);
    Page<BloodRequest> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);

    @Query("SELECT br FROM BloodRequest br WHERE br.requiredByDate BETWEEN :startDate AND :endDate")
    List<BloodRequest> findRequestsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT br FROM BloodRequest br WHERE " +
           "(LOWER(br.patientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(br.hospital.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(br.bloodGroup) = LOWER(:query) OR LOWER(br.status) = LOWER(:query))")
    Page<BloodRequest> searchRequests(@Param("query") String query, Pageable pageable);

    @Query("SELECT br FROM BloodRequest br ORDER BY br.createdAt DESC")
    List<BloodRequest> findRecentRequests(Pageable pageable);
}
