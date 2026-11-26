package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.ForgotPasswordRequest;
import br.com.cidadeemfoco.dto.ResetPasswordRequest;
import br.com.cidadeemfoco.entity.PasswordResetToken;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.PasswordResetTokenRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RegistrationEmailSender emailSender;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailSender,
                Duration.ofMinutes(30),
                Duration.ofMinutes(1),
                "https://cidadeemfoco.example/"
        );
    }

    @Test
    void shouldReturnSilentlyWhenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        service.requestReset(new ForgotPasswordRequest(" UNKNOWN@example.com "));

        verify(tokenRepository, never()).save(any());
        verify(emailSender, never()).sendPasswordReset(any(), any(), any(), any(Long.class));
    }

    @Test
    void shouldCreateHashedTokenAndSendResetLink() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(user.getEmail()).thenReturn("ana@example.com");
        when(user.getName()).thenReturn("Ana");
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.empty());

        service.requestReset(new ForgotPasswordRequest("ana@example.com"));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailSender).sendPasswordReset(
                org.mockito.ArgumentMatchers.eq("ana@example.com"),
                org.mockito.ArgumentMatchers.eq("Ana"),
                linkCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(30L)
        );
        String rawToken = linkCaptor.getValue().substring(linkCaptor.getValue().indexOf("token=") + 6);
        assertThat(linkCaptor.getValue()).startsWith("https://cidadeemfoco.example/redefinir-senha?token=");
        assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64).doesNotContain(rawToken);
    }

    @Test
    void shouldResetPasswordOnlyOnce() {
        User user = new User("Ana", "ana@example.com", "old-hash", UserRole.CITIZEN);
        PasswordResetToken token = new PasswordResetToken(user,
                "7c222fb2927d828af22f592134e8932480637c0d1a5efb61e387e0926c26e9cf",
                Instant.now().plusSeconds(300));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new-hash");

        service.resetPassword(new ResetPasswordRequest("12345678", "novaSenha123"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.getUsedAt()).isNotNull();
        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("12345678", "outraSenha123")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("invalido ou expirou");
    }
}
