package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.common.entity.Task;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.RandomAccess;
import java.util.UUID;

public class TaskTestDataFactory {

    public static final Long GROUP_ID = 1L;
    public static final int POSITION = 2;
    public static final String TITLE = "Demo title";
    public static final UUID CREATED_BY = UUID.randomUUID();
    public static final Long BOARD_ID  = 1L;

    public static TaskDTO createTaskDto() {
        return new TaskDTO(GROUP_ID, TITLE, POSITION, CREATED_BY);
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
