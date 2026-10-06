package com.tarakki.boardtask.exception;

public class BoardMemberNotFoundException extends RuntimeException {

    public BoardMemberNotFoundException(Long boardMemberId) {
        super("Board member not found with id: " + boardMemberId);
    }
}
