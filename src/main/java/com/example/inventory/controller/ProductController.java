package com.example.inventory.controller;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.UpdateProductRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }
}
