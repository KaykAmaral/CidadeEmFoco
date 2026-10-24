package br.com.cidadeemfoco.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpRegistrationEmailSender implements RegistrationEmailSender {

    @Override
    public void sendRegistrationCompleted(String recipientEmail, String citizenName) {
        // Envio intencionalmente desativado neste ambiente.
    }
}
