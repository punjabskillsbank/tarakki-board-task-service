package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.dto.GroupUpdateDTO;
import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.service.GroupService;
import com.tarakki.boardtask.service.TaskService;
import com.tarakki.common.audit.annotation.Auditable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/groups")
@AllArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroupByBoardId(@Valid @RequestBody GroupDTO groupDTO, @PathVariable Long boardId) {
        GroupDTO result = groupService.createGroupByBoardId(groupDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getGroupsByBoardId(@PathVariable Long boardId) {
        List<GroupDTO> result = groupService.getGroupsByBoardId(boardId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Auditable(eventName = "GROUP_DELETED", entityName = "GROUP", entityClass = Group.class, entityIdArgSpel = "#groupId")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId, @PathVariable Long boardId) {
        groupService.deleteGroup(groupId, boardId);
        return ResponseEntity.noContent().build();
    }

    @Auditable(eventName = "GROUP_UPDATED", entityName = "GROUP", entityClass = Group.class, entityIdArgSpel = "#groupId")
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupDTO> patchGroupById(
            @PathVariable Long boardId,
            @PathVariable Long groupId,
            @RequestBody GroupUpdateDTO groupUpdateDTO) {
        GroupDTO result = groupService.patchGroupById(boardId, groupId, groupUpdateDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{groupId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByGroupId(
            @PathVariable Long boardId,
            @PathVariable Long groupId) {
        List<TaskDTO> result = taskService.getTasksByGroupId(boardId, groupId);
        return ResponseEntity.ok(result);
    }

}
