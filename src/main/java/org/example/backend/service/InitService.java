package org.example.backend.service;

import jakarta.annotation.PostConstruct;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitService {

    private final RoleRepository roleRepository;

    @Autowired
    public InitService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
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
}