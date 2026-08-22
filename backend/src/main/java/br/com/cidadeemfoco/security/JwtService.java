package br.com.cidadeemfoco.security;

import br.com.cidadeemfoco.entity.User;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String ISSUER = "cidade-em-foco-api";

    private final SecretKey key;
    private final JwtParser parser;
    private final Duration expiration;

    public JwtService(
            @Value("${app.security.jwt.secret}") String encodedSecret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MINUTES deve ser maior que zero");
        }
        try {
            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "JWT_SECRET deve ser uma chave Base64 valida com pelo menos 32 bytes",
                    exception
            );
        }
        this.parser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build();
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getEmail())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
