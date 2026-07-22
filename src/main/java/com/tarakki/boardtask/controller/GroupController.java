package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.service.GroupService;
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

    @PostMapping
    public ResponseEntity<GroupDTO> createGroupByBoardId(@Valid @RequestBody GroupDTO groupDTO, @PathVariable Long boardId) {
        GroupDTO result = groupService.createGroupByBoardId(groupDTO, boardId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
