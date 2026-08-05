package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.enums.BoardRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class BoardMemberTestDataFactory {
    public static final Long BOARD_MEMBER_ID = 1L;
    public static final Long BOARD_ID = 1L;
    public static final Long INVALID_BOARD_ID = 999L;
    public static final Long ORG_ID = 1L;
    public static final Long ORG_MEMBER_ID = 5L;
    public static final Long INVALID_ORG_MEMBER_ID = 888L;
    public static final UUID MEMBER_ID = UUID.randomUUID();
    public static final BoardRole ROLE = BoardRole.MEMBER;
    public static final boolean CAN_EDIT = false;
    public static final boolean CAN_VIEW = true;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT = LocalDateTime.now();

    public static BoardMemberDTO createBoardMemberDto() {
        BoardMemberDTO boardMemberDTO = new BoardMemberDTO();
        boardMemberDTO.setBoardMemberId(BOARD_MEMBER_ID);
        boardMemberDTO.setBoardId(BOARD_ID);
        boardMemberDTO.setMemberId(MEMBER_ID);
        boardMemberDTO.setRole(ROLE);
        boardMemberDTO.setCanEdit(CAN_EDIT);
        boardMemberDTO.setCanView(CAN_VIEW);
        boardMemberDTO.setCreatedAt(CREATED_AT);
        boardMemberDTO.setUpdatedAt(UPDATED_AT);
        return boardMemberDTO;
    }

    public static BoardMember createBoardMemberEntity() {
        BoardMember boardMember = new BoardMember();
        boardMember.setBoardMemberId(BOARD_MEMBER_ID);
        boardMember.setBoardId(BOARD_ID);
        boardMember.setMemberId(MEMBER_ID);
        boardMember.setRole(ROLE);
        boardMember.setCanEdit(CAN_EDIT);
        boardMember.setCanView(CAN_VIEW);
        return boardMember;
    }
}
