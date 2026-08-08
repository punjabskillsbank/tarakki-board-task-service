package com.tarakki.boardtask.client;

import com.tarakki.boardtask.dto.OrgMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrgMemberClient {

    private final RestClient orgServiceRestClient;

    @Value("${org.api.endpoint}")
    private String orgApiEndpoint;

    public OrgMemberDTO findOrgMemberById(Long orgId, Long orgMemberId) {

        List<OrgMemberDTO> orgMembers = orgServiceRestClient.get()
                .uri(orgApiEndpoint + "/{orgId}/members", orgId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrgMemberDTO>>() {
                });

        return orgMembers.stream()
                .filter(orgMember -> Objects.equals(orgMember.getOrgMemberId(), orgMemberId))
                .findFirst()
                .orElse(null);
    }
}
