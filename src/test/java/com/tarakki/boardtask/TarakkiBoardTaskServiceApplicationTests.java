package com.tarakki.boardtask;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TarakkiBoardTaskServiceApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, String> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
