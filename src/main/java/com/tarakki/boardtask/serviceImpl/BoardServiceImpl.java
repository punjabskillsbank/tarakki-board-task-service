package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.dto.TaskDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.common.exceptionHandling.OrganizationNotFoundException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.OrganizationRepository;
import com.tarakki.boardtask.repository.TaskRepository;
import com.tarakki.boardtask.entity.Task;
import com.tarakki.boardtask.service.BoardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final OrganizationRepository organizationRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Override
    public BoardDTO createBoard(BoardDTO boardDTO) {

        Board board = modelMapper.map(boardDTO, Board.class);
        Board savedBoard = boardRepository.save(board);

        return modelMapper.map(savedBoard, BoardDTO.class);
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }

    @Override
    public List<BoardDTO> getBoardsByOrganization(Long orgId) {

        if (!organizationRepository.existsById(orgId)) {
            throw new OrganizationNotFoundException(orgId);
        }

        List<Board> boards = boardRepository.findByOrgId(orgId);
        return boards.stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .toList();
    }

    @Override
    public BoardDTO getBoardById(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
        return modelMapper.map(board, BoardDTO.class);
    }

    @Override
    @Transactional
    public TaskDTO patchTask(Long boardId, Long taskId, TaskDTO taskDTO) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        Task task = taskRepository.findByTaskIdAndBoardId(taskId, boardId)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + taskId + " for board id " + boardId));

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        task.setPosition(taskDTO.getPosition());

        if (taskDTO.getGroupId() != null) {
            task.setGroupId(taskDTO.getGroupId());
        }

        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskDTO.class);
    }


}
