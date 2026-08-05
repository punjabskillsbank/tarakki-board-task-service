package com.tarakki.boardtask.exception;

import java.util.UUID;

public class MemberAlreadyOnBoardException extends RuntimeException {
    public MemberAlreadyOnBoardException(UUID memberId, Long boardId) {
        super("Member " + memberId + " is already a member of board " + boardId);
    }
}
