package com.tarakki.boardtask.exception;

public class PositionAlreadyExistsException extends RuntimeException {
    public PositionAlreadyExistsException(Integer position, Long boardId) {
        super("Position " + position + " already exists for board id: " + boardId);
    }
}
