package br.com.cidadeemfoco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CidadeEmFocoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CidadeEmFocoApplication.class, args);
	}

}
