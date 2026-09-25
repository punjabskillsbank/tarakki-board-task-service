package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.GroupRepository;
import com.tarakki.boardtask.service.GroupService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public GroupDTO createGroupByBoardId(GroupDTO groupDTO, Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        if (isPositionOccupied(boardId, groupDTO.getPosition())) {
            throw new PositionAlreadyExistsException(groupDTO.getPosition(), boardId);
        }

        Group group = modelMapper.map(groupDTO, Group.class);
        groupRepository.save(group);

        return modelMapper.map(group, GroupDTO.class);
    }

    @Override
    public List<GroupDTO> getGroupsByBoardId(Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        List<Group> groups = groupRepository.findByBoardId(boardId);

        return groups.stream()
                .map(group -> modelMapper.map(group, GroupDTO.class))
                .toList();
    }

    private boolean isPositionOccupied(Long boardId, Integer position) {
        return groupRepository.existsByBoardIdAndPosition(boardId, position);
    }
}
