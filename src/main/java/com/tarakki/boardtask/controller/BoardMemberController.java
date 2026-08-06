package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.service.BoardMemberService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{boardId}/boardMembers")
@AllArgsConstructor
public class BoardMemberController {

    private final BoardMemberService boardMemberService;

    @PostMapping("/{orgMemberId}")
    public ResponseEntity<BoardMemberDTO> addMemberToBoard(@PathVariable Long boardId,
                                                           @PathVariable Long orgMemberId) {
        BoardMemberDTO result = boardMemberService.addMemberToBoard(boardId, orgMemberId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
