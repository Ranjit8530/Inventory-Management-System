package com.example.inventory.controller;

import com.example.inventory.dto.DailySalesResponse;
import com.example.inventory.dto.LowStockAlertResponse;
import com.example.inventory.dto.StockRequest;
import com.example.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{productId}/add")
    public ResponseEntity<Map<String, String>> addStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        inventoryService.addStock(productId, request.quantity());
        return ResponseEntity.ok(Map.of("message", "Stock added successfully"));
    }

    @PostMapping("/{productId}/sell")
    public ResponseEntity<Map<String, String>> sellProduct(@PathVariable Long productId, @RequestBody StockRequest request) {
        inventoryService.sellProduct(productId, request.quantity());
        return ResponseEntity.ok(Map.of("message", "Product sold successfully"));
    }

    // Client polls this endpoint on login/refresh to show low-stock warnings.
    @GetMapping("/alerts")
    public List<LowStockAlertResponse> getLowStockAlerts() {
        return inventoryService.getLowStockAlerts();
    }

    @GetMapping("/sales/daily")
    public List<DailySalesResponse> getDailySales() {
        return inventoryService.getDailySales();
    }
}
