package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.GroupDTO;

import java.util.List;

public interface GroupService {
    GroupDTO createGroupByBoardId(GroupDTO groupDTO, Long boardId);

    List<GroupDTO> getGroupsByBoardId(Long boardId);
}
