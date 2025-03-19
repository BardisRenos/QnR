package com.example.qnr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(exclude = {FlywayAutoConfiguration.class})
public class QnrApplication {

	public static void main(String[] args) {
		SpringApplication.run(QnrApplication.class, args);
	}

}
