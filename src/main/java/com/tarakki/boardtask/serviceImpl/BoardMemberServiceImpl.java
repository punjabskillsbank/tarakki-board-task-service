package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.enums.BoardRole;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.MemberAlreadyOnBoardException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotRegisteredException;
import com.tarakki.boardtask.repository.BoardMemberRepository;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.service.BoardMemberService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class BoardMemberServiceImpl implements BoardMemberService {

    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public BoardMemberDTO addMemberToBoard(Long boardId, Long orgMemberId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Long orgId = board.getOrgId();

        if (!organizationRepository.existsOrgMemberInOrganization(orgMemberId, orgId)) {
            throw new OrgMemberNotFoundException(orgMemberId, orgId);
        }

        UUID memberId = organizationRepository.findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId)
                .orElseThrow(() -> new OrgMemberNotRegisteredException(orgMemberId));

        if (isAlreadyOnBoard(boardId, memberId)) {
            throw new MemberAlreadyOnBoardException(memberId, boardId);
        }

        BoardMember boardMember = BoardMember.builder()
                .boardId(boardId)
                .memberId(memberId)
                .role(BoardRole.MEMBER)
                .canEdit(false)
                .canView(true)
                .build();

        BoardMember savedBoardMember = boardMemberRepository.save(boardMember);

        return modelMapper.map(savedBoardMember, BoardMemberDTO.class);
    }

    private boolean isAlreadyOnBoard(Long boardId, UUID memberId) {
        return boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId);
    }
}
