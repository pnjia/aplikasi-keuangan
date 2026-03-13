package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    private UUID userId;
    private String fullName;
    private String email;
    private String roleName;
    private Boolean isActive;
}
