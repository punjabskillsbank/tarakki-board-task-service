package com.tarakki.boardtask.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static com.tarakki.boardtask.util.AuditTestDataFactory.AUDIT_MESSAGE;
import static com.tarakki.boardtask.util.AuditTestDataFactory.AUDIT_TOPIC;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditKafkaProducerTest {

    @Test
    void shouldSendMessageToConfiguredTopic() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(AUDIT_TOPIC, AUDIT_MESSAGE))
                .thenReturn(CompletableFuture.completedFuture(null));
        AuditKafkaProducer producer = new AuditKafkaProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "topic", AUDIT_TOPIC);

        producer.sendAuditLog(AUDIT_MESSAGE);

        verify(kafkaTemplate).send(AUDIT_TOPIC, AUDIT_MESSAGE);
    }
}
