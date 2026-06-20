package com.tarakki.boardtask.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long boardId) {
        super("Board not found with id: " + boardId);
    }
}

