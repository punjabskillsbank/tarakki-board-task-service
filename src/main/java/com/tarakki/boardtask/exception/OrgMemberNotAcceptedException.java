package com.tarakki.boardtask.exception;

public class OrgMemberNotAcceptedException extends RuntimeException {
    public OrgMemberNotAcceptedException(Long orgMemberId, Long orgId) {
        super("Org member " + orgMemberId + " has not accepted the invite to organization " + orgId);
    }
}
