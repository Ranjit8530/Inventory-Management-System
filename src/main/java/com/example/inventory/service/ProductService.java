package com.example.inventory.service;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.UpdateProductRequest;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        Product savedProduct = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantity(0);
        inventory.setThreshold(request.threshold() == null ? 10 : request.threshold());
        inventoryRepository.save(inventory);

        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.threshold() != null) {
            Inventory inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Inventory not found for product: " + id));
            inventory.setThreshold(request.threshold());
            inventoryRepository.save(inventory);
        }

        return productRepository.save(product);
    }
}
