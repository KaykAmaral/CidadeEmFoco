package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WhatsappCloudApiSenderTest {

    @Test
    void shouldSendApprovedTemplateAndReturnMetaMessageId() {
        WhatsappCloudApiProperties properties = new WhatsappCloudApiProperties(
                true, "https://graph.facebook.com", "v23.0", "123456", "secret-token",
                "cidade_em_foco_alerta_climatico", "pt_BR", "America/Sao_Paulo", 3, 20
        );
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WhatsappCloudApiSender sender = new WhatsappCloudApiSender(properties, builder);

        server.expect(once(), requestTo("https://graph.facebook.com/v23.0/123456/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret-token"))
                .andExpect(jsonPath("$.messaging_product").value("whatsapp"))
                .andExpect(jsonPath("$.to").value("5513999999999"))
                .andExpect(jsonPath("$.template.name").value("cidade_em_foco_alerta_climatico"))
                .andExpect(jsonPath("$.template.language.code").value("pt_BR"))
                .andExpect(jsonPath("$.template.components[0].parameters.length()").value(6))
                .andRespond(withSuccess(
                        "{\"messages\":[{\"id\":\"wamid.abc123\"}]}",
                        MediaType.APPLICATION_JSON
                ));

        String messageId = sender.send(notification());

        assertThat(messageId).isEqualTo("wamid.abc123");
        server.verify();
    }

    private WhatsappNotification notification() {
        User user = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        user.enableWhatsappNotifications("+5513999999999", Instant.now());
        ClimateAlert alert = new ClimateAlert(
                "Alerta de chuva",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Possibilidade de chuva intensa",
                Instant.parse("2026-09-20T12:00:00Z"),
                Instant.parse("2026-09-20T18:00:00Z")
        );
        return new WhatsappNotification(alert, user);
    }
}
