package com.bloodbank.config;

import com.bloodbank.entity.*;
import com.bloodbank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final DonorRepository donorRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final BloodDonationRepository donationRepository;
    private final BloodRequestRepository requestRepository;
    private final ActivityLogRepository logRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            log.info("Database already seeded. Skipping seeder.");
            return;
        }

        log.info("Seeding initial database records...");

        // 1. Seed Roles
        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Role staffRole = roleRepository.save(Role.builder().name("ROLE_STAFF").build());
        Role donorRole = roleRepository.save(Role.builder().name("ROLE_DONOR").build());

        // 2. Seed Users
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@bloodbank.org")
                .enabled(true)
                .roles(new HashSet<>(List.of(adminRole)))
                .build();
        userRepository.save(admin);

        User staff = User.builder()
                .username("staff")
                .password(passwordEncoder.encode("staff123"))
                .email("staff@bloodbank.org")
                .enabled(true)
                .roles(new HashSet<>(List.of(staffRole)))
                .build();
        userRepository.save(staff);

        // 3. Seed Hospitals
        Hospital hosp1 = Hospital.builder()
                .name("City General Hospital")
                .address("102 Medical Park Drive, Metro City")
                .contactNumber("9876543210")
                .email("info@cityhospital.org")
                .build();
        hospitalRepository.save(hosp1);

        Hospital hosp2 = Hospital.builder()
                .name("St. Jude Research Hospital")
                .address("501 St. Jude Place, Memphis")
                .contactNumber("8882772589")
                .email("contact@stjude.org")
                .build();
        hospitalRepository.save(hosp2);

        // 4. Seed Donors and linked User accounts
        User donorUser1 = User.builder()
                .username("john")
                .password(passwordEncoder.encode("donor123"))
                .email("john.doe@gmail.com")
                .enabled(true)
                .roles(new HashSet<>(List.of(donorRole)))
                .build();
        userRepository.save(donorUser1);

        Donor donor1 = Donor.builder()
                .user(donorUser1)
                .firstName("John")
                .lastName("Doe")
                .bloodGroup("O+")
                .contactNumber("9500123456")
                .email("john.doe@gmail.com")
                .address("742 Evergreen Terrace, Springfield")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender("MALE")
                .lastDonationDate(LocalDate.now().minusDays(100))
                .eligibilityStatus("ELIGIBLE")
                .build();
        donorRepository.save(donor1);

        User donorUser2 = User.builder()
                .username("jane")
                .password(passwordEncoder.encode("donor123"))
                .email("jane.smith@yahoo.com")
                .enabled(true)
                .roles(new HashSet<>(List.of(donorRole)))
                .build();
        userRepository.save(donorUser2);

        Donor donor2 = Donor.builder()
                .user(donorUser2)
                .firstName("Jane")
                .lastName("Smith")
                .bloodGroup("A-")
                .contactNumber("9500987654")
                .email("jane.smith@yahoo.com")
                .address("123 Maple Street, Smallville")
                .dateOfBirth(LocalDate.of(1995, 10, 22))
                .gender("FEMALE")
                .lastDonationDate(LocalDate.now().minusDays(30)) // Deferred since it is within 90 days
                .eligibilityStatus("DEFERRED")
                .build();
        donorRepository.save(donor2);

        // 5. Seed Blood Inventory
        // Healthy stock
        inventoryRepository.save(BloodInventory.builder()
                .bloodGroup("O+")
                .units(12)
                .receivedDate(LocalDate.now().minusDays(5))
                .expiryDate(LocalDate.now().plusDays(37))
                .status("AVAILABLE")
                .donor(donor1)
                .build());

        inventoryRepository.save(BloodInventory.builder()
                .bloodGroup("A-")
                .units(6)
                .receivedDate(LocalDate.now().minusDays(10))
                .expiryDate(LocalDate.now().plusDays(32))
                .status("AVAILABLE")
                .donor(donor2)
                .build());

        inventoryRepository.save(BloodInventory.builder()
                .bloodGroup("B+")
                .units(2) // Low Stock Alert group
                .receivedDate(LocalDate.now().minusDays(4))
                .expiryDate(LocalDate.now().plusDays(38))
                .status("AVAILABLE")
                .build());

        // Expiring batch
        inventoryRepository.save(BloodInventory.builder()
                .bloodGroup("AB+")
                .units(3)
                .receivedDate(LocalDate.now().minusDays(40))
                .expiryDate(LocalDate.now().plusDays(2)) // Expiring in 2 days!
                .status("AVAILABLE")
                .build());

        // Expired batch
        inventoryRepository.save(BloodInventory.builder()
                .bloodGroup("O-")
                .units(1)
                .receivedDate(LocalDate.now().minusDays(50))
                .expiryDate(LocalDate.now().minusDays(8)) // Already expired!
                .status("EXPIRED")
                .build());

        // 6. Seed Blood Donations
        BloodDonation donation1 = BloodDonation.builder()
                .donor(donor1)
                .donationDate(LocalDate.now().minusDays(100))
                .units(1)
                .bloodGroup("O+")
                .status("COMPLETED")
                .eligibilityChecked(true)
                .remarks("First donation, standard response.")
                .build();
        donationRepository.save(donation1);

        BloodDonation donation2 = BloodDonation.builder()
                .donor(donor2)
                .donationDate(LocalDate.now().minusDays(30))
                .units(1)
                .bloodGroup("A-")
                .status("COMPLETED")
                .eligibilityChecked(true)
                .remarks("Felt slight dizziness, recovered in 5 min.")
                .build();
        donationRepository.save(donation2);

        // Pending donation appointment
        BloodDonation donation3 = BloodDonation.builder()
                .donor(donor1)
                .donationDate(LocalDate.now().plusDays(3))
                .units(1)
                .bloodGroup("O+")
                .status("PENDING")
                .eligibilityChecked(false)
                .build();
        donationRepository.save(donation3);

        // 7. Seed Blood Requests
        BloodRequest request1 = BloodRequest.builder()
                .patientName("Robert Baratheon")
                .bloodGroup("O+")
                .unitsRequired(2)
                .hospital(hosp1)
                .requiredByDate(LocalDate.now().plusDays(1))
                .urgency("EMERGENCY")
                .status("FULFILLED")
                .notes("Emergency trauma case. Fulfill immediately.")
                .handledBy(admin)
                .build();
        requestRepository.save(request1);

        BloodRequest request2 = BloodRequest.builder()
                .patientName("Ned Stark")
                .bloodGroup("A-")
                .unitsRequired(4)
                .hospital(hosp2)
                .requiredByDate(LocalDate.now().plusDays(5))
                .urgency("NORMAL")
                .status("PENDING")
                .notes("Scheduled surgery request.")
                .build();
        requestRepository.save(request2);

        // 8. Seed Activity Log
        logRepository.save(ActivityLog.builder()
                .user(admin)
                .action("SYSTEM_INITIALIZED")
                .details("System initialization complete. Seeder executed successfully.")
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Database seeding complete.");
    }
}
