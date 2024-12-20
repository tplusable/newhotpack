package com.table.hotpack;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotpackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotpackApplication.class, args);

		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("JWT_ISSUER", dotenv.get("JWT_ISSUER"));
		System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
	}

}
