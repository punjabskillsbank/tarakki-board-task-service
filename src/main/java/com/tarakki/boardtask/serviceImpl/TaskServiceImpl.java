package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Task;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.exception.TaskNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final OrgMemberClient orgMemberClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TaskDTO createTaskByBoardId(TaskDTO taskDTO, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        if (!isMemberInOrganization(board.getOrgId(), taskDTO.getCreatedBy())) {
            throw new OrgMemberNotFoundException(taskDTO.getCreatedBy(), board.getOrgId());
        }

        taskDTO.setBoardId(boardId);

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
    public TaskDTO patchTaskById(Long boardId, Long taskId, TaskUpdateDTO taskUpdateDTO) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        modelMapper.map(taskUpdateDTO, task);

        Task updatedTask = taskRepository.save(task);
        return modelMapper.map(updatedTask, TaskDTO.class);
    }

    private boolean isMemberInOrganization(Long orgId, UUID memberId) {
        try {
            return orgMemberClient.isMemberInOrganization(orgId, memberId);
        } catch (RestClientException exception) {
            throw new OrgServiceUnavailableException(orgId);
        }
    }
}
