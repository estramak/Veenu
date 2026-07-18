package estramak.Veenu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "estramak.Veenu", "model", "repositories", "security", "services", "controllers", "dtos"
})
@EntityScan(basePackages = "model")
@EnableJpaRepositories(basePackages = "repositories")
public class VeenuApplication {
	public static void main(String[] args) {
		SpringApplication.run(VeenuApplication.class, args);
	}

}
