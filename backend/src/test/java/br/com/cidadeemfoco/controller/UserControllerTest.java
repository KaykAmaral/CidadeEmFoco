package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.dto.AddressResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.dto.UpdateUserProfileRequest;
import br.com.cidadeemfoco.dto.UserProfileResponse;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                "ana@example.com", null, java.util.List.of()
        );
    }

    @Test
    void shouldReturnWhatsappPreferences() throws Exception {
        when(userService.findWhatsappPreferences("ana@example.com"))
                .thenReturn(response());

        mockMvc.perform(get("/api/users/me/whatsapp").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+5513999999999"))
                .andExpect(jsonPath("$.notificationsEnabled").value(true))
                .andExpect(jsonPath("$.deliveryEnabled").value(false));
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() throws Exception {
        when(userService.findProfile("ana@example.com")).thenReturn(profileResponse());

        mockMvc.perform(get("/api/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.city.ibgeCode").value("3541000"));
    }

    @Test
    void shouldUpdateAuthenticatedUserProfile() throws Exception {
        when(userService.updateProfile(
                org.mockito.ArgumentMatchers.eq("ana@example.com"),
                any(UpdateUserProfileRequest.class)
        )).thenReturn(profileResponse());

        mockMvc.perform(put("/api/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "cpf": "529.982.247-25",
                                  "whatsappPhone": "(13) 99999-9999",
                                  "whatsappConsentGiven": true,
                                  "street": "Avenida Brasil",
                                  "addressNumber": "100",
                                  "addressComplement": "Apto 12",
                                  "neighborhood": "Boqueirao",
                                  "postalCode": "11700-000",
                                  "cityIbgeCode": "3541000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void shouldEnableWhatsappNotificationsWithExplicitConsent() throws Exception {
        when(userService.enableWhatsappNotifications(
                org.mockito.ArgumentMatchers.eq("ana@example.com"),
                any(UpdateWhatsappPreferencesRequest.class)
        )).thenReturn(response());

        mockMvc.perform(put("/api/users/me/whatsapp")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "(13) 99999-9999",
                                  "consentGiven": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationsEnabled").value(true));
    }

    @Test
    void shouldRequireExplicitConsent() throws Exception {
        mockMvc.perform(put("/api/users/me/whatsapp")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "(13) 99999-9999",
                                  "consentGiven": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.consentGiven").exists());
    }

    @Test
    void shouldDisableWhatsappNotifications() throws Exception {
        mockMvc.perform(delete("/api/users/me/whatsapp").principal(authentication))
                .andExpect(status().isNoContent());

        verify(userService).disableWhatsappNotifications("ana@example.com");
    }

    private WhatsappPreferencesResponse response() {
        return new WhatsappPreferencesResponse(
                "+5513999999999",
                true,
                Instant.parse("2026-09-19T12:00:00Z"),
                false
        );
    }

    private UserProfileResponse profileResponse() {
        return new UserProfileResponse(
                1L,
                "Ana",
                "ana@example.com",
                "52998224725",
                "+5513999999999",
                true,
                Instant.parse("2026-10-23T12:00:00Z"),
                new AddressResponse("Avenida Brasil", "100", "Apto 12", "Boqueirao", "11700000"),
                new CityResponse(1L, "Praia Grande", "SP", "3541000"),
                Instant.parse("2026-10-23T12:00:00Z")
        );
    }
}
