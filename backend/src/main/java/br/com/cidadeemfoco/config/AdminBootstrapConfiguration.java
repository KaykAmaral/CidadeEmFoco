package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.service.AdminProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
@ConditionalOnProperty(name = "app.admin-bootstrap.enabled", havingValue = "true")
public class AdminBootstrapConfiguration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapConfiguration.class);

    private final AdminProvisioningService provisioningService;
    private final String name;
    private final String email;
    private final String password;
    private final String cityIbgeCode;

    public AdminBootstrapConfiguration(
            AdminProvisioningService provisioningService,
            @Value("${app.admin-bootstrap.name:}") String name,
            @Value("${app.admin-bootstrap.email:}") String email,
            @Value("${app.admin-bootstrap.password:}") String password,
            @Value("${app.admin-bootstrap.city-ibge-code:}") String cityIbgeCode
    ) {
        this.provisioningService = provisioningService;
        this.name = name;
        this.email = email;
        this.password = password;
        this.cityIbgeCode = cityIbgeCode;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean created = provisioningService.provision(name, email, password, cityIbgeCode);
        if (created) {
            LOGGER.info("Administrador inicial criado para a cidade de codigo IBGE {}", cityIbgeCode);
        } else {
            LOGGER.info("Administrador inicial ja estava provisionado para a cidade informada");
        }
    }
}
