package com.tarakki.boardtask.dto;

import com.tarakki.boardtask.enums.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardMemberUpdateDTO {

    private BoardRole role;

    private Boolean canEdit;

    private Boolean canView;
}
