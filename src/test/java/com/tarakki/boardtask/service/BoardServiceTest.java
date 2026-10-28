package com.tarakki.boardtask.service;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.BoardUpdateDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.BoardMember;
import com.tarakki.boardtask.enums.BoardRole;
import com.tarakki.boardtask.repository.BoardMemberRepository;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.serviceImpl.BoardServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.common.entity.Organization;
import com.tarakki.common.exceptionHandling.OrganizationNotFoundException;
import org.junit.jupiter.api.AfterEach;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private OrgMemberClient orgMemberClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    private BoardDTO dto;
    private BoardDTO dtoWithId;
    private Board board;
    private Organization organization;
    private Long existingBoardId;
    private Long missingBoardId;
    private UUID authenticatedMemberId;

    @BeforeEach
    void setUp() {
        dto = BoardTestDataFactory.createBoardDTO();
        dtoWithId = BoardTestDataFactory.createBoardDTOWithId();
        board = BoardTestDataFactory.createBoardEntity();
        existingBoardId = BoardTestDataFactory.createExistingBoardId();
        missingBoardId = BoardTestDataFactory.createMissingBoardId();
        organization = BoardTestDataFactory.createOrganizationEntity();
        authenticatedMemberId = UUID.randomUUID();
        dto.setCreatedBy(UUID.randomUUID());
        dtoWithId.setCreatedBy(authenticatedMemberId);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(authenticatedMemberId.toString(), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateBoard() {

        when(orgMemberClient.isMemberInOrganization(dto.getOrgId(), authenticatedMemberId))
                .thenReturn(true);

        when(organizationRepository.findById(dto.getOrgId()))
                .thenReturn(Optional.of(organization));

        when(modelMapper.map(any(BoardDTO.class), eq(Board.class)))
                .thenReturn(board);

        when(boardRepository.save(any(Board.class)))
                .thenReturn(board);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dtoWithId);

        BoardDTO result = boardService.createBoard(dto);

        assertNotNull(result);
        assertEquals(dtoWithId.getBoardId(), result.getBoardId());
        assertEquals(dto.getBoardName(), result.getBoardName());
        assertEquals(dto.getBoardDesc(), result.getBoardDesc());
        assertEquals(dto.getOrgId(), result.getOrgId());
        assertEquals(authenticatedMemberId, result.getCreatedBy());

        verify(orgMemberClient).isMemberInOrganization(dto.getOrgId(), authenticatedMemberId);
        verify(organizationRepository).findById(dto.getOrgId());
        ArgumentCaptor<BoardDTO> mappedDto = ArgumentCaptor.forClass(BoardDTO.class);
        verify(modelMapper).map(mappedDto.capture(), eq(Board.class));
        assertEquals(authenticatedMemberId, mappedDto.getValue().getCreatedBy());
        verify(boardRepository).save(any(Board.class));

        ArgumentCaptor<BoardMember> memberCaptor = ArgumentCaptor.forClass(BoardMember.class);
        verify(boardMemberRepository).save(memberCaptor.capture());
        BoardMember savedMember = memberCaptor.getValue();
        assertEquals(board.getBoardId(), savedMember.getBoardId());
        assertEquals(organization.getOwnerId(), savedMember.getMemberId());
        assertEquals(BoardRole.BOARD_ADMIN, savedMember.getRole());
        assertTrue(savedMember.isCanEdit());
        assertTrue(savedMember.isCanView());

        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void createBoard_shouldThrowWhenCreatorIsNotAMemberOfTheOrganization() {

        when(orgMemberClient.isMemberInOrganization(dto.getOrgId(), authenticatedMemberId))
                .thenReturn(false);

        OrgMemberNotFoundException exception = assertThrows(OrgMemberNotFoundException.class,
                () -> boardService.createBoard(dto));

        assertEquals("Member " + authenticatedMemberId + " is not a member of organization " + dto.getOrgId(),
                exception.getMessage());

        verify(orgMemberClient).isMemberInOrganization(dto.getOrgId(), authenticatedMemberId);
        verify(organizationRepository, never()).findById(any());
        verify(modelMapper, never()).map(any(BoardDTO.class), eq(Board.class));
        verify(boardRepository, never()).save(any(Board.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
    }

    @Test
    void createBoard_shouldThrowWhenOrganizationNotFound() {

        when(orgMemberClient.isMemberInOrganization(dto.getOrgId(), authenticatedMemberId))
                .thenReturn(true);

        when(organizationRepository.findById(dto.getOrgId()))
                .thenReturn(Optional.empty());

        OrganizationNotFoundException exception = assertThrows(OrganizationNotFoundException.class,
                () -> boardService.createBoard(dto));

        assertEquals("Organization with id " + dto.getOrgId() + " not found", exception.getMessage());

        verify(orgMemberClient).isMemberInOrganization(dto.getOrgId(), authenticatedMemberId);
        verify(organizationRepository).findById(dto.getOrgId());
        verify(modelMapper, never()).map(any(BoardDTO.class), eq(Board.class));
        verify(boardRepository, never()).save(any(Board.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
    }

    @Test
    void createBoard_shouldThrowWhenOrganizationServiceIsUnavailable() {

        when(orgMemberClient.isMemberInOrganization(dto.getOrgId(), authenticatedMemberId))
                .thenThrow(new RestClientException("connection refused"));

        assertThrows(OrgServiceUnavailableException.class, () -> boardService.createBoard(dto));

        verify(orgMemberClient).isMemberInOrganization(dto.getOrgId(), authenticatedMemberId);
        verify(organizationRepository, never()).findById(any());
        verify(modelMapper, never()).map(any(BoardDTO.class), eq(Board.class));
        verify(boardRepository, never()).save(any(Board.class));
        verify(boardMemberRepository, never()).save(any(BoardMember.class));
    }

    @Test
    void createBoard_shouldRejectWhenNoAuthenticatedMemberExists() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> boardService.createBoard(dto));

        verifyNoInteractions(orgMemberClient, organizationRepository, boardRepository, boardMemberRepository, modelMapper);
    }

    @Test
    void createBoard_shouldRejectWhenAuthenticatedPrincipalIsNotAMemberUuid() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("not-a-member-id", null, List.of()));

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> boardService.createBoard(dto));

        verifyNoInteractions(orgMemberClient, organizationRepository, boardRepository, boardMemberRepository, modelMapper);
    }

    @Test
    void shouldDeleteBoard() {

        when(boardRepository.deleteBoardById(existingBoardId))
                .thenReturn(1);

        boardService.deleteBoard(existingBoardId);

        verify(boardRepository).deleteBoardById(existingBoardId);
        verify(boardRepository, never()).existsById(anyLong());
        verify(boardRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteBoard_shouldThrowWhenBoardDoesNotExist() {

        when(boardRepository.deleteBoardById(missingBoardId))
                .thenReturn(0);

        BoardNotFoundException exception = assertThrows(BoardNotFoundException.class,
                () -> boardService.deleteBoard(missingBoardId));

        assertEquals("Board not found with id: " + missingBoardId, exception.getMessage());

        verify(boardRepository).deleteBoardById(missingBoardId);
        verify(boardRepository, never()).existsById(anyLong());
        verify(boardRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldGetBoardsByOrganizationSuccessfully() {

        Long orgId = BoardTestDataFactory.VALID_ORG_ID;
        List<Board> boards = List.of(board);

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);

        when(boardRepository.findByOrgId(orgId))
                .thenReturn(boards);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dtoWithId);

        List<BoardDTO> result = boardService.getBoardsByOrganization(orgId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dtoWithId.getBoardId(), result.get(0).getBoardId());
        assertEquals(dto.getBoardName(), result.get(0).getBoardName());
        assertEquals(dto.getBoardDesc(), result.get(0).getBoardDesc());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository).findByOrgId(orgId);
        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldThrowOrganizationNotFoundExceptionWhenOrgIdDoesNotExist() {

        Long orgId = BoardTestDataFactory.INVALID_ORG_ID;

        when(organizationRepository.existsById(orgId))
                .thenReturn(false);

        OrganizationNotFoundException exception = assertThrows(
                OrganizationNotFoundException.class,
                () -> boardService.getBoardsByOrganization(orgId)
        );

        assertEquals("Organization with id " + orgId + " not found", exception.getMessage());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository, never()).findByOrgId(anyLong());
        verify(modelMapper, never()).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldGetBoardByIdSuccessfully() {

        when(boardRepository.findById(existingBoardId))
                .thenReturn(Optional.of(board));
        when(modelMapper.map(board, BoardDTO.class))
                .thenReturn(dtoWithId);

        BoardDTO result = boardService.getBoardById(existingBoardId);

        assertNotNull(result);
        assertEquals(dtoWithId.getBoardId(), result.getBoardId());
        assertEquals(dto.getBoardName(), result.getBoardName());
        verify(boardRepository).findById(existingBoardId);
        verify(modelMapper).map(board, BoardDTO.class);
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenBoardIdDoesNotExist() {

        when(boardRepository.findById(missingBoardId))
                .thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class, () -> {
            boardService.getBoardById(missingBoardId);
        });

        verify(boardRepository).findById(missingBoardId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void shouldPatchBoardByIdSuccessfully() {
        BoardUpdateDTO updateDTO = BoardTestDataFactory.createBoardUpdateDTO();
        BoardDTO patchedDTO = BoardTestDataFactory.createBoardDTOWithId();
        patchedDTO.setBoardName(updateDTO.getBoardName());
        patchedDTO.setBoardDesc(updateDTO.getBoardDesc());

        when(boardRepository.findById(existingBoardId))
                .thenReturn(Optional.of(board));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(board);
        doNothing().when(modelMapper).map(any(BoardUpdateDTO.class), any(Board.class));
        when(modelMapper.map(board, BoardDTO.class))
                .thenReturn(patchedDTO);

        BoardDTO result = boardService.patchBoardById(existingBoardId, updateDTO);

        assertNotNull(result);
        assertEquals(updateDTO.getBoardName(), result.getBoardName());
        assertEquals(updateDTO.getBoardDesc(), result.getBoardDesc());
        verify(boardRepository).findById(existingBoardId);
        verify(modelMapper).map(updateDTO, board);
        verify(boardRepository).save(board);
        verify(modelMapper).map(board, BoardDTO.class);
    }

    @Test
    void shouldThrowBoardNotFoundExceptionWhenPatchingNonExistentBoard() {
        BoardUpdateDTO updateDTO = BoardTestDataFactory.createBoardUpdateDTO();

        when(boardRepository.findById(missingBoardId))
                .thenReturn(Optional.empty());

        assertThrows(BoardNotFoundException.class, () -> {
            boardService.patchBoardById(missingBoardId, updateDTO);
        });

        verify(boardRepository).findById(missingBoardId);
        verify(boardRepository, never()).save(any());
    }
}
