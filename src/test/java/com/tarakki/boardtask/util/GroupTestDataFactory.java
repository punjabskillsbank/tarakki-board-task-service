package com.tarakki.boardtask.util;

import com.tarakki.boardtask.dto.GroupDTO;
import com.tarakki.boardtask.entity.Group;

import java.time.LocalDateTime;

import java.util.UUID;

public class GroupTestDataFactory {
    public static final Long GROUP_ID = 1L;
    public static final String GROUP_NAME = "Demo group";
    public static final int POSITION = 1;
    public static final UUID CREATED_BY = UUID.randomUUID();
    public static final Long BOARD_ID = 1L;
    public static final Long INVALID_BOARD_ID = 999L;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT = LocalDateTime.now();


    public static GroupDTO createGroupDto() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setGroupId(GROUP_ID);
        groupDTO.setBoardId(BOARD_ID);
        groupDTO.setGroupName(GROUP_NAME);
        groupDTO.setPosition(POSITION);
        groupDTO.setCreatedBy(CREATED_BY);
        groupDTO.setCreatedAt(CREATED_AT);
        groupDTO.setUpdatedAt(UPDATED_AT);
        return groupDTO;
    }

    public static Group createGroupEntity() {
        Group group = new Group();
        group.setBoardId(BOARD_ID);
        group.setGroupName(GROUP_NAME);
        group.setPosition(POSITION);
        group.setCreatedBy(CREATED_BY);
        return group;
    }
}
