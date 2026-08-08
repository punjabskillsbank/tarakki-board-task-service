package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.OrgMemberDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.enums.BoardRole;
import com.tarakki.boardtask.exception.BoardMemberExistsException;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotRegisteredException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.repository.BoardMemberRepository;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.service.BoardMemberService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
@AllArgsConstructor
public class BoardMemberServiceImpl implements BoardMemberService {

    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;
    private final OrgMemberClient orgMemberClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public BoardMemberDTO addMemberToBoard(Long boardId, Long orgMemberId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Long orgId = board.getOrgId();

        OrgMemberDTO orgMember = findOrgMember(orgId, orgMemberId);

        if (orgMember == null) {
            throw new OrgMemberNotFoundException(orgMemberId, orgId);
        }

        UUID memberId = orgMember.getMemberId();

        if (memberId == null) {
            throw new OrgMemberNotRegisteredException(orgMemberId);
        }

        if (isAlreadyOnBoard(boardId, memberId)) {
            throw new BoardMemberExistsException(memberId, boardId);
        }

        BoardMemberDTO boardMemberDTO = BoardMemberDTO.builder()
                .boardId(boardId)
                .memberId(memberId)
                .role(BoardRole.MEMBER)
                .canEdit(false)
                .canView(true)
                .build();

        BoardMember boardMember = modelMapper.map(boardMemberDTO, BoardMember.class);

        BoardMember savedBoardMember = boardMemberRepository.save(boardMember);

        return modelMapper.map(savedBoardMember, BoardMemberDTO.class);
    }

    private OrgMemberDTO findOrgMember(Long orgId, Long orgMemberId) {
        try {
            return orgMemberClient.findOrgMemberById(orgId, orgMemberId);
        } catch (RestClientException exception) {
            throw new OrgServiceUnavailableException(orgId);
        }
    }

    private boolean isAlreadyOnBoard(Long boardId, UUID memberId) {
        return boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId);
    }
}
