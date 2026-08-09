package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.serviceImpl.TaskServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskDTO taskDTO;
    private Task taskEntity;
    private Long boardId;
    private Long invalidBoardId;
    private Board board;

    @BeforeEach
    void setUp() {
        taskDTO = TaskTestDataFactory.createTaskDto();
        taskEntity = TaskTestDataFactory.createTaskEntity();
        boardId = TaskTestDataFactory.BOARD_ID;
        invalidBoardId = TaskTestDataFactory.INVALID_BOARD_ID;
        board = BoardTestDataFactory.createBoardEntity();

    }

    @Test
    void shouldCreateTaskByBoardId() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(modelMapper.map(any(TaskDTO.class), eq(Task.class)))
                .thenReturn(taskEntity);

        when(taskRepository.save(any(Task.class)))
                .thenReturn(taskEntity);

        when(modelMapper.map(any(Task.class), eq(TaskDTO.class)))
                .thenReturn(taskDTO);

        TaskDTO result = taskService.createTaskByBoardId(taskDTO, boardId);

        assertNotNull(result);
        assertEquals(taskDTO.getBoardId(), result.getBoardId());
        assertEquals(taskDTO.getGroupId(), result.getGroupId());
        assertEquals(taskDTO.getTitle(), result.getTitle());
        assertEquals(taskDTO.getPosition(), result.getPosition());
        assertEquals(taskDTO.getCreatedBy(), result.getCreatedBy());

        verify(boardRepository).findById(boardId);
        verify(modelMapper).map(any(TaskDTO.class), eq(Task.class));
        verify(taskRepository).save(any(Task.class));
        verify(modelMapper).map(any(Task.class), eq(TaskDTO.class));

    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        BoardNotFoundException boardNotFoundException = assertThrows(BoardNotFoundException.class,
                () -> taskService.createTaskByBoardId(taskDTO, invalidBoardId)
        );
    }

    @Test
    void shouldGetTasksByBoardIdSuccessfully() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(taskRepository.findByBoardId(boardId))
                .thenReturn(List.of(taskEntity));

        when(modelMapper.map(taskEntity, TaskDTO.class))
                .thenReturn(taskDTO);

        List<TaskDTO> result = taskService.getTasksByBoardId(boardId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskDTO.getBoardId(), result.get(0).getBoardId());
        assertEquals(taskDTO.getGroupId(), result.get(0).getGroupId());
        assertEquals(taskDTO.getTitle(), result.get(0).getTitle());
        assertEquals(taskDTO.getPosition(), result.get(0).getPosition());

        verify(boardRepository).findById(boardId);
        verify(taskRepository).findByBoardId(boardId);
        verify(modelMapper).map(taskEntity, TaskDTO.class);
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenGettingTasksForUnknownBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> taskService.getTasksByBoardId(boardId)
        );

        assertEquals("Board not found with id: " + boardId, exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(taskRepository, never()).findByBoardId(boardId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksExistForBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.ofNullable(board));

        when(taskRepository.findByBoardId(boardId))
                .thenReturn(List.of());

        List<TaskDTO> result = taskService.getTasksByBoardId(boardId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(boardRepository).findById(boardId);
        verify(taskRepository).findByBoardId(boardId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldPatchTaskFieldsInServiceImpl() {
        Long boardId = 1L;
        Long taskId = 100L;

        TaskUpdateDTO incomingPatchDto = new TaskUpdateDTO();
        incomingPatchDto.setTitle("Updated Title via Patch");
        incomingPatchDto.setPosition(3);

        Board board = new Board();
        Task taskEntity = new Task();
        taskEntity.setTaskId(taskId);
        taskEntity.setBoardId(boardId);
        taskEntity.setTitle("Original Old Title");
        taskEntity.setPosition(1);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO expectedResponseDto = TaskTestDataFactory.createTaskDto();
        expectedResponseDto.setBoardId(boardId);
        expectedResponseDto.setTaskId(taskId);
        expectedResponseDto.setTitle("Updated Title via Patch");
        expectedResponseDto.setPosition(3);

        when(modelMapper.map(any(Task.class), eq(TaskDTO.class)))
                .thenReturn(expectedResponseDto);

        TaskDTO result = taskService.patchTaskById(boardId, taskId, incomingPatchDto);

        assertNotNull(result);
        assertEquals("Updated Title via Patch", result.getTitle());
        assertEquals(3, result.getPosition());

        verify(boardRepository, times(1)).findById(boardId);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(taskEntity);
    }

}
