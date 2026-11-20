package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.dto.UpdateUserProfileRequest;
import br.com.cidadeemfoco.dto.UserProfileResponse;
import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private WhatsappNotificationRepository notificationRepository;

    private UserService userService;
    private User citizen;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, cityRepository, notificationRepository, properties(false));
        citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
    }

    @Test
    void shouldReturnCurrentWhatsappPreferences() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        WhatsappPreferencesResponse response = userService.findWhatsappPreferences(" ANA@EXAMPLE.COM ");

        assertThat(response.notificationsEnabled()).isFalse();
        assertThat(response.phoneNumber()).isNull();
        assertThat(response.consentAt()).isNull();
        assertThat(response.deliveryEnabled()).isFalse();
    }

    @Test
    void shouldNormalizeBrazilianPhoneAndEnableNotifications() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByWhatsappPhone("+5513999999999")).thenReturn(Optional.empty());
        when(userRepository.save(citizen)).thenReturn(citizen);

        WhatsappPreferencesResponse response = userService.enableWhatsappNotifications(
                "ana@example.com",
                new UpdateWhatsappPreferencesRequest("(13) 99999-9999", true)
        );

        assertThat(response.phoneNumber()).isEqualTo("+5513999999999");
        assertThat(response.notificationsEnabled()).isTrue();
        assertThat(response.consentAt()).isNotNull();
        verify(userRepository).save(citizen);
    }

    @Test
    void shouldAcceptPhoneAlreadyInInternationalFormat() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByWhatsappPhone("+5513999999999")).thenReturn(Optional.empty());
        when(userRepository.save(citizen)).thenReturn(citizen);

        WhatsappPreferencesResponse response = userService.enableWhatsappNotifications(
                "ana@example.com",
                new UpdateWhatsappPreferencesRequest("+55 13 99999-9999", true)
        );

        assertThat(response.phoneNumber()).isEqualTo("+5513999999999");
    }

    @Test
    void shouldRejectInvalidPhone() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        assertThatThrownBy(() -> userService.enableWhatsappNotifications(
                "ana@example.com",
                new UpdateWhatsappPreferencesRequest("123", true)
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("valido");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectPhoneAlreadyUsedByAnotherUser() {
        User anotherCitizen = new User("Bia", "bia@example.com", "hash", UserRole.CITIZEN);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByWhatsappPhone("+5513999999999"))
                .thenReturn(Optional.of(anotherCitizen));

        assertThatThrownBy(() -> userService.enableWhatsappNotifications(
                "ana@example.com",
                new UpdateWhatsappPreferencesRequest("(13) 99999-9999", true)
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja esta cadastrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDisableNotificationsAndRemoveStoredPhone() {
        citizen.enableWhatsappNotifications("+5513999999999", java.time.Instant.now());
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        userService.disableWhatsappNotifications("ana@example.com");

        assertThat(citizen.isWhatsappNotificationsEnabled()).isFalse();
        assertThat(citizen.getWhatsappPhone()).isNull();
        assertThat(citizen.getWhatsappConsentAt()).isNull();
        verify(userRepository).save(citizen);
    }

    @Test
    void shouldCancelQueuedNotificationsWhenConsentIsRevoked() {
        WhatsappNotification pending = mock(WhatsappNotification.class);
        ReflectionTestUtils.setField(citizen, "id", 20L);
        citizen.enableWhatsappNotifications("+5513999999999", java.time.Instant.now());
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(notificationRepository.findByUserIdAndStatusIn(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.<WhatsappNotificationStatus>anyCollection()
        )).thenReturn(List.of(pending));

        userService.disableWhatsappNotifications("ana@example.com");

        verify(pending).cancel();
        verify(notificationRepository).saveAll(List.of(pending));
    }

    @Test
    void shouldReturnAuthenticatedCitizenProfile() {
        City city = city(1L, "Praia Grande", "3541000");
        citizen = fullCitizen(city);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        UserProfileResponse response = userService.findProfile("ana@example.com");

        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.cpf()).isEqualTo("52998224725");
        assertThat(response.city().ibgeCode()).isEqualTo("3541000");
        assertThat(response.address().postalCode()).isEqualTo("11700000");
    }

    @Test
    void shouldUpdateCitizenProfile() {
        City city = city(1L, "Praia Grande", "3541000");
        citizen = fullCitizen(city);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByCpf("11144477735")).thenReturn(Optional.empty());
        when(userRepository.findByWhatsappPhone("+5513988887777")).thenReturn(Optional.empty());
        when(cityRepository.findByIbgeCodeAndActiveTrue("3541000")).thenReturn(Optional.of(city));
        when(userRepository.save(citizen)).thenReturn(citizen);

        UserProfileResponse response = userService.updateProfile(
                "ana@example.com",
                updateRequest("3541000")
        );

        assertThat(response.name()).isEqualTo("Ana Atualizada");
        assertThat(response.cpf()).isEqualTo("11144477735");
        assertThat(response.whatsappPhone()).isEqualTo("+5513988887777");
        assertThat(response.address().street()).isEqualTo("Rua Nova");
        assertThat(response.address().complement()).isNull();
        verify(userRepository).save(citizen);
    }

    @Test
    void shouldRejectCpfUsedByAnotherCitizenDuringProfileUpdate() {
        City city = city(1L, "Praia Grande", "3541000");
        citizen = fullCitizen(city);
        User another = new User("Bia", "bia@example.com", "hash", UserRole.CITIZEN, city);
        ReflectionTestUtils.setField(another, "id", 30L);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByCpf("11144477735")).thenReturn(Optional.of(another));

        assertThatThrownBy(() -> userService.updateProfile("ana@example.com", updateRequest("3541000")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CPF");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCancelPendingNotificationsWhenCitizenChangesCity() {
        City praiaGrande = city(1L, "Praia Grande", "3541000");
        City santos = city(2L, "Santos", "3548500");
        citizen = fullCitizen(praiaGrande);
        WhatsappNotification pending = mock(WhatsappNotification.class);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(userRepository.findByCpf("11144477735")).thenReturn(Optional.empty());
        when(userRepository.findByWhatsappPhone("+5513988887777")).thenReturn(Optional.empty());
        when(cityRepository.findByIbgeCodeAndActiveTrue("3548500")).thenReturn(Optional.of(santos));
        when(notificationRepository.findByUserIdAndStatusIn(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.<WhatsappNotificationStatus>anyCollection()
        )).thenReturn(List.of(pending));
        when(userRepository.save(citizen)).thenReturn(citizen);

        UserProfileResponse response = userService.updateProfile(
                "ana@example.com",
                updateRequest("3548500")
        );

        assertThat(response.city().name()).isEqualTo("Santos");
        verify(pending).cancel();
        verify(notificationRepository).saveAll(List.of(pending));
    }

    private City city(long id, String name, String ibgeCode) {
        City city = new City(name, "SP", ibgeCode);
        ReflectionTestUtils.setField(city, "id", id);
        return city;
    }

    private User fullCitizen(City city) {
        User user = new User(
                "Ana", "ana@example.com", "hash", UserRole.CITIZEN, city,
                "52998224725", "Avenida Brasil", "100", "Apto 12",
                "Boqueirao", "11700000"
        );
        ReflectionTestUtils.setField(user, "id", 20L);
        user.enableWhatsappNotifications("+5513999999999", java.time.Instant.now());
        return user;
    }

    private UpdateUserProfileRequest updateRequest(String cityIbgeCode) {
        return new UpdateUserProfileRequest(
                " Ana Atualizada ", "111.444.777-35", "(13) 98888-7777", true,
                " Rua Nova ", "200", " ", "Centro", "11701-000", cityIbgeCode
        );
    }

    private WhatsappCloudApiProperties properties(boolean enabled) {
        return new WhatsappCloudApiProperties(
                enabled, "https://graph.facebook.com", "v23.0", "", "",
                "cidade_em_foco_alerta_climatico", "pt_BR", "America/Sao_Paulo", 3, 20
        );
    }
}
