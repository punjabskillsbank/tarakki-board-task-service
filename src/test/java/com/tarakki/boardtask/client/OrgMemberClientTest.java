package com.tarakki.boardtask.client;

import com.tarakki.boardtask.dto.OrgMemberDTO;
import com.tarakki.boardtask.util.BoardMemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrgMemberClientTest {

    @Mock
    private RestClient orgServiceRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OrgMemberClient orgMemberClient;

    private Long orgId;
    private Long orgMemberId;
    private OrgMemberDTO orgMemberDto;

    @BeforeEach
    void setUp() {
        orgMemberClient = new OrgMemberClient(orgServiceRestClient);
        orgId = BoardMemberTestDataFactory.ORG_ID;
        orgMemberId = BoardMemberTestDataFactory.ORG_MEMBER_ID;
        orgMemberDto = BoardMemberTestDataFactory.createOrgMemberDto();
    }

    private void stubRestClientChain() {
        doReturn(requestHeadersUriSpec).when(orgServiceRestClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec)
                .uri(eq("/api/organizations/{orgId}/members"), eq(orgId));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    @Test
    void findOrgMemberById_shouldReturnMatchingOrgMember() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(orgMemberDto));

        Optional<OrgMemberDTO> result = orgMemberClient.findOrgMemberById(orgId, orgMemberId);

        assertTrue(result.isPresent());
        assertEquals(orgMemberId, result.get().getOrgMemberId());
        assertEquals(BoardMemberTestDataFactory.MEMBER_ID, result.get().getMemberId());
    }

    @Test
    void findOrgMemberById_shouldReturnEmptyWhenOrgMemberIdIsNotInTheOrganization() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(orgMemberDto));

        Optional<OrgMemberDTO> result = orgMemberClient
                .findOrgMemberById(orgId, BoardMemberTestDataFactory.INVALID_ORG_MEMBER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void findOrgMemberById_shouldReturnEmptyWhenOrganizationHasNoMembers() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of());

        Optional<OrgMemberDTO> result = orgMemberClient.findOrgMemberById(orgId, orgMemberId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findOrgMemberById_shouldPropagateRestClientExceptionWhenError() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("500 Internal Server Error"));

        assertThrows(RestClientException.class,
                () -> orgMemberClient.findOrgMemberById(orgId, orgMemberId));
    }
}
