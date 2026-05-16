package com.tarakki.boardtask.exception;

import com.tarakki.boardtask.controller.BoardController;
import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.exceptionHandling.BoardIdNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBoardIdNotFoundException() {
        Long missingBoardId = 99L;

        ResponseEntity<String> response = globalExceptionHandler.handleBoardIdNotFound(
                new BoardIdNotFoundException(missingBoardId)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Board not found with id: " + missingBoardId, response.getBody());
    }

    @Test
    void shouldHandleValidationExceptionsForDtoFields() throws Exception {
        MethodArgumentNotValidException exception = createValidationException();

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("must not be blank", response.getBody().get("boardName"));
        assertEquals("must not be null", response.getBody().get("orgId"));
    }

    private MethodArgumentNotValidException createValidationException() throws NoSuchMethodException {
        BoardDTO boardDTO = new BoardDTO();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(boardDTO, "boardDTO");
        bindingResult.addError(new FieldError("boardDTO", "boardName", "must not be blank"));
        bindingResult.addError(new FieldError("boardDTO", "orgId", "must not be null"));

        Method method = BoardController.class.getMethod("createBoard", BoardDTO.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }
}
