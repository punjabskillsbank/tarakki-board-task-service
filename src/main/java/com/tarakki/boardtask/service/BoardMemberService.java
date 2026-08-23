package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;

public interface BoardMemberService {

    BoardMemberDTO addMemberToBoard(Long boardId, Long orgMemberId, BoardMemberRequestDTO request);
}
