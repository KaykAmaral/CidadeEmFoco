package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
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
                .andExpect(jsonPath("$.notificationsEnabled").value(true));
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
                Instant.parse("2026-09-19T12:00:00Z")
        );
    }
}
