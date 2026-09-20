package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.UpdateWhatsappPreferencesRequest;
import br.com.cidadeemfoco.dto.WhatsappPreferencesResponse;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public WhatsappPreferencesResponse findWhatsappPreferences(String email) {
        return WhatsappPreferencesResponse.from(findUser(email));
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
        String phone = normalizePhone(request.phoneNumber());
        userRepository.findByWhatsappPhone(phone)
                .filter(existingUser -> !existingUser.getEmail().equalsIgnoreCase(user.getEmail()))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException("Este numero de WhatsApp ja esta cadastrado");
                });

        user.enableWhatsappNotifications(phone, Instant.now());
        return WhatsappPreferencesResponse.from(userRepository.save(user));
    }

    @Transactional
    public void disableWhatsappNotifications(String email) {
        User user = findUser(email);
        user.disableWhatsappNotifications();
        userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private String normalizePhone(String value) {
        String trimmed = value.trim();
        String digits = trimmed.replaceAll("\\D", "");
        String normalized;

        if (trimmed.startsWith("+")) {
            normalized = "+" + digits;
        } else if ((digits.length() == 12 || digits.length() == 13) && digits.startsWith("55")) {
            normalized = "+" + digits;
        } else if (digits.length() == 10 || digits.length() == 11) {
            normalized = "+55" + digits;
        } else {
            throw new BusinessRuleException("Informe um numero de WhatsApp valido com DDD");
        }

        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new BusinessRuleException("Informe um numero de WhatsApp valido no formato internacional");
        }
        return normalized;
    }
}
