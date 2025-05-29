package org.example.backend.service;

import jakarta.annotation.PostConstruct;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class InitService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public InitService(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        initRoles();
        initAdminUser();
    }
    
    private void initRoles() {
        if (roleRepository.findByName(Role.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(Role.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }
    
    private void initAdminUser() {
        // Check if admin user already exists
        if (!userRepository.existsByUsername("admin") && !userRepository.existsByEmail("admin@admin.com")) {
            // Create default admin user
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@admin.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            adminUser.setIsActive(true);
            
            // Assign admin role
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
            roles.add(adminRole);
            adminUser.setRoles(roles);
            
            // Save admin user
            userRepository.save(adminUser);
        }
    }
}