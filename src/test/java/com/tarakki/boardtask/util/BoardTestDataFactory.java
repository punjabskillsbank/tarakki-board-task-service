package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.entity.Board;

import java.util.UUID;

public class BoardTestDataFactory {

    public static BoardDTO createBoardDTO() {
        BoardDTO dto = new BoardDTO();
        dto.setOrgId(1L);
        dto.setBoardName("Project Board");
        dto.setBoardDesc("Board for project tasks");
        dto.setCreatedBy(UUID.randomUUID());
        return dto;
    }

    public static Board createBoardEntity() {
        Board board = new Board();
        board.setBoardId(1L);
        board.setOrgId(1L);
        board.setBoardName("Project Board");
        board.setBoardDesc("Board for project tasks");
        board.setCreatedBy(UUID.randomUUID());
        return board;
    }
}