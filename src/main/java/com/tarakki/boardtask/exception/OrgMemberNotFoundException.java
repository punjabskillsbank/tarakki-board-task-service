package com.tarakki.boardtask.exception;

import java.util.UUID;

public class OrgMemberNotFoundException extends RuntimeException {
    public OrgMemberNotFoundException(Long orgMemberId, Long orgId) {
        super("Org member " + orgMemberId + " not found in organization " + orgId);
    }

    public OrgMemberNotFoundException(UUID memberId, Long orgId) {
        super("Member " + memberId + " is not a member of organization " + orgId);
    }
}
