package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.TaskDTO;

import java.util.List;

public interface TaskService {
    TaskDTO createTaskByBoardId(TaskDTO taskDTO, Long boardId);

    List<TaskDTO> getTasksByBoardId(Long boardId);

    TaskDTO patchTask(Long boardId, Long taskId, TaskDTO taskDTO);
}
