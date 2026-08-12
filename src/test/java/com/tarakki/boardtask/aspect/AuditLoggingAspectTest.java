package com.tarakki.boardtask.aspect;

import com.tarakki.boardtask.controller.BoardController;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.kafka.AuditKafkaProducer;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.service.BoardService;
import tools.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

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
    private BoardRepository boardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditLoggingAspect unitAspect;
    private AuditKafkaProducer mockProducer;
    private BoardRepository mockBoardRepository;

    @BeforeEach
    void setUp() {
        RequestContextHolder.resetRequestAttributes();
        mockProducer = mock(AuditKafkaProducer.class);
        mockBoardRepository = mock(BoardRepository.class);
        unitAspect = new AuditLoggingAspect(mockProducer, objectMapper, mockBoardRepository);
    }

    @Test
    void shouldPopulateOldValueFromDbOnDelete() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        Board existingBoard = createAuditBoard(boardId);

        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.of(existingBoard));

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logBoardDeletion(joinPoint, boardId);

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

        // old_value should contain the board data fetched from DB
        assertNotNull(payload.get("old_value"));
        String oldValueJson = (String) payload.get("old_value");
        Map<?, ?> oldValueMap = objectMapper.readValue(oldValueJson, Map.class);
        assertEquals(AUDIT_BOARD_NAME, oldValueMap.get("boardName"));
        assertEquals(AUDIT_BOARD_DESCRIPTION, oldValueMap.get("boardDesc"));

        // new_value should be null for delete
        assertNull(payload.get("new_value"));
        assertNotNull(payload.get("event_time"));
    }

    @Test
    void shouldHandleMissingBoardGracefully() throws Throwable {
        Long boardId = MISSING_BOARD_ID;
        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logBoardDeletion(joinPoint, boardId);

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

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(existingBoard));
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
        assertEquals(EVENT_NAME, payload.get("event_name"));
        assertEquals(SYSTEM_ACTOR, payload.get("performed_by"));

        // old_value should be populated from the mocked DB lookup
        assertNotNull(payload.get("old_value"));
        assertNull(payload.get("new_value"));
        assertNotNull(payload.get("event_time"));
    }

    @Test
    void shouldUseMemberIdFromRequestHeader() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestWithMemberId()));
        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logBoardDeletion(joinPoint, boardId);

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
        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.empty());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(null);

        unitAspect.logBoardDeletion(joinPoint, boardId);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockProducer).sendAuditLog(messageCaptor.capture());
        Map<?, ?> payload = objectMapper.readValue(messageCaptor.getValue(), Map.class);
        assertEquals(SYSTEM_ACTOR, payload.get("performed_by"));
    }

    @Test
    void shouldStopDeletionWhenOldBoardLookupFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(mockBoardRepository.findById(boardId)).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> unitAspect.logBoardDeletion(joinPoint, boardId));

        verify(joinPoint, never()).proceed();
        verifyNoInteractions(mockProducer);
    }

    @Test
    void shouldStopDeletionWhenOldBoardSerializationFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        AuditLoggingAspect aspect = new AuditLoggingAspect(mockProducer, failingObjectMapper, mockBoardRepository);
        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.of(createAuditBoard(boardId)));
        when(failingObjectMapper.writeValueAsString(any())).thenThrow(new IllegalStateException());
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        assertThrows(IllegalStateException.class, () -> aspect.logBoardDeletion(joinPoint, boardId));

        verify(joinPoint, never()).proceed();
        verifyNoInteractions(mockProducer);
    }

    @Test
    void shouldNotFailDeletionWhenAuditPublishingFails() throws Throwable {
        Long boardId = AUDIT_BOARD_ID;
        Object deletionResult = new Object();
        when(mockBoardRepository.findById(boardId)).thenReturn(Optional.empty());
        doThrow(new IllegalStateException()).when(mockProducer).sendAuditLog(anyString());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(deletionResult);

        assertSame(deletionResult, unitAspect.logBoardDeletion(joinPoint, boardId));

        verify(joinPoint).proceed();
        verify(mockProducer).sendAuditLog(anyString());
    }

    private MockHttpServletRequest requestWithMemberId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(MEMBER_ID_HEADER, MEMBER_ID);
        return request;
    }
}
