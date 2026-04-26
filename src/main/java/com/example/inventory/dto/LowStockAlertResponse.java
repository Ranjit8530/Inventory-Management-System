package com.example.inventory.dto;

public record LowStockAlertResponse(Long productId, String productName, Integer quantity, Integer threshold) {
}
