package br.com.cidadeemfoco.security;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.service.OccurrenceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "debug=false",
        "app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OccurrenceService occurrenceService;

    @Test
    void shouldAllowPublicCitizenRegistrationAndEncodePassword() throws Exception {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "email": "ANA@example.com",
                                  "password": "senha123",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("CITIZEN"))
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void shouldLoginAndReturnBearerToken() throws Exception {
        User citizen = citizen();
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana@example.com",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(86400))
                .andExpect(jsonPath("$.user.role").value("CITIZEN"));
    }

    @Test
    void shouldRejectInvalidPasswordWithoutRevealingDetails() throws Exception {
        User citizen = citizen();
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana@example.com",
                                  "password": "senha-errada"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha invalidos"));
    }

    @Test
    void shouldRequireTokenForOccurrenceData() throws Exception {
        mockMvc.perform(get("/api/occurrences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticacao necessaria"));
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/occurrences")
                        .header("Authorization", "Bearer token-adulterado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticacao necessaria"));
    }

    @Test
    void shouldUseCitizenIdentityFromValidToken() throws Exception {
        User citizen = citizen();
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        String token = jwtService.generateToken(citizen);

        mockMvc.perform(post("/api/occurrences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOccurrenceJson()))
                .andExpect(status().isCreated());

        verify(occurrenceService).create(eq("ana@example.com"), any(CreateOccurrenceRequest.class));
    }

    @Test
    void shouldForbidAdminFromCreatingOccurrenceButAllowReading() throws Exception {
        User admin = admin();
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(occurrenceService.findAll(any(OccurrenceFilter.class))).thenReturn(List.of());
        String token = jwtService.generateToken(admin);

        mockMvc.perform(post("/api/occurrences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOccurrenceJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso proibido"));

        mockMvc.perform(get("/api/occurrences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private User citizen() {
        return new User(
                "Ana",
                "ana@example.com",
                passwordEncoder.encode("senha123"),
                UserRole.CITIZEN
        );
    }

    private User admin() {
        return new User(
                "Administrador",
                "admin@example.com",
                passwordEncoder.encode("senha-admin"),
                UserRole.ADMIN
        );
    }

    private String validOccurrenceJson() {
        return """
                {
                  "category": "EVENTO_NATURAL",
                  "type": "ALAGAMENTO",
                  "description": "Via alagada",
                  "perceivedRisk": "ALTO",
                  "latitude": -24.005000,
                  "longitude": -46.402000
                }
                """;
    }
}
