package edu.cda.project.ticklybackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {

    private String token;
    private Integer userId;
    private String role;
    private boolean needsSetup;

    public AuthResponseDto(String token, Integer userId, String role, boolean needsSetup) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.needsSetup = needsSetup;
    }
}
