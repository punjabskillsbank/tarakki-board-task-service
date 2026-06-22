package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.serviceImpl.TaskServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.TaskTestDataFactory;
import com.tarakki.common.entity.Board;
import com.tarakki.boardtask.entity.Task;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
