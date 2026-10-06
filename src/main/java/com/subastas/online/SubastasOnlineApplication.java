package com.subastas.online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubastasOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubastasOnlineApplication.class, args);
	}

}
