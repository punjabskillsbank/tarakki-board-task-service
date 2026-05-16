package com.tarakki.boardtask.exception;

public class OrganisationNotFoundException extends RuntimeException {

    public OrganisationNotFoundException(Long orgId) {
        super("Organisation with id " + orgId + " not found");
    }

    public OrganisationNotFoundException(String message) {
        super(message);
    }
}
