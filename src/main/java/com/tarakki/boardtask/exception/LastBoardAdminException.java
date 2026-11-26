package com.tarakki.boardtask.exception;

public class LastBoardAdminException extends RuntimeException {
    public LastBoardAdminException(Long boardId) {
        super("Cannot remove the last admin of board with id: " + boardId);
    }
}
