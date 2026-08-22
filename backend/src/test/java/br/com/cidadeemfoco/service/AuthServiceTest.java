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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void shouldRegisterOnlyCitizenWithEncodedPassword() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(
                new RegisterRequest(" Ana ", " ANA@EXAMPLE.COM ", "senha123")
        );

        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.role()).isEqualTo(UserRole.CITIZEN);
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Ana", "ana@example.com", "senha123")
        )).isInstanceOf(BusinessRuleException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldAuthenticateAndReturnToken() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(jwtService.generateToken(citizen)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.login(new LoginRequest("ANA@example.com", "senha123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(86400L);
        assertThat(response.user().role()).isEqualTo(UserRole.CITIZEN);
    }

    @Test
    void shouldReturnGenericErrorForInvalidCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("ana@example.com", "senha-errada")
        )).isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email ou senha invalidos");
    }
}
