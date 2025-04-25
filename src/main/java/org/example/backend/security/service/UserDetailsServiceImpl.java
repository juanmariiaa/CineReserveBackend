package org.example.backend.security.service;

import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            logger.info("Intentando cargar usuario: {}", username);

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (!userOptional.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                throw new UsernameNotFoundException("User Not Found with username: " + username);
            }

            User user = userOptional.get();
            logger.info("Usuario encontrado: {}, password: {}", username, user.getPassword());

            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            logger.info("Roles del usuario: {}",
                    userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .collect(Collectors.joining(", ")));

            return userDetails;
        } catch (Exception e) {
            logger.error("Error inesperado al cargar el usuario: {}", e.getMessage(), e);
            throw e;
        }
    }
}
