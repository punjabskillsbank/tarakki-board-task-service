package com.tarakki.boardtask.dto;

import com.tarakki.boardtask.enums.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardMemberDTO {

    private Long boardMemberId;

    private Long boardId;

    private UUID memberId;

    private BoardRole role;

    private boolean canEdit;

    private boolean canView;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
