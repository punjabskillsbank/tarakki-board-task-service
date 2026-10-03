package com.tarakki.boardtask.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(Long groupId) {
        super("Group not found: "+ groupId);
    }
}
