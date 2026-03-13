package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamMemberRequestDTO {
    private String roleName;      // OWNER, ADMIN, atau KASIR
    private Boolean isActive;     // true = aktif, false = nonaktif (soft-disable)
}
