package com.nos.movieuniverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MovieUniverseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieUniverseApplication.class, args);
	}

}
