package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.entity.Board;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.serviceImpl.BoardServiceImpl;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

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
    }
}
