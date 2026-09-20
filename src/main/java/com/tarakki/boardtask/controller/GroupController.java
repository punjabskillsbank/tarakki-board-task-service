package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.service.GroupService;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.common.audit.annotation.Auditable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/groups")
@AllArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @Auditable(eventName = "GROUP_CREATED", entityName = "GROUP", entityClass = Group.class, entityIdResultSpel = "body.groupId")
    @PostMapping
    public ResponseEntity<GroupDTO> createGroupByBoardId(@Valid @RequestBody GroupDTO groupDTO, @PathVariable Long boardId) {
        GroupDTO result = groupService.createGroupByBoardId(groupDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
