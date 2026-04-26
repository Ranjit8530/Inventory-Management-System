package com.example.inventory.service;

import com.example.inventory.dto.DailySalesResponse;
import com.example.inventory.dto.LowStockAlertResponse;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryTransaction;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.TransactionType;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.InventoryTransactionRepository;
import com.example.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            InventoryTransactionRepository transactionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void addStock(Long productId, Integer quantity) {
        validatePositiveQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);

        saveTransaction(product, quantity, TransactionType.ADD);
    }

    @Transactional
    public void sellProduct(Long productId, Integer quantity) {
        validatePositiveQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        // Lock the row so only one sale request can update this product stock at a time.
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + productId));

        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("Not enough stock. Available=" + inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);

        saveTransaction(product, quantity, TransactionType.SALE);
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getLowStockAlerts() {
        return inventoryRepository.findLowStockItems().stream()
                .map(i -> new LowStockAlertResponse(
                        i.getProductId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getThreshold()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailySalesResponse> getDailySales() {
        List<Object[]> rows = transactionRepository.getDailyTotals(TransactionType.SALE);
        return rows.stream()
                .map(row -> {
                    Date sqlDate = (Date) row[0];
                    Long totalUnits = ((Number) row[1]).longValue();
                    LocalDate date = sqlDate.toLocalDate();
                    return new DailySalesResponse(date, totalUnits);
                })
                .toList();
    }

    private void saveTransaction(Product product, Integer quantity, TransactionType type) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setQuantity(quantity);
        tx.setType(type);
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    private void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
    }
}
