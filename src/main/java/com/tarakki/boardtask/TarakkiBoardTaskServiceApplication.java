package com.tarakki.boardtask;

import com.tarakki.common.config.CorsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EntityScan({
		"com.tarakki.boardtask.entity",
		"com.tarakki.common.entity"
})@Import(CorsConfig.class)
public class TarakkiBoardTaskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TarakkiBoardTaskServiceApplication.class, args);
	}

}
