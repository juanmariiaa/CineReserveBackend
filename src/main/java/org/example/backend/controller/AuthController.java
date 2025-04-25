package org.example.backend.controller;

import org.example.backend.model.ERole;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.payload.request.LoginRequest;
import org.example.backend.payload.request.SignupRequest;
import org.example.backend.payload.response.JwtResponse;
import org.example.backend.payload.response.MessageResponse;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.SimpleAuthenticationProvider;
import org.example.backend.security.jwt.JwtUtils;
import org.example.backend.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    SimpleAuthenticationProvider simpleAuthProvider;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("SIGNIN - Usuario: " + loginRequest.getUsername());
            System.out.println("SIGNIN - Contraseña: " + loginRequest.getPassword());

            // Crear la autenticación
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());

            // Autenticar usando nuestro proveedor personalizado
            Authentication authenticated = simpleAuthProvider.authenticate(authToken);

            // Establecer la autenticación en el contexto
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            String jwt = jwtUtils.generateJwtToken(authenticated);

            List<String> roles = authenticated.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Buscar el usuario para obtener más datos
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

            System.out.println("SIGNIN - Autenticación exitosa, JWT generado");

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "roles", roles
            ));
        } catch (Exception e) {
            System.out.println("SIGNIN - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Error de autenticación: " + e.getMessage()
            ));
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        // Guardar la contraseña en texto plano
        user.setPassword(signUpRequest.getPassword()); // Quita el encoder.encode
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    @PostMapping("/test-signin")
    public ResponseEntity<?> testSignin(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("TEST SIGNIN - Usuario: " + loginRequest.getUsername());
            System.out.println("TEST SIGNIN - Contraseña: " + loginRequest.getPassword());

            // Crear la autenticación
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());

            // Autenticar usando nuestro proveedor
            Authentication authenticated = simpleAuthProvider.authenticate(authToken);

            // Establecer la autenticación en el contexto
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            String jwt = jwtUtils.generateJwtToken(authenticated);

            List<String> roles = authenticated.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            System.out.println("TEST SIGNIN - Autenticación exitosa, JWT generado");

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "username", authenticated.getName(),
                    "roles", roles
            ));
        } catch (Exception e) {
            System.out.println("TEST SIGNIN - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error de autenticación: " + e.getMessage()
            ));
        }
    }}