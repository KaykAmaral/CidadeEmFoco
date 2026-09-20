package br.com.cidadeemfoco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CidadeEmFocoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CidadeEmFocoApplication.class, args);
	}

}
