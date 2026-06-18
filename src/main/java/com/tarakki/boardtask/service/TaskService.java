package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.TaskDTO;

public interface TaskService {
    TaskDTO createTaskBySpecifiedBoardId(TaskDTO taskDTO, Long boardId);

}
