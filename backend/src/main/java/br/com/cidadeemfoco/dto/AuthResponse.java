package br.com.cidadeemfoco.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
