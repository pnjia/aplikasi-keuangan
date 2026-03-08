package com.aplikasi.keuangan.dto;

import com.aplikasi.keuangan.entity.AccountType;
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
public class AccountResponseDTO {
    private UUID id;
    private UUID companyId;
    private UUID parentAccountId;
    private String accountCode;
    private String accountName;
    private AccountType accountType;
}
