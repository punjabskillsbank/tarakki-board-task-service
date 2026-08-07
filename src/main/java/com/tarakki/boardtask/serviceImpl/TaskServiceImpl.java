package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Task;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.TaskNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.service.TaskService;
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
    public TaskDTO createTaskByBoardId(TaskDTO taskDTO, Long boardId) {

        Board boards = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Task task = modelMapper.map(taskDTO, Task.class);

        taskRepository.save(task);

        return modelMapper.map(task, TaskDTO.class);
    }

    @Override
    public List<TaskDTO> getTasksByBoardId(Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        List<Task> tasks = taskRepository.findByBoardId(boardId);
        return tasks.stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public TaskDTO patchTask(Long boardId, Long taskId, TaskDTO taskDTO) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }

        if (taskDTO.getPosition() != 0) {
            task.setPosition(taskDTO.getPosition());
        }

        if (taskDTO.getGroupId() != null) {
            task.setGroupId(taskDTO.getGroupId());
        }

        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskDTO.class);
    }

}
