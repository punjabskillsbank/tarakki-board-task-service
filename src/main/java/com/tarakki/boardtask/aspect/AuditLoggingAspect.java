package com.tarakki.boardtask.aspect;

import tools.jackson.databind.ObjectMapper;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.event.AuditEventMessage;
import com.tarakki.boardtask.kafka.AuditKafkaProducer;
import com.tarakki.boardtask.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingAspect {

    private static final String SERVICE_NAME = "board-task-service";
    private static final String MEMBER_ID_HEADER = "memberID";

    private final AuditKafkaProducer auditKafkaProducer;
    private final ObjectMapper objectMapper;
    private final BoardRepository boardRepository;

    @Around("execution(* com.tarakki.boardtask.controller.BoardController.deleteBoard(..)) && args(boardId)")
    public Object logBoardDeletion(ProceedingJoinPoint joinPoint, Long boardId) throws Throwable {
        String oldValue = null;
        Board existingBoard = boardRepository.findById(boardId).orElse(null);
        if (existingBoard != null) {
            oldValue = objectMapper.writeValueAsString(existingBoard);
        }

        String performedBy = resolvePerformedBy();

        Object result = joinPoint.proceed();

        try {
            AuditEventMessage message = new AuditEventMessage(
                    SERVICE_NAME,
                    "BOARD",
                    boardId.toString(),
                    "BOARD_DELETED",
                    performedBy,
                    oldValue,
                    null,
                    Instant.now().toString()
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            auditKafkaProducer.sendAuditLog(jsonMessage);
        } catch (Exception e) {
            log.error("Failed to generate audit log for board deletion: {}", boardId, e);
        }

        return result;
    }

    private String resolvePerformedBy() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String memberId = request.getHeader(MEMBER_ID_HEADER);
                if (memberId != null && !memberId.isBlank()) {
                    return memberId;
                }
            }
        } catch (Exception e) {
            log.warn("Could not resolve member ID from request header", e);
        }
        return "SYSTEM";
    }
}
