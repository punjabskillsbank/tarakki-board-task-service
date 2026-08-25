package com.tarakki.boardtask.aspect;

import com.tarakki.boardtask.annotation.Auditable;
import com.tarakki.boardtask.controller.BoardController;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.kafka.AuditKafkaProducer;
import com.tarakki.boardtask.service.BoardService;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;

import static com.tarakki.boardtask.util.BoardTestDataFactory.*;
import static com.tarakki.boardtask.util.AuditTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = {
        BoardController.class,
        AuditLoggingAspect.class,
        org.springframework.boot.autoconfigure.aop.AopAutoConfiguration.class,
        org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration.class
    },
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    }
)
class AuditLoggingAspectTest {

    @Autowired
    private BoardController boardController;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private AuditKafkaProducer auditKafkaProducer;

    @MockitoBean
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditLoggingAspect unitAspect;
    private AuditKafkaProducer mockProducer;
    private EntityManager mockEntityManager;
    private Auditable mockAuditable;

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
        mockProducer = mock(AuditKafkaProducer.class);
        mockEntityManager = mock(EntityManager.class);
        unitAspect = new AuditLoggingAspect(mockProducer, objectMapper, mockEntityManager);
        
        mockAuditable = mock(Auditable.class);
        when(mockAuditable.eventName()).thenReturn("BOARD_DELETED");
        when(mockAuditable.entityName()).thenReturn("BOARD");
        doReturn(Board.class).when(mockAuditable).entityClass();
        when(mockAuditable.entityIdArgSpel()).thenReturn("#boardId");
        when(mockAuditable.entityIdResultSpel()).thenReturn("");
    }
    
    private ProceedingJoinPoint mockJoinPointWithArgs(Object... args) throws NoSuchMethodException {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = BoardController.class.getMethod("deleteBoard", Long.class);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    @Test
    void shouldPopulateOldValueFromDbOnDelete() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        Board existingBoard = createAuditBoard(boardId);

        when(mockEntityManager.find(Board.class, boardId)).thenReturn(existingBoard).thenReturn(null);

        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logAuditActivity(joinPoint, mockAuditable);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockProducer).sendAuditLog(messageCaptor.capture());

        String capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);

        Map<?, ?> payload = objectMapper.readValue(capturedMessage, Map.class);
        assertEquals(SERVICE_NAME, payload.get("service_name"));
        assertEquals(ENTITY_NAME, payload.get("entity_name"));
        assertEquals(boardId.toString(),   payload.get("entity_id"));
        assertEquals(EVENT_NAME, payload.get("event_name"));
        assertEquals(SYSTEM_ACTOR, payload.get("performed_by"));

        assertNotNull(payload.get("old_value"));
        String oldValueJson = (String) payload.get("old_value");
        Map<?, ?> oldValueMap = objectMapper.readValue(oldValueJson, Map.class);
        assertEquals(AUDIT_BOARD_NAME, oldValueMap.get("boardName"));
        assertEquals(AUDIT_BOARD_DESCRIPTION, oldValueMap.get("boardDesc"));

        assertNull(payload.get("new_value"));
        assertNotNull(payload.get("event_time"));
    }

    @Test
    void shouldHandleMissingBoardGracefully() throws Throwable {
        Long boardId = MISSING_BOARD_ID;
        when(mockEntityManager.find(Board.class, boardId)).thenReturn(null);

        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logAuditActivity(joinPoint, mockAuditable);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockProducer).sendAuditLog(messageCaptor.capture());

        Map<?, ?> payload = objectMapper.readValue(messageCaptor.getValue(), Map.class);
        assertNull(payload.get("old_value"));
        assertNull(payload.get("new_value"));
    }

    @Test
    void shouldInterceptDeleteBoardAndSendAuditLogWithOldValue() throws Exception {
        Long boardId = AUDIT_BOARD_ID_ALT;
        Board existingBoard = createAlternateAuditBoard(boardId);

        when(entityManager.find(Board.class, boardId)).thenReturn(existingBoard).thenReturn(null);
        doNothing().when(boardService).deleteBoard(boardId);

        boardController.deleteBoard(boardId);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditKafkaProducer).sendAuditLog(messageCaptor.capture());

        String capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);

        Map<?, ?> payload = objectMapper.readValue(capturedMessage, Map.class);
        assertEquals(SERVICE_NAME, payload.get("service_name"));
        assertEquals(ENTITY_NAME, payload.get("entity_name"));
        assertEquals(boardId.toString(),   payload.get("entity_id"));
        assertEquals("BOARD_DELETED", payload.get("event_name"));
        assertEquals(SYSTEM_ACTOR, payload.get("performed_by"));

        assertNotNull(payload.get("old_value"));
        assertNull(payload.get("new_value"));
        assertNotNull(payload.get("event_time"));
    }

    @Test
    void shouldUseMemberIdFromRequestHeader() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestWithMemberId()));
        when(mockEntityManager.find(Board.class, boardId)).thenReturn(null);

        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logAuditActivity(joinPoint, mockAuditable);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockProducer).sendAuditLog(messageCaptor.capture());
        Map<?, ?> payload = objectMapper.readValue(messageCaptor.getValue(), Map.class);
        assertEquals(MEMBER_ID, payload.get("performed_by"));
    }

    @Test
    void shouldUseSystemForBlankMemberId() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MEMBER_ID_HEADER, " ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(mockEntityManager.find(Board.class, boardId)).thenReturn(null);

        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logAuditActivity(joinPoint, mockAuditable);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockProducer).sendAuditLog(messageCaptor.capture());
        Map<?, ?> payload = objectMapper.readValue(messageCaptor.getValue(), Map.class);
        assertEquals(SYSTEM_ACTOR, payload.get("performed_by"));
    }

    @Test
    void shouldStopDeletionWhenOldBoardLookupFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(mockEntityManager.find(Board.class, boardId)).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> unitAspect.logAuditActivity(joinPoint, mockAuditable));

        verify(joinPoint, never()).proceed();
        verifyNoInteractions(mockProducer);
    }

    @Test
    void shouldStopDeletionWhenOldBoardSerializationFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        AuditLoggingAspect aspect = new AuditLoggingAspect(mockProducer, failingObjectMapper, mockEntityManager);
        when(mockEntityManager.find(Board.class, boardId)).thenReturn(createAuditBoard(boardId));
        when(failingObjectMapper.writeValueAsString(any())).thenThrow(new IllegalStateException());
        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);

        assertThrows(IllegalStateException.class, () -> aspect.logAuditActivity(joinPoint, mockAuditable));

        verify(joinPoint, never()).proceed();
        verifyNoInteractions(mockProducer);
    }

    @Test
    void shouldNotFailDeletionWhenAuditPublishingFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        Object deletionResult = new Object();
        when(mockEntityManager.find(Board.class, boardId)).thenReturn(null);
        doThrow(new IllegalStateException()).when(mockProducer).sendAuditLog(anyString());

        ProceedingJoinPoint joinPoint = mockJoinPointWithArgs(boardId);
        when(joinPoint.proceed()).thenReturn(deletionResult);

        assertSame(deletionResult, unitAspect.logAuditActivity(joinPoint, mockAuditable));

        verify(joinPoint).proceed();
        verify(mockProducer).sendAuditLog(anyString());
    }

    private MockHttpServletRequest requestWithMemberId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MEMBER_ID_HEADER, MEMBER_ID);
        return request;
    }
}
