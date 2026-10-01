package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.User;

import java.time.Instant;

public record WhatsappPreferencesResponse(
        String phoneNumber,
        boolean notificationsEnabled,
        Instant consentAt,
        boolean deliveryEnabled
) {

    public static WhatsappPreferencesResponse from(User user, boolean deliveryEnabled) {
        return new WhatsappPreferencesResponse(
                user.getWhatsappPhone(),
                user.isWhatsappNotificationsEnabled(),
                user.getWhatsappConsentAt(),
                deliveryEnabled
        );
    }
}
