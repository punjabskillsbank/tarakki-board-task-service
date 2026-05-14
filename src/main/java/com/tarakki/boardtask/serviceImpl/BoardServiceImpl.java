package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.entity.Board;
import com.tarakki.common.exceptionHandling.BoardNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    @Override
    public BoardDTO createBoard(BoardDTO boardDTO) {

        Board board = modelMapper.map(boardDTO, Board.class);
        Board savedBoard = boardRepository.save(board);

        return modelMapper.map(savedBoard, BoardDTO.class);
    }

    @Override
    public void deleteBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new BoardNotFoundException(boardId);
        }

        boardRepository.deleteById(boardId);
    }
}
