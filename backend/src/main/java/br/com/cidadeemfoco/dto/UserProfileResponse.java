package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.User;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String cpf,
        String whatsappPhone,
        boolean whatsappNotificationsEnabled,
        Instant whatsappConsentAt,
        AddressResponse address,
        CityResponse city,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getWhatsappPhone(),
                user.isWhatsappNotificationsEnabled(),
                user.getWhatsappConsentAt(),
                AddressResponse.from(user),
                user.getCity() == null ? null : CityResponse.from(user.getCity()),
                user.getCreatedAt()
        );
    }
}
