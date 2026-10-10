package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;
import com.tarakki.boardtask.dto.BoardMemberUpdateDTO;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.service.BoardMemberService;
import com.tarakki.common.audit.annotation.Auditable;
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

    @GetMapping("/{boardMemberId}")
    public ResponseEntity<BoardMemberDTO> getBoardMemberById(@PathVariable Long boardId,
                                                             @PathVariable Long boardMemberId) {
        BoardMemberDTO result = boardMemberService.getBoardMemberById(boardId, boardMemberId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Auditable(eventName = "BOARD_MEMBER_UPDATED", entityName = "BOARD_MEMBER", entityClass = BoardMember.class, entityIdArgSpel = "#boardMemberId")
    @PatchMapping("/{boardMemberId}")
    public ResponseEntity<BoardMemberDTO> patchBoardMemberById(
            @PathVariable Long boardId,
            @PathVariable Long boardMemberId,
            @RequestBody BoardMemberUpdateDTO boardMemberUpdateDTO) {
        BoardMemberDTO result = boardMemberService.patchBoardMemberById(boardId, boardMemberId, boardMemberUpdateDTO);
        return ResponseEntity.ok(result);
    }
}
