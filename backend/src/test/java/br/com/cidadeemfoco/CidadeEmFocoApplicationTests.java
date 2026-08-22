package br.com.cidadeemfoco;

import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"debug=false",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class CidadeEmFocoApplicationTests {

	@MockitoBean
	private OccurrenceRepository occurrenceRepository;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
