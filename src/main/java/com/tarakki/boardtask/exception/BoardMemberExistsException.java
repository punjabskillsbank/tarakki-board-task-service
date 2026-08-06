package com.tarakki.boardtask.exception;

import java.util.UUID;

public class BoardMemberExistsException extends RuntimeException {
    public BoardMemberExistsException(UUID memberId, Long boardId) {
        super("Member " + memberId + " is already a member of board " + boardId);
    }
}
