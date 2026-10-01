package br.com.cidadeemfoco.security;

import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    void shouldGenerateAndValidateSignedToken() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60);
        User user = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractSubject(token)).isEqualTo("ana@example.com");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }

    @Test
    void shouldRejectTokenWithModifiedSignature() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60);
        User user = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        String token = jwtService.generateToken(user);
        String[] tokenParts = token.split("\\.");
        char replacement = tokenParts[2].charAt(0) == 'a' ? 'b' : 'a';
        tokenParts[2] = replacement + tokenParts[2].substring(1);
        String modifiedToken = String.join(".", tokenParts);

        assertThatThrownBy(() -> jwtService.extractSubject(modifiedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectInvalidSecret() {
        assertThatThrownBy(() -> new JwtService("segredo-invalido", 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
