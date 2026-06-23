package com.tarakki.boardtask.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundpiy8oy8y9Exception(Long boardId) {
        super("Board not found with id: " + boardId);
    }
}

