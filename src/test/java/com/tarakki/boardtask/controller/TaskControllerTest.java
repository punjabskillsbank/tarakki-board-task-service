package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.GlobalExceptionHandler;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.exception.TaskNotFoundException;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = TaskControllerTest.MEMBER_ID_VALUE)
public class TaskControllerTest {

    static final String MEMBER_ID_VALUE = "c0ffee00-0000-4000-8000-000000000001";
    private static final UUID MEMBER_ID = UUID.fromString(MEMBER_ID_VALUE);


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
    void shouldSetCreatedByFromTheTokenAndIgnoreTheOneInTheBody() throws Exception {

        when(taskService.createTaskByBoardId(any(), eq(boardId))).thenReturn(taskDto);
        taskDto.setCreatedBy(UUID.randomUUID());

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isCreated());

        ArgumentCaptor<TaskDTO> sentToService = ArgumentCaptor.forClass(TaskDTO.class);
        verify(taskService).createTaskByBoardId(sentToService.capture(), eq(boardId));
        assertEquals(MEMBER_ID, sentToService.getValue().getCreatedBy());
    }

    @Test
    void shouldReturn404WhenCreatorIsNotAMemberOfTheOrganization() throws Exception {

        when(taskService.createTaskByBoardId(any(), eq(boardId)))
                .thenThrow(new OrgMemberNotFoundException(taskDto.getCreatedBy(), 1L));

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .string("Member " + taskDto.getCreatedBy() + " is not a member of organization 1"));

        verify(taskService).createTaskByBoardId(any(), eq(boardId));
    }

    @Test
    void shouldReturn503WhenOrganizationServiceIsUnavailable() throws Exception {

        when(taskService.createTaskByBoardId(any(), eq(boardId)))
                .thenThrow(new OrgServiceUnavailableException(1L));

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isServiceUnavailable());

        verify(taskService).createTaskByBoardId(any(), eq(boardId));
    }

    @Test
    void shouldCreateTaskBySpecifiedBoardId() throws Exception {

        when(taskService.createTaskByBoardId(any(), eq(boardId)))
                .thenReturn(taskDto);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
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

        mockMvc.perform(get("/api/{boardId}/task", boardId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value(taskDto.getGroupId()))
                .andExpect(jsonPath("$[0].title").value(taskDto.getTitle()))
                .andExpect(jsonPath("$[0].position").value(taskDto.getPosition()));
    }

    @Test
    void shouldReturnNotFoundWhenGettingTasksForUnknownBoardId() throws Exception {
        when(taskService.getTasksByBoardId(boardId))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/{boardId}/task", boardId)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + boardId));
    }

    @Test
    void shouldReturnBadRequestWhenBoardIdIsMissing() throws Exception {

        taskDto.setBoardId(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnNotFoundWhenBoardIdIsNotFound() throws Exception {
        when(taskService.createTaskByBoardId(any(),eq(boardId)))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
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
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsMissing() throws Exception {

        taskDto.setTitle(null);

        mockMvc.perform(post("/api/{boardId}/task", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPatchTaskSuccessfully() throws Exception {
        Long taskId = TaskTestDataFactory.TASK_ID;
        TaskUpdateDTO patchRequestDto = TaskTestDataFactory.createTaskUpdateDto();
        TaskDTO patchedResponseDto = TaskTestDataFactory.createTaskDto();

        when(taskService.patchTaskById(eq(boardId), eq(taskId), any(TaskUpdateDTO.class)))
                .thenReturn(patchedResponseDto);

        mockMvc.perform(patch("/api/{boardId}/task/{taskId}", boardId, taskId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(patchedResponseDto.getTitle()))
                .andExpect(jsonPath("$.position").value(patchedResponseDto.getPosition()))
                .andExpect(jsonPath("$.groupId").value(patchedResponseDto.getGroupId()));
    }

    @Test
    void shouldHandleTaskNotFoundException() throws Exception {
        Long taskId = TaskTestDataFactory.TASK_ID;
        TaskUpdateDTO patchRequestDto = TaskTestDataFactory.createTaskUpdateDto();

        when(taskService.patchTaskById(eq(boardId), eq(taskId), any(TaskUpdateDTO.class)))
                .thenThrow(new TaskNotFoundException(taskId));

        mockMvc.perform(patch("/api/{boardId}/task/{taskId}", boardId, taskId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequestDto)))
                .andExpect(status().isNotFound());
    }
}
