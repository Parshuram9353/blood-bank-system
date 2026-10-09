package com.bloodbank.service;

import com.bloodbank.dto.DonorRegistrationDto;
import com.bloodbank.entity.Donor;
import com.bloodbank.entity.Role;
import com.bloodbank.entity.User;
import com.bloodbank.repository.DonorRepository;
import com.bloodbank.repository.RoleRepository;
import com.bloodbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DonorRepository donorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional
    public User registerDonor(DonorRegistrationDto dto) {
        log.info("Registering donor user: {}", dto.getUsername());

        // 1. Fetch or create ROLE_DONOR
        Role donorRole = roleRepository.findByName("ROLE_DONOR")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_DONOR").build()));

        // 2. Create User account
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .enabled(true)
                .roles(Collections.singleton(donorRole))
                .build();

        User savedUser = userRepository.save(user);

        // 3. Create Donor Profile
        Donor donor = Donor.builder()
                .user(savedUser)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .bloodGroup(dto.getBloodGroup())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .eligibilityStatus("ELIGIBLE")
                .build();

        donorRepository.save(donor);

        auditService.log("DONOR_REGISTERED", "Registered new donor username: " + dto.getUsername());

        return savedUser;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}
