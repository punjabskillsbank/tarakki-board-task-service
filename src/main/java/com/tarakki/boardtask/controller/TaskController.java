package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

<<<<<<< HEAD
    @PostMapping("{boardId}")
=======
    @PostMapping("/api/tasks/{boardId}")
>>>>>>> 57f4fd8 (TK_93: PATCH endpoint to edit particular task by it's and board ID)
    public ResponseEntity<TaskDTO> createTaskByBoardId(@Valid @RequestBody TaskDTO taskDTO, @PathVariable Long boardId) {
        TaskDTO result = taskService.createTaskByBoardId(taskDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

<<<<<<< HEAD
    @GetMapping("{boardId}")
=======
    @GetMapping("/api/tasks/{boardId}")
>>>>>>> 57f4fd8 (TK_93: PATCH endpoint to edit particular task by it's and board ID)
    public ResponseEntity<List<TaskDTO>> getTasksByBoardId(@PathVariable Long boardId) {
        List<TaskDTO> result = taskService.getTasksByBoardId(boardId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

<<<<<<< HEAD
=======
    @PatchMapping("/api/boards/{boardId}/tasks/{taskId}")
        public ResponseEntity<TaskDTO> patchTask(
                @PathVariable Long boardId,
                @PathVariable Long taskId,
                @RequestBody TaskDTO taskDTO) {

            TaskDTO updatedTask = taskService.patchTask(boardId, taskId, taskDTO);
            return ResponseEntity.ok(updatedTask);
    }
>>>>>>> 57f4fd8 (TK_93: PATCH endpoint to edit particular task by it's and board ID)
}
