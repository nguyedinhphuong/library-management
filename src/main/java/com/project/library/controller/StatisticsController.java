package com.project.library.controller;


import com.project.library.dto.response.DashboardStatsResponse;
import com.project.library.dto.response.ResponseData;
import com.project.library.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/vi/stats")
@Tag(name = "Statistics Controller")
@Validated
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticService statisticService;

    @Operation(summary = "Get Dashboard Statistics",
            description = "Get overall library statistics for dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<ResponseData<DashboardStatsResponse>> getDashboardStats() {
        try{
            log.debug("API get dashboard stats called");
            DashboardStatsResponse response = statisticService.getDashboardStats();
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Get dashboard statistics successfully ", response));
        } catch (Exception e) {
            log.error("Unexpected error when getting dashboard stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal server error"));        }
    }
}
