package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardDTO;

import java.util.List;

public interface BoardService {

    BoardDTO createBoard(BoardDTO boardDTO);

    List<BoardDTO> getBoardsByOrganization(Long orgId);
}