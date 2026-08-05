package com.tarakki.boardtask.exception;

public class OrgMemberNotRegisteredException extends RuntimeException {
    public OrgMemberNotRegisteredException(Long orgMemberId) {
        super("Org member " + orgMemberId + " has no registered account yet");
    }
}
