package br.com.cidadeemfoco.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WhatsappCloudApiProperties.class)
public class WhatsappCloudApiConfig {
}
