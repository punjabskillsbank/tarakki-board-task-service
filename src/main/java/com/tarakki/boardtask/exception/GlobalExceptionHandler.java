package com.tarakki.boardtask.exception;

import com.tarakki.common.exceptionHandling.BoardIdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoardIdNotFoundException.class)
    public ResponseEntity<String> handleBoardIdNotFound(BoardIdNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
