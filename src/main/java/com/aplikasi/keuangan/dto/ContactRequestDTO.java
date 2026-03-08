package com.aplikasi.keuangan.dto;

import com.aplikasi.keuangan.entity.ContactType;
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
public class ContactRequestDTO {
    private UUID companyId;
    private ContactType type;
    private String name;
    private String phone;
    private String email;
}
