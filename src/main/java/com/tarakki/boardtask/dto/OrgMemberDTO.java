package com.tarakki.boardtask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Read-only view of an organization member as returned by organization-service.
 * Only the fields this service needs are mapped; the rest of the payload is ignored.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgMemberDTO {

    private Long orgMemberId;

    private Long orgId;

    private UUID memberId;

    private String email;

}
