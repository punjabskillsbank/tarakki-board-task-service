package com.tarakki.boardtask.dto;

import com.tarakki.common.enums.OrgMemberStatus;
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

    private OrgMemberStatus memberAccountStatus;

}
