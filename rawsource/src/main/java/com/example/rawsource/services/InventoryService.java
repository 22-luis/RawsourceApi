package com.example.rawsource.services;

import com.example.rawsource.entities.dto.inventory.AddInventoryDto;
import com.example.rawsource.entities.dto.inventory.DeliverOrderDto;
import com.example.rawsource.entities.dto.inventory.InventoryDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryDto;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    
    InventoryDto createInventoryForUser(UUID userId, AddInventoryDto addInventoryDto);
    
    InventoryDto getInventoryById(UUID id);
    
    InventoryDto getInventoryByUserId(UUID userId);
    
    InventoryDto getCurrentUserInventory();
    
    List<InventoryDto> getAllInventories();
    
    InventoryDto updateInventory(UUID id, UpdateInventoryDto updateInventoryDto);
    
    void deleteInventory(UUID id);
    
    boolean isCurrentUserInventory(UUID inventoryId);
    
    InventoryDto convertToDto(com.example.rawsource.entities.Inventory inventory);
    
    void deliverOrder(DeliverOrderDto deliverOrderDto);
} 