package org.example.backend.config;

import org.example.backend.model.ERole;
import org.example.backend.model.Role;
import org.example.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Inicializando roles...");

        initRole(ERole.ROLE_USER);
        initRole(ERole.ROLE_ADMIN);

        System.out.println("Roles inicializados correctamente");
    }

    private void initRole(ERole roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("Rol " + roleName + " creado");
        }
    }
}