package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.GroupRepository;
import com.tarakki.boardtask.serviceImpl.GroupServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.GroupTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private BoardRepository boardRepository;

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

    @BeforeEach
    void setUp() {
        groupDTO = GroupTestDataFactory.createGroupDto();
        groupEntity = GroupTestDataFactory.createGroupEntity();
        boardId = GroupTestDataFactory.BOARD_ID;
        invalidBoardId = GroupTestDataFactory.INVALID_BOARD_ID;
        position = GroupTestDataFactory.POSITION;
        board = BoardTestDataFactory.createBoardEntity();
    }

    @Test
    void shouldCreateGroupByBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

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
        verify(groupRepository).existsByBoardIdAndPosition(boardId, position);
        verify(modelMapper).map(any(GroupDTO.class), eq(Group.class));
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
        verify(groupRepository, never()).existsByBoardIdAndPosition(anyLong(), anyInt());
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldThrowPositionAlreadyExistsExceptionWhenPositionExistsUnderBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(groupRepository.existsByBoardIdAndPosition(boardId, position))
                .thenReturn(true);

        PositionAlreadyExistsException exception = assertThrows(PositionAlreadyExistsException.class,
                () -> groupService.createGroupByBoardId(groupDTO, boardId)
        );

        assertEquals("Position " + position + " already exists for board id: " + boardId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(groupRepository).existsByBoardIdAndPosition(boardId, position);
        verify(groupRepository, never()).save(any(Group.class));
        verify(modelMapper, never()).map(any(), any());
    }
}
