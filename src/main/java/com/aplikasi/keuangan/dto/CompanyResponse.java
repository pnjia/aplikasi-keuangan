package com.aplikasi.keuangan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CompanyResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String address;
    private String taxNumber;
}
