package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
