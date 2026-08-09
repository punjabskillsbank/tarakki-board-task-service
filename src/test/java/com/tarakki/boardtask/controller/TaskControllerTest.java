package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.TaskNotFoundException;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long boardId;
    private TaskDTO taskDto;

    @BeforeEach
    void setUp() {
        taskDto = TaskTestDataFactory.createTaskDto();
        boardId = TaskTestDataFactory.BOARD_ID;
    }

    @Test
    void shouldCreateTaskBySpecifiedBoardId() throws Exception {

        when(taskService.createTaskByBoardId(any(), eq(boardId)))
                .thenReturn(taskDto);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(taskDto.getGroupId()))
                .andExpect(jsonPath("$.title").value(taskDto.getTitle()))
                .andExpect(jsonPath("$.position").value(taskDto.getPosition()))
                .andExpect(jsonPath("$.createdBy").value(taskDto.getCreatedBy().toString()));

    }

    @Test
    void shouldGetTasksByBoardId() throws Exception {
        when(taskService.getTasksByBoardId(boardId)).thenReturn(List.of(taskDto));

        mockMvc.perform(get("/api/{boardId}/task", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value(taskDto.getGroupId()))
                .andExpect(jsonPath("$[0].title").value(taskDto.getTitle()))
                .andExpect(jsonPath("$[0].position").value(taskDto.getPosition()));
    }

    @Test
    void shouldReturnNotFoundWhenGettingTasksForUnknownBoardId() throws Exception {
        when(taskService.getTasksByBoardId(boardId))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/{boardId}/task", boardId))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + boardId));
    }

    @Test
    void shouldReturnBadRequestWhenBoardIdIsMissing() throws Exception {

        taskDto.setBoardId(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnNotFoundWhenBoardIdIsNotFound() throws Exception {
        when(taskService.createTaskByBoardId(any(),eq(boardId)))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + boardId));
    }


    @Test
    void shouldReturnBadRequestWhenGroupIdIsMissing() throws Exception {

        taskDto.setGroupId(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsMissing() throws Exception {

        taskDto.setTitle(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatedByIsMissing() throws Exception {

        taskDto.setCreatedBy(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPatchTaskSuccessfully() throws Exception {
        Long boardId = 1L;
        Long taskId = 100L;

        TaskUpdateDTO patchRequestDto = new TaskUpdateDTO();
        patchRequestDto.setTitle("New Patched Title");
        patchRequestDto.setPosition(5);

        TaskDTO patchedResponseDto = TaskTestDataFactory.createTaskDto();
        patchedResponseDto.setBoardId(boardId);
        patchedResponseDto.setTaskId(taskId);
        patchedResponseDto.setTitle("New Patched Title");
        patchedResponseDto.setPosition(5);

        when(taskService.patchTaskById(eq(boardId), eq(taskId), any(TaskUpdateDTO.class)))
                .thenReturn(patchedResponseDto);

        mockMvc.perform(patch("/api/{boardId}/task/{taskId}", boardId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Patched Title"))
                .andExpect(jsonPath("$.position").value(5))
                .andExpect(jsonPath("$.groupId").value(patchedResponseDto.getGroupId()));
    }

    @Test
    void shouldHandleTaskNotFoundException() throws Exception {
        Long boardId = 1L;
        Long taskId = 999L;

        TaskUpdateDTO patchRequestDto = new TaskUpdateDTO();
        patchRequestDto.setTitle("Updated Title");

        when(taskService.patchTaskById(eq(boardId), eq(taskId), any(TaskUpdateDTO.class)))
                .thenThrow(new TaskNotFoundException(taskId));

        mockMvc.perform(patch("/api/{boardId}/task/{taskId}", boardId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isNotFound());
    }
}
