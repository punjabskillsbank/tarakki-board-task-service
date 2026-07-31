package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Task;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.serviceImpl.BoardServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import com.tarakki.common.exceptionHandling.OrganizationNotFoundException;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    private BoardDTO dto;
    private Board board;
    private Long existingBoardId;
    private Long missingBoardId;

    @BeforeEach
    void setUp() {
        dto = BoardTestDataFactory.createBoardDTO();
        board = BoardTestDataFactory.createBoardEntity();
        existingBoardId = BoardTestDataFactory.createExistingBoardId();
        missingBoardId = BoardTestDataFactory.createMissingBoardId();
    }

    @Test
    void shouldCreateBoard() {

        when(modelMapper.map(any(BoardDTO.class), eq(Board.class)))
                .thenReturn(board);

        when(boardRepository.save(any(Board.class)))
                .thenReturn(board);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dto);

        BoardDTO result = boardService.createBoard(dto);

        assertNotNull(result);
        assertEquals(dto.getBoardName(), result.getBoardName());
        assertEquals(dto.getBoardDesc(), result.getBoardDesc());
        assertEquals(dto.getOrgId(), result.getOrgId());
        assertEquals(dto.getCreatedBy(), result.getCreatedBy());

        verify(modelMapper).map(any(BoardDTO.class), eq(Board.class));
        verify(boardRepository).save(any(Board.class));
        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldDeleteBoard() {

        doNothing().when(boardRepository).deleteById(existingBoardId);

        boardService.deleteBoard(existingBoardId);

        verify(boardRepository).deleteById(existingBoardId);
        verify(boardRepository, never()).existsById(anyLong());
    }

    @Test
    void shouldDeleteMissingBoard() {

        doNothing().when(boardRepository).deleteById(missingBoardId);

        boardService.deleteBoard(missingBoardId);

        verify(boardRepository).deleteById(missingBoardId);
    }

    @Test
    void shouldGetBoardsByOrganizationSuccessfully() {

        Long orgId = BoardTestDataFactory.VALID_ORG_ID;
        List<Board> boards = List.of(board);

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);

        when(boardRepository.findByOrgId(orgId))
                .thenReturn(boards);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dto);

        List<BoardDTO> result = boardService.getBoardsByOrganization(orgId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto.getBoardName(), result.get(0).getBoardName());
        assertEquals(dto.getBoardDesc(), result.get(0).getBoardDesc());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository).findByOrgId(orgId);
        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldThrowOrganizationNotFoundExceptionWhenOrgIdDoesNotExist() {

        Long orgId = BoardTestDataFactory.INVALID_ORG_ID;

        when(organizationRepository.existsById(orgId))
                .thenReturn(false);

        OrganizationNotFoundException exception = assertThrows(
                OrganizationNotFoundException.class,
                () -> boardService.getBoardsByOrganization(orgId)
        );

        assertEquals("Organization with id " + orgId + " not found", exception.getMessage());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository, never()).findByOrgId(anyLong());
        verify(modelMapper, never()).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldGetBoardByIdSuccessfully() {

        when(boardRepository.findById(existingBoardId))
                .thenReturn(Optional.of(board));
        when(modelMapper.map(board, BoardDTO.class))
                .thenReturn(dto);

        BoardDTO result = boardService.getBoardById(existingBoardId);

        assertNotNull(result);
        assertEquals(dto.getBoardName(), result.getBoardName());
        verify(boardRepository).findById(existingBoardId);
        verify(modelMapper).map(board, BoardDTO.class);
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(missingBoardId))
                .thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class, () -> {
            boardService.getBoardById(missingBoardId);
        });

        verify(boardRepository).findById(missingBoardId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldPatchTaskFieldsInServiceImpl() {
        Long boardId = existingBoardId;
        Long taskId = 100L;

        TaskDTO incomingPatchDto = new TaskDTO();
        incomingPatchDto.setTitle("Updated Title via Patch");
        incomingPatchDto.setPosition(3);

        Task taskEntity = new Task();
        taskEntity.setBoardId(boardId);
        taskEntity.setTitle("Original Old Title");
        taskEntity.setPosition(1);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.findByTaskIdAndBoardId(taskId, boardId))
                .thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO expectedResponseDto = TaskTestDataFactory.createTaskDto();
        expectedResponseDto.setBoardId(boardId);
        expectedResponseDto.setTitle("Updated Title via Patch");
        expectedResponseDto.setPosition(3);

        when(modelMapper.map(any(Task.class), eq(TaskDTO.class)))
                .thenReturn(expectedResponseDto);

        TaskDTO result = boardService.patchTask(boardId, taskId, incomingPatchDto);

        assertNotNull(result);
        assertEquals("Updated Title via Patch", result.getTitle());
        assertEquals(3, result.getPosition());

        verify(boardRepository, times(1)).findById(boardId);
        verify(taskRepository, times(1)).findByTaskIdAndBoardId(taskId, boardId);
        verify(taskRepository, times(1)).save(taskEntity);
    }
}
