package edu.cda.project.ticklybackend.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String avatarUrl;
}