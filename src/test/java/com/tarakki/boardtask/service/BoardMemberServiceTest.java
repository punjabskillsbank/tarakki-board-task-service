package com.tarakki.boardtask.service;

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
import com.tarakki.boardtask.serviceImpl.BoardMemberServiceImpl;
import com.tarakki.boardtask.util.BoardMemberTestDataFactory;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BoardMemberServiceImpl boardMemberService;

    @Captor
    private ArgumentCaptor<BoardMember> boardMemberCaptor;

    private Board board;
    private BoardMember boardMemberEntity;
    private BoardMemberDTO boardMemberDTO;
    private Long boardId;
    private Long invalidBoardId;
    private Long orgId;
    private Long orgMemberId;
    private Long invalidOrgMemberId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        board = BoardTestDataFactory.createBoardEntity();
        boardMemberEntity = BoardMemberTestDataFactory.createBoardMemberEntity();
        boardMemberDTO = BoardMemberTestDataFactory.createBoardMemberDto();
        boardId = BoardMemberTestDataFactory.BOARD_ID;
        invalidBoardId = BoardMemberTestDataFactory.INVALID_BOARD_ID;
        orgId = BoardMemberTestDataFactory.ORG_ID;
        orgMemberId = BoardMemberTestDataFactory.ORG_MEMBER_ID;
        invalidOrgMemberId = BoardMemberTestDataFactory.INVALID_ORG_MEMBER_ID;
        memberId = BoardMemberTestDataFactory.MEMBER_ID;
    }

    @Test
    void addMemberToBoard_shouldAddOrgMemberToBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(organizationRepository.existsOrgMemberInOrganization(orgMemberId, orgId))
                .thenReturn(true);

        when(organizationRepository.findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId))
                .thenReturn(Optional.of(memberId));

        when(boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId))
                .thenReturn(false);

        when(boardMemberRepository.save(any(BoardMember.class)))
                .thenReturn(boardMemberEntity);

        when(modelMapper.map(any(BoardMember.class), eq(BoardMemberDTO.class)))
                .thenReturn(boardMemberDTO);

        BoardMemberDTO result = boardMemberService.addMemberToBoard(boardId, orgMemberId);

        assertNotNull(result);
        assertEquals(boardMemberDTO.getBoardId(), result.getBoardId());
        assertEquals(boardMemberDTO.getMemberId(), result.getMemberId());
        assertEquals(boardMemberDTO.getRole(), result.getRole());

        verify(boardRepository).findById(boardId);
        verify(organizationRepository).existsOrgMemberInOrganization(orgMemberId, orgId);
        verify(organizationRepository).findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId);
        verify(boardMemberRepository).existsByBoardIdAndMemberId(boardId, memberId);
        verify(boardMemberRepository).save(boardMemberCaptor.capture());
        verify(modelMapper).map(any(BoardMember.class), eq(BoardMemberDTO.class));

        BoardMember savedBoardMember = boardMemberCaptor.getValue();
        assertEquals(boardId, savedBoardMember.getBoardId());
        assertEquals(memberId, savedBoardMember.getMemberId());
        assertEquals(BoardRole.MEMBER, savedBoardMember.getRole());
        assertFalse(savedBoardMember.isCanEdit());
        assertTrue(savedBoardMember.isCanView());
    }

    @Test
    void addMemberToBoard_shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(invalidBoardId))
                .thenReturn(Optional.empty());

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> boardMemberService.addMemberToBoard(invalidBoardId, orgMemberId)
        );

        assertEquals("Board not found with id: " + invalidBoardId, exception.getMessage());

        verify(boardRepository).findById(invalidBoardId);
        verify(organizationRepository, never()).existsOrgMemberInOrganization(anyLong(), anyLong());
        verify(organizationRepository, never()).findMemberIdByOrgMemberIdAndOrgId(anyLong(), anyLong());
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowOrgMemberNotFoundExceptionWhenOrgMemberIsNotInBoardOrganization() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(organizationRepository.existsOrgMemberInOrganization(invalidOrgMemberId, orgId))
                .thenReturn(false);

        OrgMemberNotFoundException exception = assertThrows(OrgMemberNotFoundException.class,
                () -> boardMemberService.addMemberToBoard(boardId, invalidOrgMemberId)
        );

        assertEquals("Org member " + invalidOrgMemberId + " not found in organization " + orgId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(organizationRepository).existsOrgMemberInOrganization(invalidOrgMemberId, orgId);
        verify(organizationRepository, never()).findMemberIdByOrgMemberIdAndOrgId(anyLong(), anyLong());
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowOrgMemberNotRegisteredExceptionWhenMemberIdIsNull() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(organizationRepository.existsOrgMemberInOrganization(orgMemberId, orgId))
                .thenReturn(true);

        when(organizationRepository.findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId))
                .thenReturn(Optional.empty());

        OrgMemberNotRegisteredException exception = assertThrows(OrgMemberNotRegisteredException.class,
                () -> boardMemberService.addMemberToBoard(boardId, orgMemberId)
        );

        assertEquals("Org member " + orgMemberId + " has no registered account yet",
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(organizationRepository).existsOrgMemberInOrganization(orgMemberId, orgId);
        verify(organizationRepository).findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId);
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowMemberAlreadyOnBoardExceptionWhenMemberIsAlreadyOnBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(organizationRepository.existsOrgMemberInOrganization(orgMemberId, orgId))
                .thenReturn(true);

        when(organizationRepository.findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId))
                .thenReturn(Optional.of(memberId));

        when(boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId))
                .thenReturn(true);

        MemberAlreadyOnBoardException exception = assertThrows(MemberAlreadyOnBoardException.class,
                () -> boardMemberService.addMemberToBoard(boardId, orgMemberId)
        );

        assertEquals("Member " + memberId + " is already a member of board " + boardId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(organizationRepository).existsOrgMemberInOrganization(orgMemberId, orgId);
        verify(organizationRepository).findMemberIdByOrgMemberIdAndOrgId(orgMemberId, orgId);
        verify(boardMemberRepository).existsByBoardIdAndMemberId(boardId, memberId);
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }
}
