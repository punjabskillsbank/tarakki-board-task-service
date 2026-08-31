package com.tarakki.boardtask.client;

import com.tarakki.common.dto.OrgMemberDTO;
import com.tarakki.common.exceptionHandling.OrganizationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrgMemberClient {

    private final RestClient orgServiceRestClient;

    @Value("${org.api.endpoint}")
    private String orgApiEndpoint;

    public Optional<OrgMemberDTO> findOrgMemberById(Long orgId, Long orgMemberId) {

        List<OrgMemberDTO> orgMembers;

        try {
            orgMembers = orgServiceRestClient.get()
                    .uri(orgApiEndpoint + "/{orgId}/members", orgId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OrgMemberDTO>>() {
                    });
        } catch (HttpClientErrorException.NotFound exception) {
            throw new OrganizationNotFoundException(orgId);
        }

        if (orgMembers == null) {
            return Optional.empty();
        }

        return orgMembers.stream()
                .filter(orgMember -> Objects.equals(orgMember.getOrgMemberId(), orgMemberId))
                .findFirst();
    }
}
