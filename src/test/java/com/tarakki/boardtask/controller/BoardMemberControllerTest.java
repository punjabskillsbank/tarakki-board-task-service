package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;
import com.tarakki.boardtask.dto.BoardMemberUpdateDTO;
import com.tarakki.boardtask.exception.BoardMemberExistsException;
import com.tarakki.boardtask.exception.BoardMemberNotFoundException;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.service.BoardMemberService;
import com.tarakki.boardtask.util.BoardMemberTestDataFactory;
import com.tarakki.common.exceptionHandling.OrganizationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardMemberController.class)
public class BoardMemberControllerTest {

    private static final String URL = "/api/boards/{boardId}/members/{orgMemberId}";
    private static final String GET_BOARD_MEMBER_URL = "/api/boards/{boardId}/members/{boardMemberId}";

    @MockitoBean
    private BoardMemberService boardMemberService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BoardMemberDTO boardMemberDto;
    private BoardMemberRequestDTO boardMemberRequestDTO;
    private Long boardId;
    private Long invalidBoardId;
    private Long boardMemberId;
    private Long invalidBoardMemberId;
    private Long orgId;
    private Long orgMemberId;
    private Long invalidOrgMemberId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        boardMemberDto = BoardMemberTestDataFactory.createBoardMemberDto();
        boardMemberRequestDTO = BoardMemberTestDataFactory.createBoardMemberRequestDto();
        boardId = BoardMemberTestDataFactory.BOARD_ID;
        invalidBoardId = BoardMemberTestDataFactory.INVALID_BOARD_ID;
        boardMemberId = BoardMemberTestDataFactory.BOARD_MEMBER_ID;
        invalidBoardMemberId = BoardMemberTestDataFactory.INVALID_BOARD_MEMBER_ID;
        orgId = BoardMemberTestDataFactory.ORG_ID;
        orgMemberId = BoardMemberTestDataFactory.ORG_MEMBER_ID;
        invalidOrgMemberId = BoardMemberTestDataFactory.INVALID_ORG_MEMBER_ID;
        memberId = BoardMemberTestDataFactory.MEMBER_ID;
    }

    @Test
    void shouldAddMemberToSpecifiedBoard() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId), any(BoardMemberRequestDTO.class)))
                .thenReturn(boardMemberDto);

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
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

        when(boardMemberService.addMemberToBoard(eq(invalidBoardId), eq(orgMemberId), any(BoardMemberRequestDTO.class)))
                .thenThrow(new BoardNotFoundException(invalidBoardId));

        mockMvc.perform(post(URL, invalidBoardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + invalidBoardId));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenOrgMemberIsNotInBoardOrganization() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(invalidOrgMemberId), any(BoardMemberRequestDTO.class)))
                .thenThrow(new OrgMemberNotFoundException(invalidOrgMemberId, orgId));

        mockMvc.perform(post(URL, boardId, invalidOrgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Org member " + invalidOrgMemberId + " not found in organization " + orgId));
    }

    @Test
    void shouldReturnNotFoundWhenOrganizationDoesNotExist() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId), any(BoardMemberRequestDTO.class)))
                .thenThrow(new OrganizationNotFoundException(orgId));

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Organization with id " + orgId + " not found"));
    }

    @Test
    void shouldReturnConflictExceptionWhenMemberIsAlreadyOnBoard() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId), any(BoardMemberRequestDTO.class)))
                .thenThrow(new BoardMemberExistsException(memberId, boardId));

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Member " + memberId + " is already a member of board " + boardId));
    }

    @Test
    void shouldReturnServiceUnavailableExceptionWhenOrganizationServiceIsUnreachable() throws Exception {

        when(boardMemberService.addMemberToBoard(eq(boardId), eq(orgMemberId), any(BoardMemberRequestDTO.class)))
                .thenThrow(new OrgServiceUnavailableException(orgId));

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Unable to reach organization service to look up members of organization " + orgId));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsMissing() throws Exception {

        boardMemberRequestDTO.setEmail(null);

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCanEditIsMissing() throws Exception {

        boardMemberRequestDTO.setCanEdit(null);

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCanViewIsMissing() throws Exception {

        boardMemberRequestDTO.setCanView(null);

        mockMvc.perform(post(URL, boardId, orgMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardMemberRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBoardMemberById() throws Exception {
        when(boardMemberService.getBoardMemberById(boardId, boardMemberId))
                .thenReturn(boardMemberDto);

        mockMvc.perform(get(GET_BOARD_MEMBER_URL, boardId, boardMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardMemberId").value(boardMemberDto.getBoardMemberId()))
                .andExpect(jsonPath("$.boardId").value(boardMemberDto.getBoardId()))
                .andExpect(jsonPath("$.memberId").value(boardMemberDto.getMemberId().toString()))
                .andExpect(jsonPath("$.role").value(boardMemberDto.getRole().name()))
                .andExpect(jsonPath("$.canEdit").value(boardMemberDto.getCanEdit()))
                .andExpect(jsonPath("$.canView").value(boardMemberDto.getCanView()));
    }

    @Test
    void shouldReturnNotFoundWhenBoardIdDoesNotExistForGetBoardMember() throws Exception {
        when(boardMemberService.getBoardMemberById(invalidBoardId, boardMemberId))
                .thenThrow(new BoardNotFoundException(invalidBoardId));

        mockMvc.perform(get(GET_BOARD_MEMBER_URL, invalidBoardId, boardMemberId))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + invalidBoardId));
    }

    @Test
    void shouldReturnNotFoundWhenBoardMemberIdDoesNotExist() throws Exception {
        when(boardMemberService.getBoardMemberById(boardId, invalidBoardMemberId))
                .thenThrow(new BoardMemberNotFoundException(invalidBoardMemberId));

        mockMvc.perform(get(GET_BOARD_MEMBER_URL, boardId, invalidBoardMemberId))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board member not found with id: " + invalidBoardMemberId));
    }

    @Test
    void shouldPatchBoardMemberById() throws Exception {
        BoardMemberUpdateDTO updateDto = BoardMemberTestDataFactory.createBoardMemberUpdateDto();
        BoardMemberDTO patchedDto = BoardMemberTestDataFactory.createBoardMemberDto();
        patchedDto.setRole(updateDto.getRole());
        patchedDto.setCanEdit(updateDto.getCanEdit());
        patchedDto.setCanView(updateDto.getCanView());

        when(boardMemberService.patchBoardMemberById(eq(boardId), eq(boardMemberId), any(BoardMemberUpdateDTO.class)))
                .thenReturn(patchedDto);

        mockMvc.perform(patch(GET_BOARD_MEMBER_URL, boardId, boardMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardMemberId").value(patchedDto.getBoardMemberId()))
                .andExpect(jsonPath("$.boardId").value(patchedDto.getBoardId()))
                .andExpect(jsonPath("$.memberId").value(patchedDto.getMemberId().toString()))
                .andExpect(jsonPath("$.role").value(updateDto.getRole().name()))
                .andExpect(jsonPath("$.canEdit").value(updateDto.getCanEdit()))
                .andExpect(jsonPath("$.canView").value(updateDto.getCanView()));
    }

    @Test
    void shouldReturnNotFoundWhenBoardIdDoesNotExistForPatchBoardMember() throws Exception {
        BoardMemberUpdateDTO updateDto = BoardMemberTestDataFactory.createBoardMemberUpdateDto();

        when(boardMemberService.patchBoardMemberById(eq(invalidBoardId), eq(boardMemberId), any(BoardMemberUpdateDTO.class)))
                .thenThrow(new BoardNotFoundException(invalidBoardId));

        mockMvc.perform(patch(GET_BOARD_MEMBER_URL, invalidBoardId, boardMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + invalidBoardId));
    }

    @Test
    void shouldReturnNotFoundWhenBoardMemberIdDoesNotExistForPatchBoardMember() throws Exception {
        BoardMemberUpdateDTO updateDto = BoardMemberTestDataFactory.createBoardMemberUpdateDto();

        when(boardMemberService.patchBoardMemberById(eq(boardId), eq(invalidBoardMemberId), any(BoardMemberUpdateDTO.class)))
                .thenThrow(new BoardMemberNotFoundException(invalidBoardMemberId));

        mockMvc.perform(patch(GET_BOARD_MEMBER_URL, boardId, invalidBoardMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board member not found with id: " + invalidBoardMemberId));
    }
}
