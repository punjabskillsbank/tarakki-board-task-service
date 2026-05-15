package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.common.exceptionHandling.BoardIdNotFoundException;
import  org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.tarakki.boardtask.util.BoardTestDataFactory.EXISTING_BOARD_ID;
import static com.tarakki.boardtask.util.BoardTestDataFactory.MISSING_BOARD_ID;

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
    void shouldDeleteBoard() throws Exception {

        mockMvc.perform(delete("/api/boards/{boardId}", EXISTING_BOARD_ID))
                .andExpect(status().isNoContent());

        verify(boardService).deleteBoard(EXISTING_BOARD_ID);
    }

    @Test
    void shouldReturnNotFoundWhenBoardDoesNotExist() throws Exception {

        doThrow(new BoardIdNotFoundException(MISSING_BOARD_ID))
                .when(boardService)
                .deleteBoard(MISSING_BOARD_ID);

        mockMvc.perform(delete("/api/boards/{boardId}", MISSING_BOARD_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Board not found with id: " + MISSING_BOARD_ID));
    }
}
