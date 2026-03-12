package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyChartData {
    private String month;
    private BigDecimal revenue;
    private BigDecimal expense;
}
