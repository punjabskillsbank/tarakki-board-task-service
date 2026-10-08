package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.BoardUpdateDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.GlobalExceptionHandler;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.tarakki.boardtask.util.BoardTestDataFactory.EXISTING_BOARD_ID;
import static com.tarakki.boardtask.util.BoardTestDataFactory.MISSING_BOARD_ID;

@WebMvcTest(BoardController.class)
@Import(GlobalExceptionHandler.class)
class BoardControllerTest {



    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @Autowired
    private ObjectMapper objectMapper;

    private BoardDTO input;
    private BoardDTO output;

    @BeforeEach
    void setUp() {
        input = BoardTestDataFactory.createBoardDTO();
        output = BoardTestDataFactory.createBoardDTOWithId();
    }

    @Test
    void shouldCreateBoard() throws Exception {

        when(boardService.createBoard(any()))
                .thenReturn(output);

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").value(output.getBoardId()))
                .andExpect(jsonPath("$.boardName").value(input.getBoardName()))
                .andExpect(jsonPath("$.boardDesc").value(input.getBoardDesc()));
    }

    @Test
    void shouldReturnBadRequestWhenBoardNameIsMissing() throws Exception {

        input.setBoardName(null);

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBoardDescIsMissing() throws Exception {

        input.setBoardDesc(null);

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOrgIdIsMissing() throws Exception {

        input.setOrgId(null);

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteBoard() throws Exception {

        doNothing().when(boardService).deleteBoard(EXISTING_BOARD_ID);

        mockMvc.perform(delete("/api/boards/{boardId}", EXISTING_BOARD_ID)
                        .with(jwt()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(boardService).deleteBoard(EXISTING_BOARD_ID);
    }

    @Test
    void shouldReturn404WhenDeletingBoardThatDoesNotExist() throws Exception {
        doThrow(new BoardNotFoundException(MISSING_BOARD_ID)).when(boardService).deleteBoard(MISSING_BOARD_ID);

        mockMvc.perform(delete("/api/boards/{boardId}", MISSING_BOARD_ID)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Board not found with id: " + MISSING_BOARD_ID));

        verify(boardService).deleteBoard(MISSING_BOARD_ID);
    }

    @Test
    void shouldReturn404WhenCreatorIsNotAMemberOfTheOrganization() throws Exception {
        when(boardService.createBoard(any()))
                .thenThrow(new OrgMemberNotFoundException(input.getCreatedBy(), input.getOrgId()));

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member " + input.getCreatedBy()
                        + " is not a member of organization " + input.getOrgId()));

        verify(boardService).createBoard(any());
    }

    @Test
    void shouldReturn503WhenOrganizationServiceIsUnavailable() throws Exception {
        when(boardService.createBoard(any()))
                .thenThrow(new OrgServiceUnavailableException(input.getOrgId()));

        mockMvc.perform(post("/api/boards")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isServiceUnavailable());

        verify(boardService).createBoard(any());
    }

    @Test
    void shouldGetBoardsByOrganizationAndReturn200Ok() throws Exception {

        Long orgId = BoardTestDataFactory.VALID_ORG_ID;
        List<BoardDTO> boards = List.of(output);

        when(boardService.getBoardsByOrganization(orgId))
                .thenReturn(boards);

        mockMvc.perform(get("/api/boards/organization/{orgId}", orgId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardId").value(output.getBoardId()))
                .andExpect(jsonPath("$[0].boardName").value(output.getBoardName()))
                .andExpect(jsonPath("$[0].boardDesc").value(output.getBoardDesc()));
    }

    @Test
    void shouldGetBoardsByOrganizationAndReturn404NotFound() throws Exception {

        Long orgId = BoardTestDataFactory.INVALID_ORG_ID;

        when(boardService.getBoardsByOrganization(orgId))
                .thenThrow(new com.tarakki.common.exceptionHandling.OrganizationNotFoundException(orgId));

        mockMvc.perform(get("/api/boards/organization/{orgId}", orgId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBoardByIdAndReturn200Ok() throws Exception {

        Long boardId = EXISTING_BOARD_ID;

        when(boardService.getBoardById(boardId))
                .thenReturn(output);

        mockMvc.perform(get("/api/boards/{id}", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(output.getBoardId()))
                .andExpect(jsonPath("$.boardName").value(output.getBoardName()))
                .andExpect(jsonPath("$.boardDesc").value(output.getBoardDesc()));
    }

    @Test
    void shouldGetBoardByIdAndReturn404WhenNotFound() throws Exception {

        Long boardId = MISSING_BOARD_ID;

        when(boardService.getBoardById(boardId))
                .thenThrow(new com.tarakki.boardtask.exception.BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/boards/{id}", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPatchBoardByIdAndReturn200Ok() throws Exception {
        Long boardId = EXISTING_BOARD_ID;
        BoardUpdateDTO updateDTO = BoardTestDataFactory.createBoardUpdateDTO();
        BoardDTO patchedDTO = BoardTestDataFactory.createBoardDTOWithId();
        patchedDTO.setBoardName(updateDTO.getBoardName());
        patchedDTO.setBoardDesc(updateDTO.getBoardDesc());

        when(boardService.patchBoardById(eq(boardId), any(BoardUpdateDTO.class)))
                .thenReturn(patchedDTO);

        mockMvc.perform(patch("/api/boards/{boardId}", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(patchedDTO.getBoardId()))
                .andExpect(jsonPath("$.boardName").value(updateDTO.getBoardName()))
                .andExpect(jsonPath("$.boardDesc").value(updateDTO.getBoardDesc()));
    }

    @Test
    void shouldPatchBoardByIdAndReturn404WhenNotFound() throws Exception {
        Long boardId = MISSING_BOARD_ID;
        BoardUpdateDTO updateDTO = BoardTestDataFactory.createBoardUpdateDTO();

        when(boardService.patchBoardById(eq(boardId), any(BoardUpdateDTO.class)))
                .thenThrow(new com.tarakki.boardtask.exception.BoardNotFoundException(boardId));

        mockMvc.perform(patch("/api/boards/{boardId}", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenCreatedByIsMissing() throws Exception {

        input.setCreatedBy(null);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }
}
