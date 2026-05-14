package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.entity.Board;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<BoardDTO> getBoardsByOrganization(Long orgId) {

        List<Board> boards = boardRepository.findByOrgId(orgId);
        return boards.stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
    }
}