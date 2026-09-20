package br.com.cidadeemfoco;

import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"debug=false",
		"app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class CidadeEmFocoApplicationTests {

	@MockitoBean
	private ClimateAlertRepository climateAlertRepository;

	@MockitoBean
	private OccurrenceRepository occurrenceRepository;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private WhatsappNotificationRepository whatsappNotificationRepository;

	@Test
	void contextLoads() {
	}

}
