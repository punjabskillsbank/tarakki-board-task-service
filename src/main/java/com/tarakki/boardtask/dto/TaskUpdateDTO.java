package com.tarakki.boardtask.dto;

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

    private Integer position;

    private Long groupId;

}