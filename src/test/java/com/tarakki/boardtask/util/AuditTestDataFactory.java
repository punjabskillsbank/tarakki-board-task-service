package com.tarakki.boardtask.util;

public final class AuditTestDataFactory {

    public static final String SERVICE_NAME = "board-task-service";
    public static final String ENTITY_NAME = "BOARD";
    public static final String EVENT_NAME = "DELETE_BOARD";
    public static final String SYSTEM_ACTOR = "SYSTEM";
    public static final String MEMBER_ID_HEADER = "memberID";
    public static final String MEMBER_ID = "member-123";
    public static final String AUDIT_TOPIC = "audit-test-topic";
    public static final String AUDIT_MESSAGE = "audit-message";

    private AuditTestDataFactory() {
    }
}
