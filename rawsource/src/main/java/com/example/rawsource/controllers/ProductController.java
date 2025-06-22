package com.example.rawsource.controllers;

import com.example.rawsource.entities.dto.product.AddProductDto;
import com.example.rawsource.entities.dto.product.ProductDto;
import com.example.rawsource.entities.dto.product.UpdateProductDto;
import com.example.rawsource.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    private final ProductService productService;

    public ProductController(@Autowired ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<ProductDto> addProduct(@RequestBody @Valid AddProductDto product) {
        try {
            ProductDto createdProduct = productService.createProduct(product);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String id,
            @RequestBody @Valid UpdateProductDto product) {
        try {
            ProductDto updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<List<ProductDto>> getProductsByProvider(@PathVariable UUID providerId) {
        return ResponseEntity.ok(productService.getProductsByProvider(providerId));
    }
}
