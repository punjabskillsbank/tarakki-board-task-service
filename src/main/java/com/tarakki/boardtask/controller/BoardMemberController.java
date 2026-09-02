package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;
import com.tarakki.boardtask.service.BoardMemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/members")
@AllArgsConstructor
public class BoardMemberController {

    private final BoardMemberService boardMemberService;

    @PostMapping("/{orgMemberId}")
    public ResponseEntity<BoardMemberDTO> addMemberToBoard(@PathVariable Long boardId,
                                                           @PathVariable Long orgMemberId,
                                                           @Valid @RequestBody BoardMemberRequestDTO request) {
        BoardMemberDTO result = boardMemberService.addMemberToBoard(boardId, orgMemberId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
