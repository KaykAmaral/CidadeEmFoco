package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdminProvisioningService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProvisioningService(
            UserRepository userRepository,
            CityRepository cityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean provision(String name, String email, String password, String cityIbgeCode) {
        String normalizedName = requireText(name, "ADMIN_BOOTSTRAP_NAME");
        String normalizedEmail = requireText(email, "ADMIN_BOOTSTRAP_EMAIL").toLowerCase(Locale.ROOT);
        String normalizedPassword = requireText(password, "ADMIN_BOOTSTRAP_PASSWORD");
        String normalizedCityCode = requireText(cityIbgeCode, "ADMIN_BOOTSTRAP_CITY_IBGE_CODE");

        if (normalizedName.length() > 100) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_NAME deve ter no maximo 100 caracteres");
        }
        if (normalizedEmail.length() > 254 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_EMAIL deve ser um email valido");
        }
        if (normalizedPassword.length() < 8) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD deve ter ao menos 8 caracteres");
        }
        if (!normalizedCityCode.matches("\\d{7}")) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_CITY_IBGE_CODE deve conter 7 digitos");
        }

        City city = cityRepository.findByIbgeCodeAndActiveTrue(normalizedCityCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Nao existe cidade ativa para ADMIN_BOOTSTRAP_CITY_IBGE_CODE"
                ));

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existing -> validateExisting(existing, city))
                .orElseGet(() -> create(normalizedName, normalizedEmail, normalizedPassword, city));
    }

    private boolean validateExisting(User existing, City city) {
        if (existing.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_EMAIL ja pertence a um cidadao");
        }
        if (!existing.getCity().getIbgeCode().equals(city.getIbgeCode())) {
            throw new IllegalStateException("O administrador existente pertence a outra cidade");
        }
        return false;
    }

    private boolean create(String name, String email, String password, City city) {
        userRepository.save(new User(
                name,
                email,
                passwordEncoder.encode(password),
                UserRole.ADMIN,
                city
        ));
        return true;
    }

    private String requireText(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variableName + " e obrigatoria quando o provisionamento esta ativo");
        }
        return value.trim();
    }
}
