package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.entity.Board;

import java.util.UUID;

public class BoardTestDataFactory {

    public static final Long EXISTING_BOARD_ID = 1L;
    public static final Long MISSING_BOARD_ID = 99L;
    public static final Long AUDIT_BOARD_ID = 123L;
    public static final Long AUDIT_BOARD_ID_ALT = 456L;
    public static final String AUDIT_BOARD_NAME = "My Board";
    public static final String AUDIT_BOARD_DESCRIPTION = "Description";
    public static final String AUDIT_BOARD_NAME_ALT = "Integration Board";

    public static Long createExistingBoardId() {
        return EXISTING_BOARD_ID;
    }

    public static Long createMissingBoardId() {
        return MISSING_BOARD_ID;
    }

    public static final Long VALID_ORG_ID = 1L;
    public static final Long INVALID_ORG_ID = 999L;
    public static final Long VALID_BOARD_ID = 1L;

    public static final UUID CREATED_BY = UUID.fromString("2af05d6e-b8db-4fe2-8385-d2a737332521");

    public static BoardDTO createBoardDTO() {
        BoardDTO dto = new BoardDTO();
        dto.setOrgId(VALID_ORG_ID);
        dto.setBoardName("Project Board");
        dto.setBoardDesc("Board for project tasks");
        dto.setCreatedBy(CREATED_BY);
        return dto;
    }

    public static BoardDTO createBoardDTOWithId() {
        BoardDTO dto = createBoardDTO();
        dto.setBoardId(VALID_BOARD_ID);
        return dto;
    }


    public static Board createBoardEntity() {
        Board board = new Board();
        board.setBoardId(VALID_BOARD_ID);
        board.setOrgId(VALID_ORG_ID);
        board.setBoardName("Project Board");
        board.setBoardDesc("Board for project tasks");
        board.setCreatedBy(CREATED_BY);
        return board;
    }

    public static Board createBoard() {
        return createBoardEntity();
    }

    public static Board createAuditBoard(Long boardId) {
        Board board = createBoardEntity();
        board.setBoardId(boardId);
        board.setBoardName(AUDIT_BOARD_NAME);
        board.setBoardDesc(AUDIT_BOARD_DESCRIPTION);
        return board;
    }

    public static Board createAlternateAuditBoard(Long boardId) {
        Board board = createAuditBoard(boardId);
        board.setBoardName(AUDIT_BOARD_NAME_ALT);
        return board;
    }
}
