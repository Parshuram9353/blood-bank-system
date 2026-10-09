package com.bloodbank.service;

import com.bloodbank.entity.BloodInventory;
import com.bloodbank.entity.Donor;
import com.bloodbank.repository.BloodInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final BloodInventoryRepository inventoryRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${app.blood.expiry-days:42}")
    private int expiryDays;

    @Value("${app.blood.low-stock-threshold:5}")
    private int lowStockThreshold;

    public Page<BloodInventory> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return inventoryRepository.findAll(pageable);
    }

    public Page<BloodInventory> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        if (query == null || query.trim().isEmpty()) {
            return inventoryRepository.findAll(pageable);
        }
        return inventoryRepository.searchInventory(query, pageable);
    }

    public Optional<BloodInventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Transactional
    public BloodInventory save(BloodInventory inventory) {
        boolean isNew = inventory.getId() == null;
        if (isNew && inventory.getExpiryDate() == null) {
            inventory.setExpiryDate(inventory.getReceivedDate().plusDays(expiryDays));
        }

        BloodInventory saved = inventoryRepository.save(inventory);

        if (isNew) {
            auditService.log("INVENTORY_ADDED", "Added " + inventory.getUnits() + " units of " + inventory.getBloodGroup());
        } else {
            auditService.log("INVENTORY_UPDATED", "Updated inventory batch #" + inventory.getId());
        }

        checkLowStock(inventory.getBloodGroup());
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        inventoryRepository.findById(id).ifPresent(item -> {
            inventoryRepository.deleteById(id);
            auditService.log("INVENTORY_DELETED", "Deleted inventory batch #" + id + " of " + item.getBloodGroup());
        });
    }

    @Transactional
    public void addBloodUnits(String bloodGroup, int units, LocalDate receivedDate, Donor donor) {
        LocalDate expiry = receivedDate.plusDays(expiryDays);
        BloodInventory item = BloodInventory.builder()
                .bloodGroup(bloodGroup)
                .units(units)
                .receivedDate(receivedDate)
                .expiryDate(expiry)
                .donor(donor)
                .status("AVAILABLE")
                .build();
        inventoryRepository.save(item);
        auditService.log("INVENTORY_AUTO_ADDED", String.format("Auto-added %d units of %s from donor %s", units, bloodGroup, donor != null ? donor.getFullName() : "Anonymous"));
        checkLowStock(bloodGroup);
    }

    @Transactional
    public boolean useBloodUnits(String bloodGroup, int units) {
        List<BloodInventory> activeStock = inventoryRepository.findByBloodGroupAndStatus(bloodGroup, "AVAILABLE");
        
        // Expiry First (FIFO on expiry date)
        activeStock.sort(Comparator.comparing(BloodInventory::getExpiryDate));

        int totalAvailable = activeStock.stream().mapToInt(BloodInventory::getUnits).sum();
        if (totalAvailable < units) {
            log.warn("Insufficient stock for group {}. Needed: {}, Available: {}", bloodGroup, units, totalAvailable);
            return false;
        }

        int needed = units;
        for (BloodInventory bag : activeStock) {
            if (bag.getUnits() >= needed) {
                bag.setUnits(bag.getUnits() - needed);
                if (bag.getUnits() == 0) {
                    bag.setStatus("USED");
                }
                inventoryRepository.save(bag);
                needed = 0;
                break;
            } else {
                needed -= bag.getUnits();
                bag.setUnits(0);
                bag.setStatus("USED");
                inventoryRepository.save(bag);
            }
        }

        auditService.log("INVENTORY_DEDUCTED", String.format("Deducted %d units of %s from stock", units, bloodGroup));
        checkLowStock(bloodGroup);
        return needed == 0;
    }

    public int getStockCount(String bloodGroup) {
        return inventoryRepository.getTotalUnitsByBloodGroup(bloodGroup);
    }

    public int getTotalAvailableUnits() {
        return inventoryRepository.getTotalAvailableUnits();
    }

    public long getExpiredUnitsCount() {
        return inventoryRepository.countExpiredUnits(LocalDate.now());
    }

    public Map<String, Integer> getStockOverviewMap() {
        Map<String, Integer> overview = new LinkedHashMap<>();
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String group : groups) {
            overview.put(group, getStockCount(group));
        }
        return overview;
    }

    @Scheduled(cron = "0 0 1 * * ?") // 1:00 AM daily
    @Transactional
    public void markExpiredUnits() {
        log.info("Running daily scheduled job to check expired blood units");
        List<BloodInventory> expiring = inventoryRepository.findByExpiryDateBeforeAndStatus(LocalDate.now(), "AVAILABLE");
        for (BloodInventory item : expiring) {
            item.setStatus("EXPIRED");
            inventoryRepository.save(item);
            auditService.log("BLOOD_EXPIRED", "Batch #" + item.getId() + " (" + item.getBloodGroup() + ") marked as EXPIRED");
            emailService.sendBloodExpiryAlert(item.getBloodGroup(), item.getId().toString(), item.getUnits(), item.getExpiryDate().toString());
        }
    }

    private void checkLowStock(String bloodGroup) {
        int currentStock = getStockCount(bloodGroup);
        if (currentStock < lowStockThreshold) {
            emailService.sendLowStockAlert(bloodGroup, currentStock);
        }
    }
}
