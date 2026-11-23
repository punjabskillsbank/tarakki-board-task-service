package com.tarakki.boardtask.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateDTO {

    private String title;

    @Size(max = 500)
    private String description;

    private Integer position;

    private Long groupId;

}