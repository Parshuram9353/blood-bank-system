package com.bloodbank.service;

import com.bloodbank.dto.DonorRegistrationDto;
import com.bloodbank.entity.User;

import java.util.Optional;

public interface UserService {
    User registerDonor(DonorRegistrationDto registrationDto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}
