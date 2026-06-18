package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
    private TaskDTO input;
    private TaskDTO output;

    @BeforeEach
    void setUp() {
        input = TaskTestDataFactory.createTaskDto();
        output = TaskTestDataFactory.createTaskDto();
        boardId = TaskTestDataFactory.BOARD_ID;
    }

    @Test
    void shouldCreateTaskBySpecifiedBoardId() throws Exception {

        when(taskService.createTaskBySpecifiedBoardId(any(), eq(boardId)))
                .thenReturn(output);

        mockMvc.perform(post("/api/tasks/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(output.getGroupId()))
                .andExpect(jsonPath("$.title").value(output.getTitle()))
                .andExpect(jsonPath("$.position").value(output.getPosition()))
                .andExpect(jsonPath("$.createdBy").value(output.getCreatedBy().toString()));

    }
}
