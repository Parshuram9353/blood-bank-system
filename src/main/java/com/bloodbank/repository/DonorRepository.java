package com.bloodbank.repository;

import com.bloodbank.entity.Donor;
import com.bloodbank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByUser(User user);
    Optional<Donor> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT d FROM Donor d WHERE " +
           "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.bloodGroup) = LOWER(:query)")
    Page<Donor> searchDonors(@Param("query") String query, Pageable pageable);

    @Query("SELECT d FROM Donor d WHERE " +
           "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.bloodGroup) = LOWER(:query)")
    List<Donor> searchDonorsList(@Param("query") String query);

    long countByBloodGroup(String bloodGroup);
}
