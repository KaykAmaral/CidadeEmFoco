package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.ForgotPasswordRequest;
import br.com.cidadeemfoco.dto.ResetPasswordRequest;
import br.com.cidadeemfoco.entity.PasswordResetToken;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.PasswordResetTokenRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationEmailSender emailSender;
    private final Duration tokenLifetime;
    private final Duration requestCooldown;
    private final String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            RegistrationEmailSender emailSender,
            @Value("${app.email.password-reset-token-lifetime:30m}") Duration tokenLifetime,
            @Value("${app.email.password-reset-request-cooldown:1m}") Duration requestCooldown,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.tokenLifetime = tokenLifetime;
        this.requestCooldown = requestCooldown;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            Instant now = Instant.now();
            boolean coolingDown = tokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                    .map(token -> token.getCreatedAt().plus(requestCooldown).isAfter(now))
                    .orElse(false);
            if (coolingDown) return;

            tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())
                    .forEach(token -> token.markUsed(now));

            String rawToken = generateToken();
            tokenRepository.save(new PasswordResetToken(
                    user,
                    hash(rawToken),
                    now.plus(tokenLifetime)
            ));
            emailSender.sendPasswordReset(
                    user.getEmail(),
                    user.getName(),
                    frontendUrl + "/redefinir-senha?token=" + rawToken,
                    tokenLifetime.toMinutes()
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(request.token().trim()))
                .filter(candidate -> candidate.isAvailableAt(now))
                .orElseThrow(() -> new BusinessRuleException("O link de recuperacao e invalido ou expirou"));

        token.getUser().changePassword(passwordEncoder.encode(request.newPassword()));
        token.markUsed(now);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }
}
