package com.bloodbank.service;

import com.bloodbank.entity.*;
import com.bloodbank.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupRestoreService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final BloodDonationRepository donationRepository;
    private final BloodRequestRepository requestRepository;
    private final ActivityLogRepository logRepository;
    private final AuditService auditService;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional(readOnly = true)
    public String exportDatabaseToJson() throws Exception {
        Map<String, Object> backup = new LinkedHashMap<>();

        // 1. Roles
        List<Map<String, Object>> rolesList = roleRepository.findAll().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("name", r.getName());
            return map;
        }).collect(Collectors.toList());
        backup.put("roles", rolesList);

        // 2. Users
        List<Map<String, Object>> usersList = userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("password", u.getPassword());
            map.put("email", u.getEmail());
            map.put("enabled", u.isEnabled());
            map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            map.put("updatedAt", u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null);
            map.put("roleIds", u.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
        backup.put("users", usersList);

        // 3. Donors
        List<Map<String, Object>> donorsList = donorRepository.findAll().stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("userId", d.getUser() != null ? d.getUser().getId() : null);
            map.put("firstName", d.getFirstName());
            map.put("lastName", d.getLastName());
            map.put("bloodGroup", d.getBloodGroup());
            map.put("contactNumber", d.getContactNumber());
            map.put("email", d.getEmail());
            map.put("address", d.getAddress());
            map.put("dateOfBirth", d.getDateOfBirth().toString());
            map.put("gender", d.getGender());
            map.put("lastDonationDate", d.getLastDonationDate() != null ? d.getLastDonationDate().toString() : null);
            map.put("eligibilityStatus", d.getEligibilityStatus());
            map.put("createdAt", d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
            map.put("updatedAt", d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("donors", donorsList);

        // 4. Hospitals
        List<Map<String, Object>> hospitalsList = hospitalRepository.findAll().stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("name", h.getName());
            map.put("address", h.getAddress());
            map.put("contactNumber", h.getContactNumber());
            map.put("email", h.getEmail());
            map.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);
            map.put("updatedAt", h.getUpdatedAt() != null ? h.getUpdatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("hospitals", hospitalsList);

        // 5. BloodInventory
        List<Map<String, Object>> stockList = inventoryRepository.findAll().stream().map(bi -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bi.getId());
            map.put("bloodGroup", bi.getBloodGroup());
            map.put("units", bi.getUnits());
            map.put("receivedDate", bi.getReceivedDate().toString());
            map.put("expiryDate", bi.getExpiryDate().toString());
            map.put("status", bi.getStatus());
            map.put("donorId", bi.getDonor() != null ? bi.getDonor().getId() : null);
            map.put("createdAt", bi.getCreatedAt() != null ? bi.getCreatedAt().toString() : null);
            map.put("updatedAt", bi.getUpdatedAt() != null ? bi.getUpdatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("inventory", stockList);

        // 6. BloodDonations
        List<Map<String, Object>> donationsList = donationRepository.findAll().stream().map(bd -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bd.getId());
            map.put("donorId", bd.getDonor().getId());
            map.put("donationDate", bd.getDonationDate().toString());
            map.put("units", bd.getUnits());
            map.put("bloodGroup", bd.getBloodGroup());
            map.put("status", bd.getStatus());
            map.put("eligibilityChecked", bd.isEligibilityChecked());
            map.put("remarks", bd.getRemarks());
            map.put("createdAt", bd.getCreatedAt() != null ? bd.getCreatedAt().toString() : null);
            map.put("updatedAt", bd.getUpdatedAt() != null ? bd.getUpdatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("donations", donationsList);

        // 7. BloodRequests
        List<Map<String, Object>> requestsList = requestRepository.findAll().stream().map(br -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", br.getId());
            map.put("patientName", br.getPatientName());
            map.put("bloodGroup", br.getBloodGroup());
            map.put("unitsRequired", br.getUnitsRequired());
            map.put("hospitalId", br.getHospital() != null ? br.getHospital().getId() : null);
            map.put("requiredByDate", br.getRequiredByDate().toString());
            map.put("urgency", br.getUrgency());
            map.put("status", br.getStatus());
            map.put("notes", br.getNotes());
            map.put("handledBy", br.getHandledBy() != null ? br.getHandledBy().getId() : null);
            map.put("createdAt", br.getCreatedAt() != null ? br.getCreatedAt().toString() : null);
            map.put("updatedAt", br.getUpdatedAt() != null ? br.getUpdatedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("requests", requestsList);

        // 8. Logs
        List<Map<String, Object>> logsList = logRepository.findAll().stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("userId", l.getUser() != null ? l.getUser().getId() : null);
            map.put("action", l.getAction());
            map.put("details", l.getDetails());
            map.put("timestamp", l.getTimestamp() != null ? l.getTimestamp().toString() : null);
            return map;
        }).collect(Collectors.toList());
        backup.put("logs", logsList);

        auditService.log("BACKUP_CREATED", "Database backup generated successfully");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup);
    }

    @Transactional
    public void restoreDatabaseFromJson(InputStream is) throws Exception {
        Map<String, Object> backup = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
        log.info("Restoring database from JSON backup...");

        // 1. Delete existing records in dependency order
        logRepository.deleteAllInBatch();
        requestRepository.deleteAllInBatch();
        donationRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();
        donorRepository.deleteAllInBatch();
        hospitalRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        // 2. Restore Roles
        List<Map<String, Object>> roles = (List<Map<String, Object>>) backup.get("roles");
        Map<Long, Role> roleMap = new HashMap<>();
        for (Map<String, Object> rMap : roles) {
            Role r = Role.builder()
                    .name((String) rMap.get("name"))
                    .build();
            Role saved = roleRepository.save(r);
            roleMap.put(Long.valueOf(rMap.get("id").toString()), saved);
        }

        // 3. Restore Users
        List<Map<String, Object>> users = (List<Map<String, Object>>) backup.get("users");
        Map<Long, User> userMap = new HashMap<>();
        for (Map<String, Object> uMap : users) {
            List<Integer> rIds = (List<Integer>) uMap.get("roleIds");
            Set<Role> uRoles = rIds.stream().map(id -> roleMap.get(id.longValue())).filter(Objects::nonNull).collect(Collectors.toSet());

            User u = User.builder()
                    .username((String) uMap.get("username"))
                    .password((String) uMap.get("password"))
                    .email((String) uMap.get("email"))
                    .enabled((Boolean) uMap.get("enabled"))
                    .roles(uRoles)
                    .build();

            User saved = userRepository.save(u);
            userMap.put(Long.valueOf(uMap.get("id").toString()), saved);
        }

        // 4. Restore Donors
        List<Map<String, Object>> donors = (List<Map<String, Object>>) backup.get("donors");
        Map<Long, Donor> donorMap = new HashMap<>();
        for (Map<String, Object> dMap : donors) {
            Object uIdObj = dMap.get("userId");
            User linkedUser = uIdObj != null ? userMap.get(Long.valueOf(uIdObj.toString())) : null;

            Donor d = Donor.builder()
                    .user(linkedUser)
                    .firstName((String) dMap.get("firstName"))
                    .lastName((String) dMap.get("lastName"))
                    .bloodGroup((String) dMap.get("bloodGroup"))
                    .contactNumber((String) dMap.get("contactNumber"))
                    .email((String) dMap.get("email"))
                    .address((String) dMap.get("address"))
                    .dateOfBirth(LocalDate.parse((String) dMap.get("dateOfBirth")))
                    .gender((String) dMap.get("gender"))
                    .lastDonationDate(dMap.get("lastDonationDate") != null ? LocalDate.parse((String) dMap.get("lastDonationDate")) : null)
                    .eligibilityStatus((String) dMap.get("eligibilityStatus"))
                    .build();

            Donor saved = donorRepository.save(d);
            donorMap.put(Long.valueOf(dMap.get("id").toString()), saved);
        }

        // 5. Restore Hospitals
        List<Map<String, Object>> hospitals = (List<Map<String, Object>>) backup.get("hospitals");
        Map<Long, Hospital> hospitalMap = new HashMap<>();
        for (Map<String, Object> hMap : hospitals) {
            Hospital h = Hospital.builder()
                    .name((String) hMap.get("name"))
                    .address((String) hMap.get("address"))
                    .contactNumber((String) hMap.get("contactNumber"))
                    .email((String) hMap.get("email"))
                    .build();
            Hospital saved = hospitalRepository.save(h);
            hospitalMap.put(Long.valueOf(hMap.get("id").toString()), saved);
        }

        // 6. Restore Inventory
        List<Map<String, Object>> inventory = (List<Map<String, Object>>) backup.get("inventory");
        for (Map<String, Object> biMap : inventory) {
            Object dIdObj = biMap.get("donorId");
            Donor d = dIdObj != null ? donorMap.get(Long.valueOf(dIdObj.toString())) : null;

            BloodInventory bi = BloodInventory.builder()
                    .bloodGroup((String) biMap.get("bloodGroup"))
                    .units((Integer) biMap.get("units"))
                    .receivedDate(LocalDate.parse((String) biMap.get("receivedDate")))
                    .expiryDate(LocalDate.parse((String) biMap.get("expiryDate")))
                    .status((String) biMap.get("status"))
                    .donor(d)
                    .build();
            inventoryRepository.save(bi);
        }

        // 7. Restore Donations
        List<Map<String, Object>> donations = (List<Map<String, Object>>) backup.get("donations");
        for (Map<String, Object> bdMap : donations) {
            Donor d = donorMap.get(Long.valueOf(bdMap.get("donorId").toString()));
            BloodDonation bd = BloodDonation.builder()
                    .donor(d)
                    .donationDate(LocalDate.parse((String) bdMap.get("donationDate")))
                    .units((Integer) bdMap.get("units"))
                    .bloodGroup((String) bdMap.get("bloodGroup"))
                    .status((String) bdMap.get("status"))
                    .eligibilityChecked((Boolean) bdMap.get("eligibilityChecked"))
                    .remarks((String) bdMap.get("remarks"))
                    .build();
            donationRepository.save(bd);
        }

        // 8. Restore Requests
        List<Map<String, Object>> requests = (List<Map<String, Object>>) backup.get("requests");
        for (Map<String, Object> brMap : requests) {
            Object hIdObj = brMap.get("hospitalId");
            Hospital h = hIdObj != null ? hospitalMap.get(Long.valueOf(hIdObj.toString())) : null;

            Object hByUserObj = brMap.get("handledBy");
            User handler = hByUserObj != null ? userMap.get(Long.valueOf(hByUserObj.toString())) : null;

            BloodRequest br = BloodRequest.builder()
                    .patientName((String) brMap.get("patientName"))
                    .bloodGroup((String) brMap.get("bloodGroup"))
                    .unitsRequired((Integer) brMap.get("unitsRequired"))
                    .hospital(h)
                    .requiredByDate(LocalDate.parse((String) brMap.get("requiredByDate")))
                    .urgency((String) brMap.get("urgency"))
                    .status((String) brMap.get("status"))
                    .notes((String) brMap.get("notes"))
                    .handledBy(handler)
                    .build();
            requestRepository.save(br);
        }

        // 9. Restore Logs
        List<Map<String, Object>> logs = (List<Map<String, Object>>) backup.get("logs");
        for (Map<String, Object> lMap : logs) {
            Object uIdObj = lMap.get("userId");
            User u = uIdObj != null ? userMap.get(Long.valueOf(uIdObj.toString())) : null;

            ActivityLog logRecord = ActivityLog.builder()
                    .user(u)
                    .action((String) lMap.get("action"))
                    .details((String) lMap.get("details"))
                    .timestamp(LocalDateTime.parse((String) lMap.get("timestamp")))
                    .build();
            logRepository.save(logRecord);
        }

        auditService.log("RESTORE_COMPLETED", "Database backup restored successfully");
        log.info("Database restore complete!");
    }
}
