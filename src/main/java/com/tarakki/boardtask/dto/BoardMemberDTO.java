package com.tarakki.boardtask.dto;

import com.tarakki.boardtask.enums.BoardRole;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long boardId;

    @NotNull
    private UUID memberId;

    @NotNull
    private BoardRole role;

    @NotNull
    private Boolean canEdit;

    @NotNull
    private Boolean canView;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
