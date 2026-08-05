package com.tarakki.boardtask.exception;

public class OrgMemberNotFoundException extends RuntimeException {
    public OrgMemberNotFoundException(Long orgMemberId, Long orgId) {
        super("Org member " + orgMemberId + " not found in organization " + orgId);
    }
}
