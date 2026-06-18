package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.common.entity.Board;
import com.tarakki.common.entity.Task;
import com.tarakki.common.exceptionHandling.BoardNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TaskDTO createTaskBySpecifiedBoardId(TaskDTO taskDTO, Long boardId) {

        Board boards = boardRepository.findById(boardId)
                .orElseThrow(()->  new BoardNotFoundException(boardId)); // if board is not present of given id , throws an Exception

        Task task = modelMapper.map(taskDTO, Task.class);

        task.setBoardId(boardId);

        taskRepository.save(task);

        return modelMapper.map(task, TaskDTO.class);
    }
}
