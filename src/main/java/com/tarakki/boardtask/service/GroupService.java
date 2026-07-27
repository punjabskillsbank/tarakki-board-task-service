package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.GroupDTO;

public interface GroupService {
    GroupDTO createGroupByBoardId(GroupDTO groupDTO, Long boardId);
}
