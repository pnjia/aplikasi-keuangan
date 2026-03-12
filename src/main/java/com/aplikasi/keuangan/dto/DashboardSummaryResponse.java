package com.aplikasi.keuangan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalCashBalance;
    private BigDecimal currentMonthRevenue;
    private BigDecimal currentMonthExpense;
    private BigDecimal netProfit;
    private List<MonthlyChartData> monthlyRevenueExpense;
    private List<CategoryChartData> expenseByCategory;
}
