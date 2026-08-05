package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardMemberDTO;

public interface BoardMemberService {

    BoardMemberDTO addMemberToBoard(Long boardId, Long orgMemberId);
}
