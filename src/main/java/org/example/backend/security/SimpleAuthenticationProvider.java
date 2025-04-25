package org.example.backend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.service.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SimpleAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAuthenticationProvider.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.info("Intentando autenticar usuario: {}", username);
        System.out.println("AUTH - Usuario: " + username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            logger.warn("Usuario no encontrado: {}", username);
            throw new BadCredentialsException("Usuario no encontrado");
        }

        User user = userOpt.get();
        System.out.println("AUTH - Contraseña almacenada: " + user.getPassword());

        if (!password.equals(user.getPassword())) {
            logger.warn("Contraseña incorrecta para usuario: {}", username);
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // Obtener roles del usuario
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        System.out.println("AUTH - Roles: " + authorities);

        // En este punto, simplemente usa el nombre de usuario como principal
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}