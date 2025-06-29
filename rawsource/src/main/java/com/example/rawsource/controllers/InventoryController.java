package com.example.rawsource.controllers;

import com.example.rawsource.entities.dto.inventory.AddInventoryDto;
import com.example.rawsource.entities.dto.inventory.DeliverOrderDto;
import com.example.rawsource.entities.dto.inventory.InventoryDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryDto;
import com.example.rawsource.services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<InventoryDto> createInventoryForUser(
            @PathVariable UUID userId,
            @RequestBody @Valid AddInventoryDto addInventoryDto) {
        InventoryDto createdInventory = inventoryService.createInventoryForUser(userId, addInventoryDto);
        return new ResponseEntity<>(createdInventory, HttpStatus.CREATED);
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BUYER', 'PROVIDER')")
    public ResponseEntity<InventoryDto> getCurrentUserInventory() {
        InventoryDto inventory = inventoryService.getCurrentUserInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryServiceImpl.isCurrentUserInventory(#id)")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable UUID id) {
        InventoryDto inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userServiceImpl.isCurrentUser(#userId)")
    public ResponseEntity<InventoryDto> getInventoryByUserId(@PathVariable UUID userId) {
        InventoryDto inventory = inventoryService.getInventoryByUserId(userId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/users/{userId}/exists")
    @PreAuthorize("hasAuthority('ADMIN') or @userServiceImpl.isCurrentUser(#userId)")
    public ResponseEntity<Boolean> checkInventoryExists(@PathVariable UUID userId) {
        inventoryService.getInventoryByUserId(userId);
        return ResponseEntity.ok(true);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<InventoryDto>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @PostMapping("/deliver-order")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> deliverOrder(@RequestBody DeliverOrderDto deliverOrderDto) {
        inventoryService.deliverOrder(deliverOrderDto);
        return ResponseEntity.ok("Order delivered successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryServiceImpl.isCurrentUserInventory(#id)")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateInventoryDto updateInventoryDto) {
        InventoryDto updatedInventory = inventoryService.updateInventory(id, updateInventoryDto);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @inventoryServiceImpl.isCurrentUserInventory(#id)")
    public ResponseEntity<Void> deleteInventory(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
} 