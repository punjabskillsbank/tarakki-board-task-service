package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
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
                output = BoardTestDataFactory.createBoardDTO();
    }

    @Test
    void shouldCreateBoard() throws Exception {

        when(boardService.createBoard(any()))
                .thenReturn(output);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardName").value(input.getBoardName()))
                .andExpect(jsonPath("$.boardDesc").value(input.getBoardDesc()));
    }

    @Test
    void shouldReturnBadRequestWhenBoardNameIsMissing() throws Exception {

        input.setBoardName(null);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBoardDescIsMissing() throws Exception {

        input.setBoardDesc(null);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOrgIdIsMissing() throws Exception {

        input.setOrgId(null);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatedByIsMissing() throws Exception {

        input.setCreatedBy(null);

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBoardsByOrganizationAndReturn200Ok() throws Exception {

        Long orgId = BoardTestDataFactory.VALID_ORG_ID;
        List<BoardDTO> boards = List.of(output);

        when(boardService.getBoardsByOrganization(orgId))
                .thenReturn(boards);

        mockMvc.perform(get("/api/boards/organization/{orgId}", orgId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardName").value(output.getBoardName()))
                .andExpect(jsonPath("$[0].boardDesc").value(output.getBoardDesc()));
    }

    @Test
    void shouldGetBoardsByOrganizationAndReturn404NotFound() throws Exception {

        Long orgId = BoardTestDataFactory.INVALID_ORG_ID;

        when(boardService.getBoardsByOrganization(orgId))
                .thenThrow(new com.tarakki.common.exceptionHandling.OrganisationNotFoundException(orgId));

        mockMvc.perform(get("/api/boards/organization/{orgId}", orgId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}