package com.project.library.utils;

import java.time.LocalDateTime;

public class StudentCodeGenerator {

    private StudentCodeGenerator() {}

    /**
     * Generate student code format: SV{YEAR}{MAJOR_CODE}{SEQUENCE}
     * Example: SV2024IT001, SV2024CS002
     *
     * @param major major the student's major
     * @param sequence sequence the sequence number (auto-increment from DB)
     * @return generated student code
     */
    public static String generate(Major major, Long sequence){
        int year = LocalDateTime.now().getYear(); // 2026
        String majorCode = getMajorCode(major);
        String sequenceStr = String.format("%03d", sequence);
        return String.format("SV%d%s%s", year, majorCode, sequenceStr);
    }

    private static String getMajorCode(Major major) {
        return switch (major) {
            case INFORMATION_TECHNOLOGY -> "IT";
            case COMPUTER_SCIENCE -> "CS";
            case SOFTWARE_ENGINEERING -> "SE";
            case INFORMATION_SYSTEMS -> "IS";
            case DATA_SCIENCE -> "DS";
            case CYBER_SECURITY -> "CY";
            case ARTIFICIAL_INTELLIGENCE -> "AI";
            case BUSINESS_ADMINISTRATION -> "BA";
            case ACCOUNTING -> "AC";
            case FINANCE -> "FI";
            case MARKETING -> "MK";
            case ECONOMICS -> "EC";
        };
    }

}
