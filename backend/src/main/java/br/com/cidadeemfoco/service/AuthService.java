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
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.security.JwtService;
import br.com.cidadeemfoco.util.UserDataNormalizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CityRepository cityRepository;
    private final RegistrationEmailSender registrationEmailSender;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CityRepository cityRepository,
            RegistrationEmailSender registrationEmailSender
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.cityRepository = cityRepository;
        this.registrationEmailSender = registrationEmailSender;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Ja existe um usuario cadastrado com este email");
        }

        String cpf = UserDataNormalizer.normalizeCpf(request.cpf());
        if (userRepository.existsByCpf(cpf)) {
            throw new BusinessRuleException("Ja existe um usuario cadastrado com este CPF");
        }

        String whatsappPhone = UserDataNormalizer.normalizePhone(request.whatsappPhone());
        userRepository.findByWhatsappPhone(whatsappPhone).ifPresent(existingUser -> {
            throw new BusinessRuleException("Este numero de WhatsApp ja esta cadastrado");
        });

        if (!request.whatsappConsentGiven()) {
            throw new BusinessRuleException("E necessario autorizar o recebimento de alertas pelo WhatsApp");
        }

        String cityIbgeCode = request.cityIbgeCode().trim();
        var city = cityRepository.findByIbgeCodeAndActiveTrue(cityIbgeCode)
                .orElseThrow(() -> new BusinessRuleException("A cidade informada nao esta disponivel para cadastro"));

        User user = new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                UserRole.CITIZEN,
                city,
                cpf,
                request.street().trim(),
                request.addressNumber().trim(),
                normalizeOptional(request.addressComplement()),
                request.neighborhood().trim(),
                UserDataNormalizer.normalizePostalCode(request.postalCode())
        );
        user.enableWhatsappNotifications(whatsappPhone, Instant.now());
        User savedUser = userRepository.save(user);
        registrationEmailSender.sendRegistrationCompleted(savedUser.getEmail(), savedUser.getName());
        return createAuthResponse(savedUser);
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
        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
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

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
