package com.tarakki.boardtask.exception;

public class OrgServiceUnavailableException extends RuntimeException {
    public OrgServiceUnavailableException(Long orgId) {
        super("Unable to reach organization service to look up members of organization " + orgId);
    }
}
