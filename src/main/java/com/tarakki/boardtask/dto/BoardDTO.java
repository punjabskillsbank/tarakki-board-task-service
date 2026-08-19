package com.tarakki.boardtask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

    private Long boardId;

    @NotNull
    private Long orgId;

    @NotBlank
    private String boardName;

    @NotBlank
    private String boardDesc;

    @NotNull
    private UUID createdBy;
}