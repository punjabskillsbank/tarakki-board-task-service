package com.tarakki.boardtask.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${audit.logging.topic}")
    private String topic;

    public void sendAuditLog(String message) {
        kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent audit message to topic '{}': {}", topic, message);
                    } else {
                        log.error("Failed to send audit message to topic '{}': {}", topic, message, ex);
                    }
                });
    }
}
