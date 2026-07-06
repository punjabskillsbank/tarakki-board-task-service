package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.entity.Board;

import java.util.UUID;

public class BoardTestDataFactory {

    public static final Long EXISTING_BOARD_ID = 1L;
    public static final Long MISSING_BOARD_ID = 99L;

    public static Long createExistingBoardId() {
        return EXISTING_BOARD_ID;
    }

    public static Long createMissingBoardId() {
        return MISSING_BOARD_ID;
    }

    public static final Long VALID_ORG_ID = 1L;
    public static final Long INVALID_ORG_ID = 999L;
    public static final Long VALID_BOARD_ID = 1L;

    public static BoardDTO createBoardDTO() {
        BoardDTO dto = new BoardDTO();
        dto.setOrgId(VALID_ORG_ID);
        dto.setBoardName("Project Board");
        dto.setBoardDesc("Board for project tasks");
        dto.setCreatedBy(UUID.randomUUID());
        return dto;
    }

    public static Board createBoardEntity() {
        Board board = new Board();
        board.setBoardId(VALID_BOARD_ID);
        board.setOrgId(VALID_ORG_ID);
        board.setBoardName("Project Board");
        board.setBoardDesc("Board for project tasks");
        board.setCreatedBy(UUID.randomUUID());
        return board;
    }

    public static Board createBoard() {
        return createBoardEntity();
    }
}
