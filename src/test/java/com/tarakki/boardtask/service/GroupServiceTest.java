package com.tarakki.boardtask.service;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.dto.GroupUpdateDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.GroupNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.GroupRepository;
import com.tarakki.boardtask.serviceImpl.GroupServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.GroupTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private OrgMemberClient orgMemberClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private GroupDTO groupDTO;
    private Group groupEntity;
    private Long boardId;
    private Long invalidBoardId;
    private Integer position;
    private Board board;
    private Long groupId;
    private Long invalidGroupId;

    @BeforeEach
    void setUp() {
        groupDTO = GroupTestDataFactory.createGroupDto();
        groupEntity = GroupTestDataFactory.createGroupEntity();
        boardId = GroupTestDataFactory.BOARD_ID;
        invalidBoardId = GroupTestDataFactory.INVALID_BOARD_ID;
        position = GroupTestDataFactory.POSITION;
        board = BoardTestDataFactory.createBoardEntity();
        groupId = GroupTestDataFactory.GROUP_ID;
        invalidGroupId = GroupTestDataFactory.INVALID_GROUP_ID;
    }

    @Test
    void shouldCreateGroupByBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(orgMemberClient.isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy()))
                .thenReturn(true);

        when(groupRepository.existsByBoardIdAndPosition(boardId, position))
                .thenReturn(false);

        when(modelMapper.map(any(GroupDTO.class), eq(Group.class)))
                .thenReturn(groupEntity);

        when(groupRepository.save(any(Group.class)))
                .thenReturn(groupEntity);

        when(modelMapper.map(any(Group.class), eq(GroupDTO.class)))
                .thenReturn(groupDTO);

        GroupDTO result = groupService.createGroupByBoardId(groupDTO, boardId);

        assertNotNull(result);
        assertEquals(groupDTO.getBoardId(), result.getBoardId());
        assertEquals(groupDTO.getGroupName(), result.getGroupName());
        assertEquals(groupDTO.getPosition(), result.getPosition());
        assertEquals(groupDTO.getCreatedBy(), result.getCreatedBy());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy());
        verify(groupRepository).existsByBoardIdAndPosition(boardId, position);
        verify(modelMapper).map(any(GroupDTO.class), eq(Group.class));
        verify(groupRepository).save(any(Group.class));
        verify(modelMapper).map(any(Group.class), eq(GroupDTO.class));
    }

    @Test
    void createGroupByBoardId_shouldSaveUnderTheBoardIdFromThePathAndNotTheBody() {

        groupDTO.setBoardId(invalidBoardId);

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(orgMemberClient.isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy()))
                .thenReturn(true);

        when(groupRepository.existsByBoardIdAndPosition(boardId, position))
                .thenReturn(false);

        when(modelMapper.map(any(GroupDTO.class), eq(Group.class)))
                .thenReturn(groupEntity);

        when(groupRepository.save(any(Group.class)))
                .thenReturn(groupEntity);

        when(modelMapper.map(any(Group.class), eq(GroupDTO.class)))
                .thenReturn(groupDTO);

        groupService.createGroupByBoardId(groupDTO, boardId);

        ArgumentCaptor<GroupDTO> mappedDto = ArgumentCaptor.forClass(GroupDTO.class);
        verify(modelMapper).map(mappedDto.capture(), eq(Group.class));
        assertEquals(boardId, mappedDto.getValue().getBoardId());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy());
        verify(groupRepository).existsByBoardIdAndPosition(boardId, position);
        verify(groupRepository).save(any(Group.class));
        verify(modelMapper).map(any(Group.class), eq(GroupDTO.class));
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> groupService.createGroupByBoardId(groupDTO, invalidBoardId)
        );

        assertEquals("Board not found with id: " + invalidBoardId, exception.getMessage());

        verify(boardRepository).findById(invalidBoardId);
        verify(orgMemberClient, never()).isMemberInOrganization(any(), any());
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void createGroupByBoardId_shouldThrowWhenCreatorIsNotAMemberOfTheOrganization() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(orgMemberClient.isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy()))
                .thenReturn(false);

        OrgMemberNotFoundException exception = assertThrows(OrgMemberNotFoundException.class,
                () -> groupService.createGroupByBoardId(groupDTO, boardId));

        assertEquals("Member " + groupDTO.getCreatedBy() + " is not a member of organization " + board.getOrgId(),
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy());
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void createGroupByBoardId_shouldThrowWhenOrganizationServiceIsUnavailable() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(orgMemberClient.isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy()))
                .thenThrow(new RestClientException("connection refused"));

        assertThrows(OrgServiceUnavailableException.class,
                () -> groupService.createGroupByBoardId(groupDTO, boardId));

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy());
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldThrowPositionAlreadyExistsExceptionWhenPositionExistsUnderBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(orgMemberClient.isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy()))
                .thenReturn(true);

        when(groupRepository.existsByBoardIdAndPosition(boardId, position))
                .thenReturn(true);

        PositionAlreadyExistsException exception = assertThrows(PositionAlreadyExistsException.class,
                () -> groupService.createGroupByBoardId(groupDTO, boardId)
        );

        assertEquals("Position " + position + " already exists for board id: " + boardId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy());
        verify(groupRepository).existsByBoardIdAndPosition(boardId, position);
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldGetGroupsByBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(groupRepository.findByBoardId(boardId))
                .thenReturn(List.of(groupEntity));

        when(modelMapper.map(any(Group.class), eq(GroupDTO.class)))
                .thenReturn(groupDTO);

        List<GroupDTO> result = groupService.getGroupsByBoardId(boardId);

        assertEquals(1, result.size());
        assertEquals(groupDTO.getBoardId(), result.get(0).getBoardId());
        assertEquals(groupDTO.getGroupName(), result.get(0).getGroupName());
        assertEquals(groupDTO.getPosition(), result.get(0).getPosition());
        assertEquals(groupDTO.getCreatedBy(), result.get(0).getCreatedBy());

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findByBoardId(boardId);
        verify(modelMapper).map(any(Group.class), eq(GroupDTO.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoGroupsExistForBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(groupRepository.findByBoardId(boardId))
                .thenReturn(List.of());

        List<GroupDTO> result = groupService.getGroupsByBoardId(boardId);

        assertTrue(result.isEmpty());

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findByBoardId(boardId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenGettingGroupsForUnknownBoard() {

        when(boardRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> groupService.getGroupsByBoardId(invalidBoardId)
        );

        assertEquals("Board not found with id: " + invalidBoardId, exception.getMessage());

        verify(boardRepository).findById(invalidBoardId);
        verify(groupRepository, never()).findByBoardId(anyLong());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldDeleteGroup() {

        groupRepository.deleteById(groupId);

        verify(groupRepository).deleteById(groupId);
        verify(groupRepository, never()).existsById(anyLong());
        verify(groupRepository, never()).deleteById(invalidGroupId);

    }

    @Test
    void shouldPatchGroupSuccessfully() {
        GroupUpdateDTO updateDTO = GroupTestDataFactory.createGroupUpdateDto();
        GroupDTO updatedDTO = GroupTestDataFactory.createGroupDto();
        updatedDTO.setGroupName(updateDTO.getGroupName());
        updatedDTO.setPosition(updateDTO.getPosition());

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupRepository.existsByBoardIdAndPosition(boardId, updateDTO.getPosition())).thenReturn(false);
        doNothing().when(modelMapper).map(any(GroupUpdateDTO.class), any(Group.class));
        when(groupRepository.save(groupEntity)).thenReturn(groupEntity);
        when(modelMapper.map(groupEntity, GroupDTO.class)).thenReturn(updatedDTO);

        GroupDTO result = groupService.patchGroupById(boardId, groupId, updateDTO);

        assertNotNull(result);
        assertEquals(updateDTO.getGroupName(), result.getGroupName());
        assertEquals(updateDTO.getPosition(), result.getPosition());

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findById(groupId);
        verify(groupRepository).existsByBoardIdAndPosition(boardId, updateDTO.getPosition());
        verify(modelMapper).map(updateDTO, groupEntity);
        verify(groupRepository).save(groupEntity);
        verify(modelMapper).map(groupEntity, GroupDTO.class);
    }

    @Test
    void shouldPatchGroupSuccessfullyWhenPositionIsSame() {
        GroupUpdateDTO updateDTO = GroupUpdateDTO.builder()
                .groupName("New Name")
                .position(groupEntity.getPosition())
                .build();
        GroupDTO updatedDTO = GroupTestDataFactory.createGroupDto();
        updatedDTO.setGroupName("New Name");

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
        doNothing().when(modelMapper).map(any(GroupUpdateDTO.class), any(Group.class));
        when(groupRepository.save(groupEntity)).thenReturn(groupEntity);
        when(modelMapper.map(groupEntity, GroupDTO.class)).thenReturn(updatedDTO);

        GroupDTO result = groupService.patchGroupById(boardId, groupId, updateDTO);

        assertNotNull(result);
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(modelMapper).map(updateDTO, groupEntity);
        verify(groupRepository).save(groupEntity);
    }

    @Test
    void shouldPatchGroupSuccessfullyWhenPositionIsNull() {
        GroupUpdateDTO updateDTO = GroupUpdateDTO.builder()
                .groupName("New Name")
                .position(null)
                .build();
        GroupDTO updatedDTO = GroupTestDataFactory.createGroupDto();
        updatedDTO.setGroupName("New Name");

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
        doNothing().when(modelMapper).map(any(GroupUpdateDTO.class), any(Group.class));
        when(groupRepository.save(groupEntity)).thenReturn(groupEntity);
        when(modelMapper.map(groupEntity, GroupDTO.class)).thenReturn(updatedDTO);

        GroupDTO result = groupService.patchGroupById(boardId, groupId, updateDTO);

        assertNotNull(result);
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(modelMapper).map(updateDTO, groupEntity);
        verify(groupRepository).save(groupEntity);
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenBoardNotFoundOnPatch() {
        GroupUpdateDTO updateDTO = GroupTestDataFactory.createGroupUpdateDto();

        when(boardRepository.findById(invalidBoardId)).thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class,
                () -> groupService.patchGroupById(invalidBoardId, groupId, updateDTO));

        verify(boardRepository).findById(invalidBoardId);
        verify(groupRepository, never()).findById(anyLong());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenGroupNotFoundOnPatch() {
        GroupUpdateDTO updateDTO = GroupTestDataFactory.createGroupUpdateDto();

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(invalidGroupId)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class,
                () -> groupService.patchGroupById(boardId, invalidGroupId, updateDTO));

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findById(invalidGroupId);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenGroupBelongsToDifferentBoardOnPatch() {
        GroupUpdateDTO updateDTO = GroupTestDataFactory.createGroupUpdateDto();
        Group otherBoardGroup = GroupTestDataFactory.createGroupEntity();
        otherBoardGroup.setBoardId(999L);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(otherBoardGroup));

        assertThrows(GroupNotFoundException.class,
                () -> groupService.patchGroupById(boardId, groupId, updateDTO));

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findById(groupId);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void shouldThrowPositionAlreadyExistsExceptionWhenPositionOccupiedOnPatch() {
        GroupUpdateDTO updateDTO = GroupTestDataFactory.createGroupUpdateDto();

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
        when(groupRepository.existsByBoardIdAndPosition(boardId, updateDTO.getPosition())).thenReturn(true);

        assertThrows(PositionAlreadyExistsException.class,
                () -> groupService.patchGroupById(boardId, groupId, updateDTO));

        verify(boardRepository).findById(boardId);
        verify(groupRepository).findById(groupId);
        verify(groupRepository).existsByBoardIdAndPosition(boardId, updateDTO.getPosition());
        verify(groupRepository, never()).save(any());
    }

}
