package org.example.backend.security.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    @Value("${google.client.id}")
    private String clientId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public String authenticateGoogleToken(String idTokenString) throws Exception {
        logger.info("Authenticating Google token");

        if (idTokenString == null || idTokenString.isEmpty()) {
            logger.error("Token is null or empty");
            throw new Exception("Token cannot be empty");
        }

        try {
            // Create a simple verifier with minimal configuration
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            // Verify the token
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                logger.error("Invalid ID token - verification failed");
                throw new Exception("Invalid ID token: verification failed");
            }

            // Extract user information from the token
            Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (email == null) {
                logger.error("Email is missing from token payload");
                throw new Exception("Email not provided in token");
            }

            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            // Find or create the user
            User user = findOrCreateUser(email, firstName, lastName);

            // Create authentication object
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            logger.info("JWT token generated successfully for user: {}", user.getUsername());
            return jwt;

        } catch (GeneralSecurityException e) {
            logger.error("Security exception during token verification", e);
            throw new Exception("Invalid ID token: Security error");
        } catch (IOException e) {
            logger.error("IO exception during token verification", e);
            throw new Exception("Invalid ID token: Network error");
        } catch (Exception e) {
            logger.error("Unexpected error during token verification", e);
            throw new Exception("Token verification failed: " + e.getMessage());
        }
    }

    private User findOrCreateUser(String email, String firstName, String lastName) {
        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            logger.info("Creating new user for email: {}", email);
            // Create new user
            user = new User();
            user.setEmail(email);

            // Generate a username based on email
            String username = email.substring(0, email.indexOf('@')) + "-" +
                    UUID.randomUUID().toString().substring(0, 6);
            user.setUsername(username);

            // Set user details
            user.setFirstName(firstName != null ? firstName : "");
            user.setLastName(lastName != null ? lastName : "");
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);

            // Set role to USER
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(Role.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
            user.setRoles(roles);

            userRepository.save(user);
            logger.info("New user created with username: {}", username);
        } else {
            logger.info("User found with email: {}", email);
        }

        return user;
    }
}