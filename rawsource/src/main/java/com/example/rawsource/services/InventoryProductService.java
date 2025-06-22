package com.example.rawsource.services;

import com.example.rawsource.entities.dto.inventory.AddInventoryProductDto;
import com.example.rawsource.entities.dto.inventory.InventoryProductDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryProductDto;

import java.util.List;
import java.util.UUID;

public interface InventoryProductService {
    
    InventoryProductDto addProductToInventory(UUID inventoryId, AddInventoryProductDto addInventoryProductDto);
    
    InventoryProductDto updateInventoryProduct(UUID inventoryId, UUID productId, UpdateInventoryProductDto updateInventoryProductDto);
    
    void removeProductFromInventory(UUID inventoryId, UUID productId);
    
    List<InventoryProductDto> getInventoryProducts(UUID inventoryId);
    
    InventoryProductDto getInventoryProduct(UUID inventoryId, UUID productId);
    
    List<InventoryProductDto> getLowStockProducts(UUID inventoryId);
    
    List<InventoryProductDto> getProductsByStatus(UUID inventoryId, String status);
    
    boolean isCurrentUserInventory(UUID inventoryId);
    
    InventoryProductDto convertToDto(com.example.rawsource.entities.InventoryProduct inventoryProduct);
} 