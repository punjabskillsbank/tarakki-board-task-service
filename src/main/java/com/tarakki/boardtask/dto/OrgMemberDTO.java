package com.tarakki.boardtask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tarakki.boardtask.enums.OrgMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgMemberDTO {

    private Long orgMemberId;

    private Long orgId;

    private UUID memberId;

    private OrgMemberStatus memberAccountStatus;

}
