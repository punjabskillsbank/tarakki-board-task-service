package com.tarakki.boardtask.aspect;

import com.tarakki.boardtask.annotation.Auditable;
import com.tarakki.boardtask.event.AuditEventMessage;
import com.tarakki.boardtask.kafka.AuditKafkaProducer;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
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
    private final EntityManager entityManager;
    
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(auditable)")
    public Object logAuditActivity(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String eventName = auditable.eventName();
        String entityName = auditable.entityName();
        Class<?> entityClass = auditable.entityClass();

        String entityId = null;
        Object parsedIdObj = null;
        String oldValue = null;
        String newValue = null;

        // 1. Evaluate SpEL to get entityId from arguments (before)
        if (!auditable.entityIdArgSpel().isBlank()) {
            parsedIdObj = evaluateSpelAgainstArgs(joinPoint, auditable.entityIdArgSpel());
            if (parsedIdObj != null) {
                entityId = parsedIdObj.toString();
                
                if (entityClass != void.class) {
                    Object oldEntity = entityManager.find(entityClass, parsedIdObj);
                    if (oldEntity != null) {
                        oldValue = objectMapper.writeValueAsString(oldEntity);
                        entityManager.detach(oldEntity);
                    }
                }
            }
        }

        String performedBy = resolvePerformedBy();
        Object result = joinPoint.proceed();

        // 2. If entityId is not in arguments (e.g. update)  try getting from result
        if (entityId == null && !auditable.entityIdResultSpel().isBlank() && result != null) {
            StandardEvaluationContext context = new StandardEvaluationContext(result);
            Object resultIdObj = spelParser.parseExpression(auditable.entityIdResultSpel()).getValue(context);
            if (resultIdObj != null) {
                entityId = resultIdObj.toString();
                parsedIdObj = resultIdObj;
            }
        }

        // 3. Fetch newValue
        if (entityId != null && entityClass != void.class) {
            Object newEntity = entityManager.find(entityClass, parsedIdObj);
            if (newEntity != null) {
                newValue = objectMapper.writeValueAsString(newEntity);
            }
        }

        try {
            AuditEventMessage message = new AuditEventMessage(
                    SERVICE_NAME,
                    entityName,
                    entityId,
                    eventName,
                    performedBy,
                    oldValue,
                    newValue,
                    Instant.now().toString()
            );

            auditKafkaProducer.sendAuditLog(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Failed to generate audit log for event: {}", eventName, e);
        }

        return result;
    }

    private Object evaluateSpelAgainstArgs(ProceedingJoinPoint joinPoint, String spel) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = nameDiscoverer.getParameterNames(signature.getMethod());
            Object[] args = joinPoint.getArgs();

            StandardEvaluationContext context = new StandardEvaluationContext();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }
            return spelParser.parseExpression(spel).getValue(context);
        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL: {}", spel, e);
            return null;
        }
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
