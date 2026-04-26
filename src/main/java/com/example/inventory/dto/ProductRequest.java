package com.example.inventory.dto;

import java.math.BigDecimal;

public record ProductRequest(String name, BigDecimal price, Integer threshold) {
}
