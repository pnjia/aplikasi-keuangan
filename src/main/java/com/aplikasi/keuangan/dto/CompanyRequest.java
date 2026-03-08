package com.aplikasi.keuangan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyRequest {
    private String name;
    private String address;
    private String taxNumber;
}
