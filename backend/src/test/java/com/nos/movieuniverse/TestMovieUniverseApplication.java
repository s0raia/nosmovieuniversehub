package com.nos.movieuniverse;

import org.springframework.boot.SpringApplication;

public class TestMovieUniverseApplication {

	public static void main(String[] args) {
		SpringApplication.from(MovieUniverseApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
