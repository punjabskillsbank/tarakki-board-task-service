package com.tarakki.boardtask.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardMemberRequestDTO {

    @NotNull(message = "Email must not be empty")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotNull(message = "CanEdit must not be empty")
    private Boolean canEdit;

    @NotNull(message = "CanView must not be empty")
    private Boolean canView;

}
