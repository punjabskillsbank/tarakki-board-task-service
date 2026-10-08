package com.tarakki.boardtask.serviceImpl;

import com.tarakki.boardtask.client.OrgMemberClient;
import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.entity.Board;
import com.tarakki.boardtask.entity.Group;
import com.tarakki.boardtask.exception.BoardNotFoundException;
import com.tarakki.boardtask.exception.OrgMemberNotFoundException;
import com.tarakki.boardtask.exception.OrgServiceUnavailableException;
import com.tarakki.boardtask.exception.PositionAlreadyExistsException;
import com.tarakki.boardtask.repository.BoardRepository;
import com.tarakki.boardtask.repository.GroupRepository;
import com.tarakki.boardtask.service.GroupService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final BoardRepository boardRepository;
    private final OrgMemberClient orgMemberClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public GroupDTO createGroupByBoardId(GroupDTO groupDTO, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        if (!isMemberInOrganization(board.getOrgId(), groupDTO.getCreatedBy())) {
            throw new OrgMemberNotFoundException(groupDTO.getCreatedBy(), board.getOrgId());
        }

        if (isPositionOccupied(boardId, groupDTO.getPosition())) {
            throw new PositionAlreadyExistsException(groupDTO.getPosition(), boardId);
        }

        groupDTO.setBoardId(boardId);

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

    private boolean isMemberInOrganization(Long orgId, UUID memberId) {
        try {
            return orgMemberClient.isMemberInOrganization(orgId, memberId);
        } catch (RestClientException exception) {
            throw new OrgServiceUnavailableException(orgId);
        }
    }

    private boolean isPositionOccupied(Long boardId, Integer position) {
        return groupRepository.existsByBoardIdAndPosition(boardId, position);
    }
}
