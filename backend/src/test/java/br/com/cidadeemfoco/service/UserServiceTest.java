package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private User citizen;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
    }

    @Test
    void shouldReturnCurrentWhatsappPreferences() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));

        WhatsappPreferencesResponse response = userService.findWhatsappPreferences(" ANA@EXAMPLE.COM ");

        assertThat(response.notificationsEnabled()).isFalse();
        assertThat(response.phoneNumber()).isNull();
        assertThat(response.consentAt()).isNull();
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
}
