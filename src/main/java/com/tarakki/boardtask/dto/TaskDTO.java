package com.tarakki.boardtask.dto;

import jakarta.validation.constraints.NotBlank;
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
public class TaskDTO {

    private Long taskId;

    @NotNull
    private Long boardId;

    @NotNull
    private Long groupId;

    @NotBlank
    private String title;

    @NotNull
    private int position;

    @NotNull
    private UUID createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
