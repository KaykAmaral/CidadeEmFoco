package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    List<PasswordResetToken> findByUserIdAndUsedAtIsNull(Long userId);
}
