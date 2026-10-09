package com.bloodbank.repository;

import com.bloodbank.entity.BloodInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    List<BloodInventory> findByStatus(String status);
    List<BloodInventory> findByBloodGroupAndStatus(String bloodGroup, String status);
    Page<BloodInventory> findByStatus(String status, Pageable pageable);

    @Query("SELECT bi.bloodGroup, COALESCE(SUM(bi.units), 0) FROM BloodInventory bi WHERE bi.status = 'AVAILABLE' GROUP BY bi.bloodGroup")
    List<Object[]> getBloodStockOverview();

    @Query("SELECT COALESCE(SUM(bi.units), 0) FROM BloodInventory bi WHERE bi.bloodGroup = :bloodGroup AND bi.status = 'AVAILABLE'")
    int getTotalUnitsByBloodGroup(@Param("bloodGroup") String bloodGroup);

    @Query("SELECT COALESCE(SUM(bi.units), 0) FROM BloodInventory bi WHERE bi.status = 'AVAILABLE'")
    int getTotalAvailableUnits();

    List<BloodInventory> findByExpiryDateBeforeAndStatus(LocalDate date, String status);
    
    @Query("SELECT COUNT(bi) FROM BloodInventory bi WHERE bi.expiryDate <= :date AND bi.status = 'AVAILABLE'")
    long countExpiredUnits(@Param("date") LocalDate date);

    @Query("SELECT bi FROM BloodInventory bi WHERE " +
           "(LOWER(bi.bloodGroup) = LOWER(:query) OR LOWER(bi.status) = LOWER(:query))")
    Page<BloodInventory> searchInventory(@Param("query") String query, Pageable pageable);
}
