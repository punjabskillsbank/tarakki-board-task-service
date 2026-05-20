package com.tarakki.boardtask.controller;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@Valid @RequestBody BoardDTO boardDTO) {

        BoardDTO result = boardService.createBoard(boardDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Integer> deleteBoard(@PathVariable Long boardId) {
        int deletedRows = boardService.deleteBoard(boardId);
        return ResponseEntity.ok(deletedRows);
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<BoardDTO>> getBoardsByOrganization(@PathVariable Long orgId) {

        List<BoardDTO> result = boardService.getBoardsByOrganization(orgId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
