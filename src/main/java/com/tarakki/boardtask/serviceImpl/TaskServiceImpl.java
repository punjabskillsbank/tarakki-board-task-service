package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Task;
import com.tarakki.boardtask.exception.BoardNotFoundException;
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
    public TaskDTO patchTask(Long boardId, Long taskId, TaskDTO taskDTO) {
        // 1. Task ko boardId aur taskId se find karo
        Task task = taskRepository.findByTaskIdAndBoardId(taskId, boardId)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + taskId + " for board id " + boardId));

        // 2. Sirf wahi fields update karo jo client ne bheji hain (PATCH logic)
        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getPosition() != 0) { // ya jo bhi default/valid check ho
            task.setPosition(taskDTO.getPosition());
        }

        // 3. Database mein save karo
        Task savedTask = taskRepository.save(task);

        // 4. Entity ko DTO mein convert karke return karo
        return modelMapper.map(savedTask, TaskDTO.class);
    }



}
