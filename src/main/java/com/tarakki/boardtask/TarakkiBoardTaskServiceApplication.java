package com.tarakki.boardtask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.tarakki.common.entity")
public class TarakkiBoardTaskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarakkiBoardTaskServiceApplication.class, args);
	}

}
