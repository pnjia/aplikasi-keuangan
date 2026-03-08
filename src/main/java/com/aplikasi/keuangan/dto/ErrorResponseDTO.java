package com.aplikasi.keuangan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class ErrorResponseDTO {
    private ZonedDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
}
