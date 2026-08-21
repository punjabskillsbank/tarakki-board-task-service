package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.exception.BoardMemberExistsException;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotAcceptedException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.service.BoardMemberService;
import com.tarakki.boardtask.util.BoardMemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardMemberController.class)
public class BoardMemberControllerTest {

    private static final String URL = "/api/boards/{boardId}/members/{orgMemberId}";

    @MockitoBean
    private BoardMemberService boardMemberService;

    @Autowired
    private MockMvc mockMvc;

    private BoardMemberDTO boardMemberDto;
    private Long boardId;
    private Long invalidBoardId;
    private Long orgId;
    private Long orgMemberId;
    private Long invalidOrgMemberId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        boardMemberDto = BoardMemberTestDataFactory.createBoardMemberDto();
        boardId = BoardMemberTestDataFactory.BOARD_ID;
        invalidBoardId = BoardMemberTestDataFactory.INVALID_BOARD_ID;
        orgId = BoardMemberTestDataFactory.ORG_ID;
        orgMemberId = BoardMemberTestDataFactory.ORG_MEMBER_ID;
        invalidOrgMemberId = BoardMemberTestDataFactory.INVALID_ORG_MEMBER_ID;
        memberId = BoardMemberTestDataFactory.MEMBER_ID;
    }

    @Test
    void shouldAddMemberToSpecifiedBoard() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId)))
                .thenReturn(boardMemberDto);

        mockMvc.perform(post(URL, boardId, orgMemberId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardMemberId").value(boardMemberDto.getBoardMemberId()))
                .andExpect(jsonPath("$.boardId").value(boardMemberDto.getBoardId()))
                .andExpect(jsonPath("$.memberId").value(boardMemberDto.getMemberId().toString()))
                .andExpect(jsonPath("$.role").value(boardMemberDto.getRole().name()))
                .andExpect(jsonPath("$.canEdit").value(false))
                .andExpect(jsonPath("$.canView").value(true));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenBoardIdIsNotFound() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(invalidBoardId), eq(orgMemberId)))
                .thenThrow(new BoardNotFoundException(invalidBoardId));

        mockMvc.perform(post(URL, invalidBoardId, orgMemberId))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + invalidBoardId));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenOrgMemberIsNotInBoardOrganization() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(invalidOrgMemberId)))
                .thenThrow(new OrgMemberNotFoundException(invalidOrgMemberId, orgId));

        mockMvc.perform(post(URL, boardId, invalidOrgMemberId))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Org member " + invalidOrgMemberId + " not found in organization " + orgId));
    }

    @Test
    void shouldReturnConflictExceptionWhenOrgMemberHasNotAcceptedOrgInvite() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId)))
                .thenThrow(new OrgMemberNotAcceptedException(orgMemberId, orgId));

        mockMvc.perform(post(URL, boardId, orgMemberId))
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Org member " + orgMemberId + " has not accepted the invite to organization " + orgId));
    }

    @Test
    void shouldReturnConflictExceptionWhenMemberIsAlreadyOnBoard() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId)))
                .thenThrow(new BoardMemberExistsException(memberId, boardId));

        mockMvc.perform(post(URL, boardId, orgMemberId))
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Member " + memberId + " is already a member of board " + boardId));
    }

    @Test
    void shouldReturnServiceUnavailableExceptionWhenOrganizationServiceIsUnreachable() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId)))
                .thenThrow(new OrgServiceUnavailableException(orgId));

        mockMvc.perform(post(URL, boardId, orgMemberId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Unable to reach organization service to look up members of organization " + orgId));
    }
}
