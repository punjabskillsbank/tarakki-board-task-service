package com.tarakki.boardtask.service;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.dto.GroupUpdateDTO;

import java.util.List;

public interface GroupService {
    GroupDTO createGroupByBoardId(GroupDTO groupDTO, Long boardId);

    List<GroupDTO> getGroupsByBoardId(Long boardId);

    void deleteGroup(Long groupId ,Long boardId);

    GroupDTO patchGroupById(Long boardId, Long groupId, GroupUpdateDTO groupUpdateDTO);
}
