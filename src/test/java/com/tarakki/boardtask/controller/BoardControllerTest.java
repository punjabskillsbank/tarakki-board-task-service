package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import  org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}