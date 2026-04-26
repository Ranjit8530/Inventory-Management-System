package com.example.inventory.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(String name, BigDecimal price, Integer threshold) {
}
