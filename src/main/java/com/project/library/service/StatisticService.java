package com.project.library.service;

import com.project.library.dto.response.DashboardStatsResponse;
import com.project.library.dto.response.MonthlyReportResponse;

public interface StatisticService {

    DashboardStatsResponse getDashboardStats();
    MonthlyReportResponse getMonthlyReport(String month);
}
