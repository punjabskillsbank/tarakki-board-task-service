package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.entity.Board;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.serviceImpl.BoardServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.common.exceptionHandling.OrganisationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private ModelMapper modelMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    private BoardDTO dto;
    private Board board;
    private Long existingBoardId;
    private Long missingBoardId;

    @BeforeEach
    void setUp() {
        dto = BoardTestDataFactory.createBoardDTO();
        board = BoardTestDataFactory.createBoardEntity();
        existingBoardId = BoardTestDataFactory.createExistingBoardId();
        missingBoardId = BoardTestDataFactory.createMissingBoardId();
    }

    @Test
    void shouldCreateBoard() {

        when(modelMapper.map(any(BoardDTO.class), eq(Board.class)))
                .thenReturn(board);

        when(boardRepository.save(any(Board.class)))
                .thenReturn(board);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dto);

        BoardDTO result = boardService.createBoard(dto);

        assertNotNull(result);
        assertEquals(dto.getBoardName(), result.getBoardName());
        assertEquals(dto.getBoardDesc(), result.getBoardDesc());
        assertEquals(dto.getOrgId(), result.getOrgId());
        assertEquals(dto.getCreatedBy(), result.getCreatedBy());

        verify(modelMapper).map(any(BoardDTO.class), eq(Board.class));
        verify(boardRepository).save(any(Board.class));
        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldDeleteBoard() {

        when(boardRepository.deleteBoardById(existingBoardId))
                .thenReturn(1);

        int deletedRows = boardService.deleteBoard(existingBoardId);

        assertEquals(1, deletedRows);
        verify(boardRepository).deleteBoardById(existingBoardId);
        verify(boardRepository, never()).existsById(anyLong());
        verify(boardRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnZeroWhenDeletingMissingBoard() {

        when(boardRepository.deleteBoardById(missingBoardId))
                .thenReturn(0);

        int deletedRows = boardService.deleteBoard(missingBoardId);

        assertEquals(0, deletedRows);
        verify(boardRepository).deleteBoardById(missingBoardId);
        verify(boardRepository, never()).existsById(anyLong());
        verify(boardRepository, never()).deleteById(anyLong());
    void shouldGetBoardsByOrganizationSuccessfully() {

        Long orgId = BoardTestDataFactory.VALID_ORG_ID;
        List<Board> boards = List.of(board);

        when(organizationRepository.existsById(orgId))
                .thenReturn(true);

        when(boardRepository.findByOrgId(orgId))
                .thenReturn(boards);

        when(modelMapper.map(any(Board.class), eq(BoardDTO.class)))
                .thenReturn(dto);

        List<BoardDTO> result = boardService.getBoardsByOrganization(orgId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto.getBoardName(), result.get(0).getBoardName());
        assertEquals(dto.getBoardDesc(), result.get(0).getBoardDesc());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository).findByOrgId(orgId);
        verify(modelMapper).map(any(Board.class), eq(BoardDTO.class));
    }

    @Test
    void shouldThrowOrganisationNotFoundExceptionWhenOrgIdDoesNotExist() {

        Long orgId = BoardTestDataFactory.INVALID_ORG_ID;

        when(organizationRepository.existsById(orgId))
                .thenReturn(false);

        OrganisationNotFoundException exception = assertThrows(
                OrganisationNotFoundException.class,
                () -> boardService.getBoardsByOrganization(orgId)
        );

        assertEquals("Organisation with id " + orgId + " not found", exception.getMessage());

        verify(organizationRepository).existsById(orgId);
        verify(boardRepository, never()).findByOrgId(anyLong());
        verify(modelMapper, never()).map(any(Board.class), eq(BoardDTO.class));
    }
}
