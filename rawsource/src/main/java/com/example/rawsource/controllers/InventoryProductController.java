package com.example.rawsource.controllers;

import com.example.rawsource.entities.dto.inventory.AddInventoryProductDto;
import com.example.rawsource.entities.dto.inventory.InventoryProductDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryProductDto;
import com.example.rawsource.services.InventoryProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories/{inventoryId}/products")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryProductController {

    @Autowired
    private InventoryProductService inventoryProductService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<InventoryProductDto> addProductToInventory(
            @PathVariable UUID inventoryId,
            @RequestBody @Valid AddInventoryProductDto addInventoryProductDto) {
        
        InventoryProductDto addedProduct = inventoryProductService.addProductToInventory(inventoryId, addInventoryProductDto);
        return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<List<InventoryProductDto>> getInventoryProducts(@PathVariable UUID inventoryId) {
        
        List<InventoryProductDto> products = inventoryProductService.getInventoryProducts(inventoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<InventoryProductDto> getInventoryProduct(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId) {
        
        InventoryProductDto product = inventoryProductService.getInventoryProduct(inventoryId, productId);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<InventoryProductDto> updateInventoryProduct(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateInventoryProductDto updateInventoryProductDto) {
        
        InventoryProductDto updatedProduct = inventoryProductService.updateInventoryProduct(inventoryId, productId, updateInventoryProductDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<Void> removeProductFromInventory(
            @PathVariable UUID inventoryId,
            @PathVariable UUID productId) {
        
        inventoryProductService.removeProductFromInventory(inventoryId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<List<InventoryProductDto>> getLowStockProducts(@PathVariable UUID inventoryId) {
        
        List<InventoryProductDto> lowStockProducts = inventoryProductService.getLowStockProducts(inventoryId);
        return ResponseEntity.ok(lowStockProducts);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryProductServiceImpl.isCurrentUserInventory(#inventoryId)")
    public ResponseEntity<List<InventoryProductDto>> getProductsByStatus(
            @PathVariable UUID inventoryId,
            @PathVariable String status) {
        
        List<InventoryProductDto> products = inventoryProductService.getProductsByStatus(inventoryId, status);
        return ResponseEntity.ok(products);
    }
} 