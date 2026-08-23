package com.tarakki.boardtask.service;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.BoardMemberDTO;
import com.tarakki.boardtask.dto.BoardMemberRequestDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.enums.BoardRole;
import com.tarakki.boardtask.exception.BoardMemberExistsException;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotAcceptedException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.repository.BoardMemberRepository;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.common.dto.OrgMemberDTO;
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
import org.springframework.web.client.RestClientException;

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
    private OrgMemberClient orgMemberClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BoardMemberServiceImpl boardMemberService;

    @Captor
    private ArgumentCaptor<BoardMemberDTO> boardMemberDtoCaptor;

    private Board board;
    private BoardMember boardMemberEntity;
    private BoardMemberDTO boardMemberDTO;
    private OrgMemberDTO orgMemberDTO;
    private BoardMemberRequestDTO boardMemberRequestDTO;
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
        orgMemberDTO = BoardMemberTestDataFactory.createOrgMemberDto();
        boardMemberRequestDTO = BoardMemberTestDataFactory.createBoardMemberRequestDto();
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

        when(orgMemberClient.findOrgMemberById(orgId, orgMemberId))
                .thenReturn(orgMemberDTO);

        when(boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId))
                .thenReturn(false);

        when(modelMapper.map(any(BoardMemberDTO.class), eq(BoardMember.class)))
                .thenReturn(boardMemberEntity);

        when(boardMemberRepository.save(any(BoardMember.class)))
                .thenReturn(boardMemberEntity);

        when(modelMapper.map(any(BoardMember.class), eq(BoardMemberDTO.class)))
                .thenReturn(boardMemberDTO);

        BoardMemberDTO result = boardMemberService.addMemberToBoard(boardId, orgMemberId, boardMemberRequestDTO);

        assertNotNull(result);
        assertEquals(boardMemberDTO.getBoardId(), result.getBoardId());
        assertEquals(boardMemberDTO.getMemberId(), result.getMemberId());
        assertEquals(boardMemberDTO.getRole(), result.getRole());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).findOrgMemberById(orgId, orgMemberId);
        verify(boardMemberRepository).existsByBoardIdAndMemberId(boardId, memberId);
        verify(modelMapper).map(boardMemberDtoCaptor.capture(), eq(BoardMember.class));
        verify(boardMemberRepository).save(any(BoardMember.class));
        verify(modelMapper).map(any(BoardMember.class), eq(BoardMemberDTO.class));

        BoardMemberDTO mappedBoardMember = boardMemberDtoCaptor.getValue();
        assertEquals(boardId, mappedBoardMember.getBoardId());
        assertEquals(memberId, mappedBoardMember.getMemberId());
        assertEquals(BoardRole.MEMBER, mappedBoardMember.getRole());
        assertFalse(mappedBoardMember.getCanEdit());
        assertTrue(mappedBoardMember.getCanView());
    }

    @Test
    void addMemberToBoard_shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(invalidBoardId))
                .thenReturn(Optional.empty());

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> boardMemberService.addMemberToBoard(invalidBoardId, orgMemberId, boardMemberRequestDTO)
        );

        assertEquals("Board not found with id: " + invalidBoardId, exception.getMessage());

        verify(boardRepository).findById(invalidBoardId);
        verify(orgMemberClient, never()).findOrgMemberById(anyLong(), anyLong());
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowOrgMemberNotFoundExceptionWhenOrgMemberIsNotInBoardOrganization() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(orgMemberClient.findOrgMemberById(orgId, invalidOrgMemberId))
                .thenReturn(null);

        OrgMemberNotFoundException exception = assertThrows(OrgMemberNotFoundException.class,
                () -> boardMemberService.addMemberToBoard(boardId, invalidOrgMemberId, boardMemberRequestDTO)
        );

        assertEquals("Org member " + invalidOrgMemberId + " not found in organization " + orgId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).findOrgMemberById(orgId, invalidOrgMemberId);
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowOrgMemberNotAcceptedExceptionWhenOrgMemberHasNotAcceptedInvite() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(orgMemberClient.findOrgMemberById(orgId, orgMemberId))
                .thenReturn(BoardMemberTestDataFactory.createPendingOrgMemberDto());

        OrgMemberNotAcceptedException exception = assertThrows(OrgMemberNotAcceptedException.class,
                () -> boardMemberService.addMemberToBoard(boardId, orgMemberId, boardMemberRequestDTO)
        );

        assertEquals("Org member " + orgMemberId + " has not accepted the invite to organization " + orgId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).findOrgMemberById(orgId, orgMemberId);
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowBoardMemberExistsExceptionWhenMemberIsAlreadyOnBoard() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(orgMemberClient.findOrgMemberById(orgId, orgMemberId))
                .thenReturn(orgMemberDTO);

        when(boardMemberRepository.existsByBoardIdAndMemberId(boardId, memberId))
                .thenReturn(true);

        BoardMemberExistsException exception = assertThrows(BoardMemberExistsException.class,
                () -> boardMemberService.addMemberToBoard(boardId, orgMemberId, boardMemberRequestDTO)
        );

        assertEquals("Member " + memberId + " is already a member of board " + boardId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).findOrgMemberById(orgId, orgMemberId);
        verify(boardMemberRepository).existsByBoardIdAndMemberId(boardId, memberId);
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void addMemberToBoard_shouldThrowOrgServiceUnavailableExceptionWhenOrganizationServiceIsUnreachable() {

        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        when(orgMemberClient.findOrgMemberById(orgId, orgMemberId))
                .thenThrow(new RestClientException("Connection refused"));

        OrgServiceUnavailableException exception = assertThrows(OrgServiceUnavailableException.class,
                () -> boardMemberService.addMemberToBoard(boardId, orgMemberId, boardMemberRequestDTO)
        );

        assertEquals("Unable to reach organization service to look up members of organization " + orgId,
                exception.getMessage());

        verify(boardRepository).findById(boardId);
        verify(orgMemberClient).findOrgMemberById(orgId, orgMemberId);
        verify(boardMemberRepository, never()).existsByBoardIdAndMemberId(anyLong(), any(UUID.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verify(modelMapper, never()).map(any(), any());
    }
}
