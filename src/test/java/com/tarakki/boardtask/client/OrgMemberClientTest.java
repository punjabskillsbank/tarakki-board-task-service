package com.tarakki.boardtask.client;

import com.tarakki.boardtask.dto.OrgMemberDTO;
import com.tarakki.boardtask.util.BoardMemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        ReflectionTestUtils.setField(orgMemberClient, "orgApiEndpoint",
                BoardMemberTestDataFactory.ORG_API_ENDPOINT);
        orgId = BoardMemberTestDataFactory.ORG_ID;
        orgMemberId = BoardMemberTestDataFactory.ORG_MEMBER_ID;
        orgMemberDto = BoardMemberTestDataFactory.createOrgMemberDto();
    }

    private void stubRestClientChain() {
        doReturn(requestHeadersUriSpec).when(orgServiceRestClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec)
                .uri(eq(BoardMemberTestDataFactory.ORG_API_ENDPOINT + "/{orgId}/members"), eq(orgId));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    @Test
    void findOrgMemberById_shouldReturnMatchingOrgMember() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(orgMemberDto));

        OrgMemberDTO result = orgMemberClient.findOrgMemberById(orgId, orgMemberId);

        assertNotNull(result);
        assertEquals(orgMemberId, result.getOrgMemberId());
        assertEquals(BoardMemberTestDataFactory.MEMBER_ID, result.getMemberId());
    }

    @Test
    void findOrgMemberById_shouldReturnNullWhenOrgMemberIdIsNotInTheOrganization() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(orgMemberDto));

        OrgMemberDTO result = orgMemberClient
                .findOrgMemberById(orgId, BoardMemberTestDataFactory.INVALID_ORG_MEMBER_ID);

        assertNull(result);
    }

    @Test
    void findOrgMemberById_shouldReturnNullWhenOrganizationHasNoMembers() {

        stubRestClientChain();
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of());

        OrgMemberDTO result = orgMemberClient.findOrgMemberById(orgId, orgMemberId);

        assertNull(result);
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
