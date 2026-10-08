package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.GlobalExceptionHandler;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.service.GroupService;
import com.tarakki.boardtask.util.GroupTestDataFactory;
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

import static com.tarakki.boardtask.util.BoardTestDataFactory.EXISTING_BOARD_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(GroupController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = GroupControllerTest.MEMBER_ID_VALUE)
public class GroupControllerTest {

    static final String MEMBER_ID_VALUE = "c0ffee00-0000-4000-8000-000000000001";
    private static final UUID MEMBER_ID = UUID.fromString(MEMBER_ID_VALUE);


    @MockitoBean
    private GroupService groupService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long boardId;
    private GroupDTO groupDto;
    private Long invalidGroupId;

    @BeforeEach
    void setUp() {
        groupDto = GroupTestDataFactory.createGroupDto();
        invalidGroupId = GroupTestDataFactory.INVALID_GROUP_ID;
        boardId = GroupTestDataFactory.BOARD_ID;
    }

    @Test
    void shouldSetCreatedByFromTheTokenAndIgnoreTheOneInTheBody() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId))).thenReturn(groupDto);
        groupDto.setCreatedBy(UUID.randomUUID());

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated());

        ArgumentCaptor<GroupDTO> sentToService = ArgumentCaptor.forClass(GroupDTO.class);
        verify(groupService).createGroupByBoardId(sentToService.capture(), eq(boardId));
        assertEquals(MEMBER_ID, sentToService.getValue().getCreatedBy());
    }

    @Test
    void shouldReturn404WhenCreatorIsNotAMemberOfTheOrganization() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenThrow(new OrgMemberNotFoundException(groupDto.getCreatedBy(), 1L));

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .string("Member " + groupDto.getCreatedBy() + " is not a member of organization 1"));

        verify(groupService).createGroupByBoardId(any(), eq(boardId));
    }

    @Test
    void shouldReturn503WhenOrganizationServiceIsUnavailable() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenThrow(new OrgServiceUnavailableException(1L));

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isServiceUnavailable());

        verify(groupService).createGroupByBoardId(any(), eq(boardId));
    }

    @Test
    void shouldCreateGroupBySpecifiedBoardId() throws Exception {

        when(groupService.createGroupByBoardId(any(), eq(boardId)))
                .thenReturn(groupDto);

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
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

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
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

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Position " + GroupTestDataFactory.POSITION + " already exists for board id: " + boardId));
    }

    @Test
    void shouldReturnBadRequestWhenBoardIdIsMissing() throws Exception {

        groupDto.setBoardId(null);

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGroupNameIsMissing() throws Exception {

        groupDto.setGroupName(null);

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPositionIsMissing() throws Exception {

        groupDto.setPosition(null);

        mockMvc.perform(post("/api/boards/{boardId}/groups", boardId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetGroupsByBoardId() throws Exception {

        when(groupService.getGroupsByBoardId(boardId))
                .thenReturn(List.of(groupDto));

        mockMvc.perform(get("/api/boards/{boardId}/groups", boardId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupId").value(groupDto.getGroupId()))
                .andExpect(jsonPath("$[0].boardId").value(groupDto.getBoardId()))
                .andExpect(jsonPath("$[0].groupName").value(groupDto.getGroupName()))
                .andExpect(jsonPath("$[0].position").value(groupDto.getPosition()))
                .andExpect(jsonPath("$[0].createdBy").value(groupDto.getCreatedBy().toString()));

        verify(groupService).getGroupsByBoardId(boardId);
    }

    @Test
    void shouldGetEmptyListWhenBoardHasNoGroups() throws Exception {

        when(groupService.getGroupsByBoardId(boardId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/boards/{boardId}/groups", boardId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(groupService).getGroupsByBoardId(boardId);
    }

    @Test
    void shouldReturnNotFoundExceptionWhenGettingGroupsForUnknownBoardId() throws Exception {

        when(groupService.getGroupsByBoardId(boardId))
                .thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/boards/{boardId}/groups", boardId)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "Board not found with id: " + boardId));

        verify(groupService).getGroupsByBoardId(boardId);
    }

    @Test
    void shouldDeleteGroupById() throws Exception {
        doNothing().when(groupService).deleteGroup(groupDto.getGroupId(), boardId);

        mockMvc.perform((delete("/api/boards/{boardId}/groups/{groupId}", EXISTING_BOARD_ID, groupDto.getGroupId()))
                        .with(jwt()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(groupService).deleteGroup(groupDto.getGroupId(), boardId);
    }

    @Test
    void shouldReturnNoContentWhenGroupDoesNotExist() throws Exception {
        doNothing().when(groupService).deleteGroup(invalidGroupId, boardId);

        mockMvc.perform(delete("/api/boards/{boardId}/groups/{groupId}", EXISTING_BOARD_ID, invalidGroupId)
                        .with(jwt()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(groupService).deleteGroup(invalidGroupId, boardId);

    }

}
