package com.tarakki.boardtask.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GroupDTO {

    private Long groupId;

    @NotNull
    private Long boardId;

    @NotBlank
    private String groupName;

    @NotNull
    private Integer position;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
