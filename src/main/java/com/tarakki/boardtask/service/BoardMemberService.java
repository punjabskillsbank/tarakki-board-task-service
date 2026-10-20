package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;
import com.tarakki.boardtask.dto.BoardMemberUpdateDTO;

import java.util.List;

public interface BoardMemberService {

    BoardMemberDTO addMemberToBoard(Long boardId, Long orgMemberId, BoardMemberRequestDTO request);

    BoardMemberDTO getBoardMemberById(Long boardId, Long boardMemberId);

    List<BoardMemberDTO> getBoardMembersByBoardId(Long boardId);

    BoardMemberDTO patchBoardMemberById(Long boardId, Long boardMemberId, BoardMemberUpdateDTO boardMemberUpdateDTO);
}
