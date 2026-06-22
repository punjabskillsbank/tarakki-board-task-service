package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Task;

import java.time.LocalDateTime;

import java.util.UUID;

public class TaskTestDataFactory {
    public static final Long TASK_ID = 10L;
    public static final Long GROUP_ID = 1L;
    public static final int POSITION = 2;
    public static final String TITLE = "Demo title";
    public static final UUID CREATED_BY = UUID.randomUUID();
    public static final Long BOARD_ID  = 1L;
    public static final Long INVALID_BOARD_ID  = 999L;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT= LocalDateTime.now();


    public static TaskDTO createTaskDto() {
        TaskDTO taskDTO= new TaskDTO();
        taskDTO.setTaskId(TASK_ID);
        taskDTO.setBoardId(BOARD_ID);
        taskDTO.setGroupId(GROUP_ID);
        taskDTO.setTitle(TITLE);
        taskDTO.setPosition(POSITION);
        taskDTO.setCreatedBy(CREATED_BY);
        taskDTO.setCreatedAt(CREATED_AT);
        taskDTO.setUpdatedAt(UPDATED_AT);
        return taskDTO;
    }

    public static Task createTaskEntity() {
        Task task = new Task();
        task.setBoardId(BOARD_ID);
        task.setGroupId(GROUP_ID);
        task.setTitle(TITLE);
        task.setPosition(POSITION);
        task.setCreatedBy(CREATED_BY);
        return task;
    }
}
