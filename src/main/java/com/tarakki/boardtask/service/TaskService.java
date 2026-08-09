package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;

import java.util.List;

public interface TaskService {
    TaskDTO createTaskByBoardId(TaskDTO taskDTO, Long boardId);

    List<TaskDTO> getTasksByBoardId(Long boardId);

    TaskDTO patchTaskById(Long boardId, Long taskId, TaskUpdateDTO taskUpdateDTO);
}
