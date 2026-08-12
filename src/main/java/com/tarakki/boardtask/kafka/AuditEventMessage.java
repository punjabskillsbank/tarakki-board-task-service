package com.tarakki.boardtask.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditEventMessage(

        @JsonProperty("service_name")
        String serviceName,

        @JsonProperty("entity_name")
        String entityName,

        @JsonProperty("entity_id")
        String entityId,

        @JsonProperty("event_name")
        String eventName,

        @JsonProperty("performed_by")
        String performedBy,

        @JsonProperty("old_value")
        Object oldValue,

        @JsonProperty("new_value")
        Object newValue,

        @JsonProperty("event_time")
        String eventTime
) {}
