package com.tarakki.boardtask.client;

import com.tarakki.boardtask.dto.OrgMemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@AllArgsConstructor
public class OrgMemberClient {

    private final RestClient orgServiceRestClient;

    /**
     * Organization-service exposes no lookup by orgMemberId, so the organization's
     * members are fetched and matched here.
     */
    public Optional<OrgMemberDTO> findOrgMemberById(Long orgId, Long orgMemberId) {

        List<OrgMemberDTO> orgMembers = orgServiceRestClient.get()
                .uri("/api/organizations/{orgId}/members", orgId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrgMemberDTO>>() {
                });

        if (orgMembers == null) {
            return Optional.empty();
        }

        return orgMembers.stream()
                .filter(orgMember -> Objects.equals(orgMember.getOrgMemberId(), orgMemberId))
                .findFirst();
    }
}
