package com.example.inventory.dto;

import java.time.LocalDate;

public record DailySalesResponse(LocalDate date, Long totalUnitsSold) {
}
