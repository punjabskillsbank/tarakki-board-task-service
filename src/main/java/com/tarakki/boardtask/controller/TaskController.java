package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/{boardId}")
    public ResponseEntity<TaskDTO> createTaskByBoardId(@Valid @RequestBody TaskDTO taskDTO, @PathVariable Long boardId) {
        TaskDTO result = taskService.createTaskByBoardId(taskDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
