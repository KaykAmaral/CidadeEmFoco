package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByWhatsappPhone(String whatsappPhone);

    boolean existsByEmailIgnoreCase(String email);
}
