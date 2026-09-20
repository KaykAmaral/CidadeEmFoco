package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.exception.WhatsappDeliveryException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.whatsapp.cloud-api.enabled", havingValue = "true")
public class WhatsappCloudApiSender implements WhatsappMessageSender {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final WhatsappCloudApiProperties properties;
    private final RestClient restClient;
    private final ZoneId timezone;

    public WhatsappCloudApiSender(WhatsappCloudApiProperties properties) {
        this(properties, RestClient.builder());
    }

    WhatsappCloudApiSender(WhatsappCloudApiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        validateConfiguration(properties);
        this.timezone = ZoneId.of(properties.timezone());
        this.restClient = restClientBuilder
                .baseUrl(removeTrailingSlash(properties.baseUrl()))
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .build();
    }

    @Override
    public String send(WhatsappNotification notification) {
        try {
            CloudApiResponse response = restClient.post()
                    .uri("/{version}/{phoneNumberId}/messages",
                            properties.apiVersion(), properties.phoneNumberId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequest(notification))
                    .retrieve()
                    .body(CloudApiResponse.class);

            if (response == null || response.messages() == null || response.messages().isEmpty()
                    || isBlank(response.messages().getFirst().id())) {
                throw new WhatsappDeliveryException("A Meta aceitou a requisicao sem informar o ID da mensagem");
            }
            return response.messages().getFirst().id();
        } catch (RestClientResponseException exception) {
            throw new WhatsappDeliveryException(
                    "WhatsApp Cloud API respondeu HTTP " + exception.getStatusCode().value()
                            + ": " + limit(exception.getResponseBodyAsString(), 700),
                    exception
            );
        } catch (RestClientException exception) {
            throw new WhatsappDeliveryException("Nao foi possivel acessar a WhatsApp Cloud API", exception);
        }
    }

    private CloudApiRequest buildRequest(WhatsappNotification notification) {
        List<TemplateParameter> parameters = List.of(
                new TemplateParameter("text", notification.getAlertTitle()),
                new TemplateParameter("text", readable(notification.getAlertType().name())),
                new TemplateParameter("text", readable(notification.getAlertSeverity().name())),
                new TemplateParameter("text", notification.getAlertDescription()),
                new TemplateParameter("text", DATE_FORMAT.format(notification.getAlertStartAt().atZone(timezone))),
                new TemplateParameter("text", DATE_FORMAT.format(notification.getAlertEndAt().atZone(timezone)))
        );
        Template template = new Template(
                properties.templateName(),
                new TemplateLanguage(properties.templateLanguage()),
                List.of(new TemplateComponent("body", parameters))
        );
        return new CloudApiRequest(
                "whatsapp",
                "individual",
                notification.getRecipientPhone().replaceAll("\\D", ""),
                "template",
                template
        );
    }

    private static void validateConfiguration(WhatsappCloudApiProperties properties) {
        if (isBlank(properties.baseUrl()) || isBlank(properties.apiVersion())
                || isBlank(properties.phoneNumberId()) || isBlank(properties.accessToken())
                || isBlank(properties.templateName()) || isBlank(properties.templateLanguage())
                || isBlank(properties.timezone())) {
            throw new IllegalStateException(
                    "Preencha todas as configuracoes da WhatsApp Cloud API antes de habilitar o envio"
            );
        }
        if (properties.maxAttempts() < 1 || properties.batchSize() < 1) {
            throw new IllegalStateException("WHATSAPP_MAX_ATTEMPTS e WHATSAPP_BATCH_SIZE devem ser maiores que zero");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String readable(String value) {
        return value.replace('_', ' ');
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "resposta sem detalhes";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record CloudApiRequest(
            String messaging_product,
            String recipient_type,
            String to,
            String type,
            Template template
    ) {
    }

    private record Template(String name, TemplateLanguage language, List<TemplateComponent> components) {
    }

    private record TemplateLanguage(String code) {
    }

    private record TemplateComponent(String type, List<TemplateParameter> parameters) {
    }

    private record TemplateParameter(String type, String text) {
    }

    private record CloudApiResponse(List<CloudApiMessage> messages) {
    }

    private record CloudApiMessage(String id) {
    }
}
