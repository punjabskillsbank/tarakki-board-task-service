package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.BoardUpdateDTO;

import java.util.List;

public interface BoardService {

    BoardDTO createBoard(BoardDTO boardDTO);

    void deleteBoard(Long boardId);

    List<BoardDTO> getBoardsByOrganization(Long orgId);

    BoardDTO getBoardById(Long boardId);

    BoardDTO patchBoardById(Long boardId, BoardUpdateDTO boardUpdateDTO);
}

