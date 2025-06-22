package com.example.rawsource.services.impl;

import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.User;
import com.example.rawsource.entities.dto.inventory.AddInventoryDto;
import com.example.rawsource.entities.dto.inventory.InventoryDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryDto;
import com.example.rawsource.repositories.InventoryRepository;
import com.example.rawsource.repositories.UserRepository;
import com.example.rawsource.services.InventoryService;
import com.example.rawsource.utils.SecureLogger;
import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public InventoryDto createInventoryForUser(UUID userId, AddInventoryDto addInventoryDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (inventoryRepository.existsByUser(user)) {
            throw new ForbiddenException("User already has an inventory");
        }
        
        Inventory inventory = new Inventory();
        inventory.setName(addInventoryDto.getName());
        inventory.setDescription(addInventoryDto.getDescription());
        inventory.setUser(user);
        inventory.setDate(LocalDate.now());
        inventory.setIsActive(true);
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        SecureLogger.info("Created inventory for user - User ID: {}", user.getId());
        
        return convertToDto(savedInventory);
    }
    
    @Override
    public InventoryDto getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        return convertToDto(inventory);
    }
    
    @Override
    public InventoryDto getInventoryByUserId(UUID userId) {
        Inventory inventory = inventoryRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "userId", userId));
        return convertToDto(inventory);
    }
    
    @Override
    public InventoryDto getCurrentUserInventory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));
        
        Inventory inventory = inventoryRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "user", user));
        
        return convertToDto(inventory);
    }
    
    @Override
    public List<InventoryDto> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public InventoryDto updateInventory(UUID id, UpdateInventoryDto updateInventoryDto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        

        if (!isCurrentUserInventory(id)) {
            throw new ForbiddenException("You are not allowed to update this inventory");
        }
        
        inventory.setName(updateInventoryDto.getName());
        inventory.setDescription(updateInventoryDto.getDescription());
        if (updateInventoryDto.getIsActive() != null) {
            inventory.setIsActive(updateInventoryDto.getIsActive());
        }
        
        Inventory updatedInventory = inventoryRepository.save(inventory);
        SecureLogger.info("Updated inventory - Inventory ID: {}", id);
        
        return convertToDto(updatedInventory);
    }
    
    @Override
    public void deleteInventory(UUID id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory", "id", id);
        }

        if (!isCurrentUserInventory(id)) {
            throw new ForbiddenException("You are not allowed to delete this inventory");
        }
        
        inventoryRepository.deleteById(id);
        SecureLogger.info("Deleted inventory - Inventory ID: {}", id);
    }
    
    @Override
    public boolean isCurrentUserInventory(UUID inventoryId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        return inventory.getUser().getId().equals(user.getId()) || 
               user.getRole().name().equals("ADMIN");
    }
    
    @Override
    public InventoryDto convertToDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setName(inventory.getName());
        dto.setDescription(inventory.getDescription());
        dto.setIsActive(inventory.getIsActive());
        dto.setUserId(inventory.getUser().getId());
        dto.setUserName(inventory.getUser().getName());
        return dto;
    }
} 