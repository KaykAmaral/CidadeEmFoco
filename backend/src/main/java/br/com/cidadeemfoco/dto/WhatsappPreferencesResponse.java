package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.User;

import java.time.Instant;

public record WhatsappPreferencesResponse(
        String phoneNumber,
        boolean notificationsEnabled,
        Instant consentAt
) {

    public static WhatsappPreferencesResponse from(User user) {
        return new WhatsappPreferencesResponse(
                user.getWhatsappPhone(),
                user.isWhatsappNotificationsEnabled(),
                user.getWhatsappConsentAt()
        );
    }
}
