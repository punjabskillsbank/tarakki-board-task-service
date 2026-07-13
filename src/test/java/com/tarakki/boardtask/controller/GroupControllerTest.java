package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.service.GroupService;
import com.tarakki.boardtask.util.GroupTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
public class GroupControllerTest {

    @MockitoBean
    private GroupService groupService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long boardId;
    private GroupDTO groupDto;

    @BeforeEach
    void setUp() {
        groupDto = GroupTestDataFactory.createGroupDto();
        boardId = GroupTestDataFactory.BOARD_ID;
    }

    @Test
    void shouldCreateGroupBySpecifiedBoardId() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenReturn(groupDto);

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(groupDto.getGroupId()))
                .andExpect(jsonPath("$.boardId").value(groupDto.getBoardId()))
                .andExpect(jsonPath("$.groupName").value(groupDto.getGroupName()))
                .andExpect(jsonPath("$.position").value(groupDto.getPosition()))
                .andExpect(jsonPath("$.createdBy").value(groupDto.getCreatedBy().toString()));
    }

    @Test
    void shouldReturnNotFoundExceptionWhenBoardIdIsNotFound() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + boardId));
    }

    @Test
    void shouldReturnConflictExceptionWhenPositionAlreadyExists() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenThrow(new PositionAlreadyExistsException(GroupTestDataFactory.POSITION, boardId));

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Position " + GroupTestDataFactory.POSITION + " already exists for board id: " + boardId));
    }

    @Test
    void shouldReturnBadRequestWhenBoardIdIsMissing() throws Exception {

        groupDto.setBoardId(null);

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGroupNameIsMissing() throws Exception {

        groupDto.setGroupName(null);

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPositionIsMissing() throws Exception {

        groupDto.setPosition(null);

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatedByIsMissing() throws Exception {

        groupDto.setCreatedBy(null);

        mockMvc.perform(post("/api/groups/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }
}
