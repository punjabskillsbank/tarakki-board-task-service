package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        doNothing().when(boardService).deleteBoard(EXISTING_BOARD_ID);

        mockMvc.perform(delete("/api/boards/{boardId}", EXISTING_BOARD_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(boardService).deleteBoard(EXISTING_BOARD_ID);
    }

    @Test
    void shouldReturnNoContentWhenBoardDoesNotExist() throws Exception {
        doNothing().when(boardService).deleteBoard(MISSING_BOARD_ID);

        mockMvc.perform(delete("/api/boards/{boardId}", MISSING_BOARD_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(boardService).deleteBoard(MISSING_BOARD_ID);
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
                .thenThrow(new com.tarakki.common.exceptionHandling.OrganizationNotFoundException(orgId));

        mockMvc.perform(get("/api/boards/organization/{orgId}", orgId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBoardByIdAndReturn200Ok() throws Exception {

        Long boardId = EXISTING_BOARD_ID;

        when(boardService.getBoardById(boardId))
                .thenReturn(output);

        mockMvc.perform(get("/api/boards/{id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardName").value(output.getBoardName()))
                .andExpect(jsonPath("$.boardDesc").value(output.getBoardDesc()));
    }

    @Test
    void shouldGetBoardByIdAndReturn404WhenNotFound() throws Exception {

        Long boardId = MISSING_BOARD_ID;

        when(boardService.getBoardById(boardId))
                .thenThrow(new com.tarakki.boardtask.exception.BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/boards/{id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPatchTaskSuccessfully() throws Exception {
        Long boardId = EXISTING_BOARD_ID;
        Long taskId = 100L;

        TaskDTO patchRequestDto = new TaskDTO();
        patchRequestDto.setTitle("New Patched Title");
        patchRequestDto.setPosition(5);

        TaskDTO patchedResponseDto = TaskTestDataFactory.createTaskDto();
        patchedResponseDto.setTitle("New Patched Title");
        patchedResponseDto.setPosition(5);

        when(boardService.patchTask(eq(boardId), eq(taskId), any(TaskDTO.class)))
                .thenReturn(patchedResponseDto);

        mockMvc.perform(patch("/api/boards/{boardId}/tasks/{taskId}", boardId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Patched Title"))
                .andExpect(jsonPath("$.position").value(5))
                .andExpect(jsonPath("$.groupId").value(patchedResponseDto.getGroupId()));
    }


}
