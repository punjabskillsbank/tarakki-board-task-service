package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.service.GroupService;
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

    @Auditable(eventName = "DELETE_GROUP", entityName = "GROUP", entityClass = Group.class, entityIdArgSpel = "#groupId")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId, @PathVariable String boardId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

}
