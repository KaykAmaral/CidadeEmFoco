package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminProvisioningService service;
    private City city;

    @BeforeEach
    void setUp() {
        service = new AdminProvisioningService(userRepository, cityRepository, passwordEncoder);
        city = new City("Praia Grande", "SP", "3541000");
    }

    @Test
    void shouldCreateAdminWithEncodedPasswordAndCity() {
        when(cityRepository.findByIbgeCodeAndActiveTrue("3541000")).thenReturn(Optional.of(city));
        when(userRepository.findByEmailIgnoreCase("admin@cidade.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash-bcrypt");

        boolean created = service.provision(
                "Prefeitura de Praia Grande",
                " ADMIN@CIDADE.COM ",
                "senha-segura",
                "3541000"
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User admin = userCaptor.getValue();
        assertThat(created).isTrue();
        assertThat(admin.getEmail()).isEqualTo("admin@cidade.com");
        assertThat(admin.getPasswordHash()).isEqualTo("hash-bcrypt");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getCity()).isSameAs(city);
    }

    @Test
    void shouldBeIdempotentWhenAdminAlreadyExistsInSameCity() {
        User existing = new User("Admin", "admin@cidade.com", "hash", UserRole.ADMIN, city);
        when(cityRepository.findByIbgeCodeAndActiveTrue("3541000")).thenReturn(Optional.of(city));
        when(userRepository.findByEmailIgnoreCase("admin@cidade.com")).thenReturn(Optional.of(existing));

        boolean created = service.provision("Admin", "admin@cidade.com", "senha-segura", "3541000");

        assertThat(created).isFalse();
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectCitizenEmailInsteadOfPromotingIt() {
        User citizen = new User("Ana", "ana@cidade.com", "hash", UserRole.CITIZEN, city);
        when(cityRepository.findByIbgeCodeAndActiveTrue("3541000")).thenReturn(Optional.of(city));
        when(userRepository.findByEmailIgnoreCase("ana@cidade.com")).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> service.provision(
                "Admin", "ana@cidade.com", "senha-segura", "3541000"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("ADMIN_BOOTSTRAP_EMAIL ja pertence a um cidadao");
    }

    @Test
    void shouldRejectInvalidConfiguration() {
        assertThatThrownBy(() -> service.provision("Admin", "email-invalido", "1234", "3541000"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ADMIN_BOOTSTRAP_EMAIL deve ser um email valido");

        verify(cityRepository, never()).findByIbgeCodeAndActiveTrue(org.mockito.ArgumentMatchers.anyString());
    }
}
