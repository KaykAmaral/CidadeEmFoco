package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.AuthResponse;
import br.com.cidadeemfoco.dto.LoginRequest;
import br.com.cidadeemfoco.dto.RegisterRequest;
import br.com.cidadeemfoco.dto.UserResponse;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.InvalidCredentialsException;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Ja existe um usuario cadastrado com este email");
        }

        User user = new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                UserRole.CITIZEN
        );
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.password())
            );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponse.from(user)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
