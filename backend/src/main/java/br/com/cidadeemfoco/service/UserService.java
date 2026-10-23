package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.UpdateUserProfileRequest;
import br.com.cidadeemfoco.dto.UserProfileResponse;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import br.com.cidadeemfoco.util.UserDataNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Set<WhatsappNotificationStatus> CHANGEABLE_STATUSES = Set.of(
            WhatsappNotificationStatus.PENDING,
            WhatsappNotificationStatus.FAILED
    );

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final WhatsappNotificationRepository notificationRepository;
    private final boolean deliveryEnabled;

    public UserService(
            UserRepository userRepository,
            CityRepository cityRepository,
            WhatsappNotificationRepository notificationRepository,
            WhatsappCloudApiProperties properties
    ) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryEnabled = properties.enabled();
    }

    public UserProfileResponse findProfile(String email) {
        return UserProfileResponse.from(findUser(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateUserProfileRequest request) {
        User user = findUser(email);
        String cpf = UserDataNormalizer.normalizeCpf(request.cpf());
        userRepository.findByCpf(cpf)
                .filter(existingUser -> !isSameUser(existingUser, user))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException("Ja existe um usuario cadastrado com este CPF");
                });

        String phone = UserDataNormalizer.normalizePhone(request.whatsappPhone());
        userRepository.findByWhatsappPhone(phone)
                .filter(existingUser -> !isSameUser(existingUser, user))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException("Este numero de WhatsApp ja esta cadastrado");
                });

        if (!request.whatsappConsentGiven()) {
            throw new BusinessRuleException("E necessario autorizar o recebimento de alertas pelo WhatsApp");
        }

        var city = cityRepository.findByIbgeCodeAndActiveTrue(request.cityIbgeCode().trim())
                .orElseThrow(() -> new BusinessRuleException(
                        "A cidade informada nao esta disponivel para cadastro"
                ));
        boolean cityChanged = user.getCity() == null
                || !Objects.equals(user.getCity().getIbgeCode(), city.getIbgeCode());

        if (cityChanged) {
            cancelPendingNotifications(user);
        }

        user.updateProfile(
                request.name().trim(),
                cpf,
                request.street().trim(),
                request.addressNumber().trim(),
                normalizeOptional(request.addressComplement()),
                request.neighborhood().trim(),
                UserDataNormalizer.normalizePostalCode(request.postalCode()),
                city
        );
        user.enableWhatsappNotifications(phone, Instant.now());
        User savedUser = userRepository.save(user);

        if (!cityChanged) {
            updatePendingRecipients(savedUser, phone);
        }
        return UserProfileResponse.from(savedUser);
    }

    public WhatsappPreferencesResponse findWhatsappPreferences(String email) {
        return WhatsappPreferencesResponse.from(findUser(email), deliveryEnabled);
    }

    @Transactional
    public WhatsappPreferencesResponse enableWhatsappNotifications(
            String email,
            UpdateWhatsappPreferencesRequest request
    ) {
        if (!request.consentGiven()) {
            throw new BusinessRuleException("E necessario autorizar o recebimento de alertas pelo WhatsApp");
        }
        User user = findUser(email);
        String phone = UserDataNormalizer.normalizePhone(request.phoneNumber());
        userRepository.findByWhatsappPhone(phone)
                .filter(existingUser -> !existingUser.getEmail().equalsIgnoreCase(user.getEmail()))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException("Este numero de WhatsApp ja esta cadastrado");
                });

        user.enableWhatsappNotifications(phone, Instant.now());
        User savedUser = userRepository.save(user);
        updatePendingRecipients(savedUser, phone);
        return WhatsappPreferencesResponse.from(savedUser, deliveryEnabled);
    }

    @Transactional
    public void disableWhatsappNotifications(String email) {
        User user = findUser(email);
        cancelPendingNotifications(user);
        user.disableWhatsappNotifications();
        userRepository.save(user);
    }

    private void updatePendingRecipients(User user, String phone) {
        if (user.getId() == null) {
            return;
        }
        List<WhatsappNotification> notifications = notificationRepository
                .findByUserIdAndStatusIn(user.getId(), CHANGEABLE_STATUSES);
        notifications.forEach(notification -> notification.updateRecipientPhone(phone));
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    private void cancelPendingNotifications(User user) {
        if (user.getId() == null) {
            return;
        }
        List<WhatsappNotification> notifications = notificationRepository
                .findByUserIdAndStatusIn(user.getId(), CHANGEABLE_STATUSES);
        notifications.forEach(WhatsappNotification::cancel);
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private boolean isSameUser(User first, User second) {
        if (first.getId() != null && second.getId() != null) {
            return Objects.equals(first.getId(), second.getId());
        }
        return first.getEmail().equalsIgnoreCase(second.getEmail());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}
