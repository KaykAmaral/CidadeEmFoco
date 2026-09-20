package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WhatsappNotificationRepository extends JpaRepository<WhatsappNotification, Long> {

    boolean existsByAlertIdAndUserId(Long alertId, Long userId);

    List<WhatsappNotification> findByAlertIdInAndStatusIn(
            Collection<Long> alertIds,
            Collection<WhatsappNotificationStatus> statuses
    );

    List<WhatsappNotification> findByStatus(WhatsappNotificationStatus status, Sort sort);

    List<WhatsappNotification> findByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
            Collection<WhatsappNotificationStatus> statuses,
            int maxAttempts,
            Pageable pageable
    );
}
