package org.example.backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        // Update user details
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public User changeUserRole(Long id, List<String> roleNames) {
        User user = getUserById(id);
        
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public User activateUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(false);
        return userRepository.save(user);
    }
    
    public User giveAdminRole(Long id) {
        User user = getUserById(id);
        Set<Role> roles = user.getRoles();
        
        // Add ROLE_ADMIN if not already present
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new EntityNotFoundException("Admin role not found"));
        roles.add(adminRole);
        
        user.setRoles(roles);
        return userRepository.save(user);
    }
}