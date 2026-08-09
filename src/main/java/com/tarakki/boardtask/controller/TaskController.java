package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.dto.TaskUpdateDTO;
import com.tarakki.boardtask.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{boardId}/task")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTaskByBoardId(@Valid @RequestBody TaskDTO taskDTO, @PathVariable Long boardId) {
        TaskDTO result = taskService.createTaskByBoardId(taskDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasksByBoardId(@PathVariable Long boardId) {
        List<TaskDTO> result = taskService.getTasksByBoardId(boardId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDTO> patchTaskById(
            @PathVariable Long boardId,
            @PathVariable Long taskId,
            @RequestBody TaskUpdateDTO taskUpdateDTO) {

        TaskDTO updatedTask = taskService.patchTaskById(boardId, taskId, taskUpdateDTO);
        return ResponseEntity.ok(updatedTask);
    }
}
