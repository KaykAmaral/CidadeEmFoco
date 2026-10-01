package br.com.cidadeemfoco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.whatsapp.cloud-api")
public record WhatsappCloudApiProperties(
        boolean enabled,
        String baseUrl,
        String apiVersion,
        String phoneNumberId,
        String accessToken,
        String templateName,
        String templateLanguage,
        String timezone,
        int maxAttempts,
        int batchSize
) {
}
