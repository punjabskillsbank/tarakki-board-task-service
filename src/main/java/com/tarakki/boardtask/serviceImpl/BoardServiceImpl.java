package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.common.exceptionHandling.OrganisationNotFoundException;
import com.tarakki.common.entity.Board;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.service.BoardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;

    @Override
    public BoardDTO createBoard(BoardDTO boardDTO) {

        Board board = modelMapper.map(boardDTO, Board.class);
        Board savedBoard = boardRepository.save(board);

        return modelMapper.map(savedBoard, BoardDTO.class);
    }

    @Override
    @Transactional
    public int deleteBoard(Long boardId) {
        return boardRepository.deleteBoardById(boardId);
    }
}
    public List<BoardDTO> getBoardsByOrganization(Long orgId) {

        // Validate organization exists
        if (!organizationRepository.existsById(orgId)) {
            throw new OrganisationNotFoundException(orgId);
        }

        List<Board> boards = boardRepository.findByOrgId(orgId);
        return boards.stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
    }
}
