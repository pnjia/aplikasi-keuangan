package com.aplikasi.keuangan.service;

import com.aplikasi.keuangan.dto.CategoryChartData;
import com.aplikasi.keuangan.dto.DashboardSummaryResponse;
import com.aplikasi.keuangan.dto.MonthlyChartData;
import com.aplikasi.keuangan.repository.JournalLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JournalLineRepository journalLineRepository;

    public DashboardSummaryResponse getDashboardSummary(UUID companyId) {
        // Current month bounds
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
        
        Instant startOfMonthInstant = startOfMonth.toInstant();
        Instant endOfMonthInstant = endOfMonth.toInstant();

        // 6 months ago bounds for trend
        ZonedDateTime startOfSixMonthsAgo = now.minusMonths(5).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        Instant startOfTrendInstant = startOfSixMonthsAgo.toInstant();

        BigDecimal totalCashBalance = journalLineRepository.getCashBalance(companyId);
        if (totalCashBalance == null) {
            totalCashBalance = BigDecimal.ZERO;
        }

        BigDecimal currentMonthRevenue = journalLineRepository.getCurrentMonthRevenue(companyId, startOfMonthInstant, endOfMonthInstant);
        if (currentMonthRevenue == null) {
            currentMonthRevenue = BigDecimal.ZERO;
        }

        BigDecimal currentMonthExpense = journalLineRepository.getCurrentMonthExpense(companyId, startOfMonthInstant, endOfMonthInstant);
        if (currentMonthExpense == null) {
            currentMonthExpense = BigDecimal.ZERO;
        }

        BigDecimal netProfit = currentMonthRevenue.subtract(currentMonthExpense);

        List<Object[]> expenseCategoryRaw = journalLineRepository.getExpenseByCategory(companyId, startOfMonthInstant, endOfMonthInstant);
        List<CategoryChartData> expenseByCategory = new ArrayList<>();
        for (Object[] row : expenseCategoryRaw) {
            String categoryName = (String) row[0];
            BigDecimal value = (BigDecimal) row[1];
            expenseByCategory.add(new CategoryChartData(categoryName, value != null ? value : BigDecimal.ZERO));
        }

        List<Object[]> monthlyTrendRaw = journalLineRepository.getMonthlyTrend(companyId, startOfTrendInstant, endOfMonthInstant);
        List<MonthlyChartData> monthlyTrend = new ArrayList<>();
        for (Object[] row : monthlyTrendRaw) {
            String month = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            BigDecimal expense = (BigDecimal) row[2];
            monthlyTrend.add(new MonthlyChartData(
                    month, 
                    revenue != null ? revenue : BigDecimal.ZERO, 
                    expense != null ? expense : BigDecimal.ZERO
            ));
        }

        return DashboardSummaryResponse.builder()
                .totalCashBalance(totalCashBalance)
                .currentMonthRevenue(currentMonthRevenue)
                .currentMonthExpense(currentMonthExpense)
                .netProfit(netProfit)
                .monthlyRevenueExpense(monthlyTrend)
                .expenseByCategory(expenseByCategory)
                .build();
    }
}
