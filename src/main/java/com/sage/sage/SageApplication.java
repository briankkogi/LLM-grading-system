package com.sage.sage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SageApplication {

	public static void main(String[] args) {
            System.setProperty("jna.library.path", "/opt/homebrew/lib");
		SpringApplication.run(SageApplication.class, args);
	}

}
