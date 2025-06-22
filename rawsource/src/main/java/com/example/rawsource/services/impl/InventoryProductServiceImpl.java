package com.example.rawsource.services.impl;

import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.InventoryProduct;
import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.Status;
import com.example.rawsource.entities.User;
import com.example.rawsource.entities.dto.inventory.AddInventoryProductDto;
import com.example.rawsource.entities.dto.inventory.InventoryProductDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryProductDto;
import com.example.rawsource.exceptions.BadRequestException;
import com.example.rawsource.exceptions.ForbiddenException;
import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.repositories.InventoryProductRepository;
import com.example.rawsource.repositories.InventoryRepository;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.repositories.UserRepository;
import com.example.rawsource.services.InventoryProductService;
import com.example.rawsource.utils.SecureLogger;

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
public class InventoryProductServiceImpl implements InventoryProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryProductServiceImpl.class);
    
    @Autowired
    private InventoryProductRepository inventoryProductRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public InventoryProductDto addProductToInventory(UUID inventoryId, AddInventoryProductDto addInventoryProductDto) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to add products to this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        Product product = productRepository.findById(addInventoryProductDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", addInventoryProductDto.getProductId()));

        if (inventoryProductRepository.existsByInventoryAndProduct(inventory, product)) {
            throw new BadRequestException("Product already exists in this inventory");
        }
        
        InventoryProduct inventoryProduct = new InventoryProduct();
        inventoryProduct.setInventory(inventory);
        inventoryProduct.setProduct(product);
        inventoryProduct.setQuantity(addInventoryProductDto.getQuantity());
        inventoryProduct.setMinimumStock(addInventoryProductDto.getMinimumStock());
        inventoryProduct.setMaximumStock(addInventoryProductDto.getMaximumStock());
        inventoryProduct.setUnitPrice(addInventoryProductDto.getUnitPrice());
        inventoryProduct.setDate(LocalDate.now());
        inventoryProduct.setStatus(Status.ACTIVE);
        
        InventoryProduct savedInventoryProduct = inventoryProductRepository.save(inventoryProduct);
        SecureLogger.info("Added product to inventory - Product ID: {}, Inventory ID: {}", product.getId(), inventoryId);
        
        return convertToDto(savedInventoryProduct);
    }
    
    @Override
    public InventoryProductDto updateInventoryProduct(UUID inventoryId, UUID productId, UpdateInventoryProductDto updateInventoryProductDto) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to update products in this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        InventoryProduct inventoryProduct = inventoryProductRepository.findByInventoryAndProduct(inventory, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "inventory", "Product not found in inventory"));

        if (updateInventoryProductDto.getQuantity() != null) {
            inventoryProduct.setQuantity(updateInventoryProductDto.getQuantity());
        }
        if (updateInventoryProductDto.getMinimumStock() != null) {
            inventoryProduct.setMinimumStock(updateInventoryProductDto.getMinimumStock());
        }
        if (updateInventoryProductDto.getMaximumStock() != null) {
            inventoryProduct.setMaximumStock(updateInventoryProductDto.getMaximumStock());
        }
        if (updateInventoryProductDto.getUnitPrice() != null) {
            inventoryProduct.setUnitPrice(updateInventoryProductDto.getUnitPrice());
        }
        if (updateInventoryProductDto.getStatus() != null) {
            inventoryProduct.setStatus(updateInventoryProductDto.getStatus());
        }
        
        InventoryProduct updatedInventoryProduct = inventoryProductRepository.save(inventoryProduct);
        SecureLogger.info("Updated product in inventory - Product ID: {}, Inventory ID: {}", product.getId(), inventoryId);
        
        return convertToDto(updatedInventoryProduct);
    }
    
    @Override
    public void removeProductFromInventory(UUID inventoryId, UUID productId) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to remove products from this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        InventoryProduct inventoryProduct = inventoryProductRepository.findByInventoryAndProduct(inventory, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "inventory", "Product not found in inventory"));
        
        inventoryProductRepository.delete(inventoryProduct);
        SecureLogger.info("Removed product from inventory - Product ID: {}, Inventory ID: {}", product.getId(), inventoryId);
    }
    
    @Override
    public List<InventoryProductDto> getInventoryProducts(UUID inventoryId) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to view this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        return inventoryProductRepository.findByInventory(inventory).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public InventoryProductDto getInventoryProduct(UUID inventoryId, UUID productId) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to view this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        InventoryProduct inventoryProduct = inventoryProductRepository.findByInventoryAndProduct(inventory, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "inventory", "Product not found in inventory"));
        
        return convertToDto(inventoryProduct);
    }
    
    @Override
    public List<InventoryProductDto> getLowStockProducts(UUID inventoryId) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to view this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        return inventoryProductRepository.findByInventoryAndQuantityLessThanEqual(inventory, 0).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryProductDto> getProductsByStatus(UUID inventoryId, String status) {

        if (!isCurrentUserInventory(inventoryId)) {
            throw new ForbiddenException("You are not allowed to view this inventory");
        }
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        
        try {
            Status statusEnum = Status.valueOf(status.toUpperCase());
            
            return inventoryProductRepository.findByInventoryAndStatus(inventory, statusEnum).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
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
    public InventoryProductDto convertToDto(InventoryProduct inventoryProduct) {
        InventoryProductDto dto = new InventoryProductDto();
        dto.setId(inventoryProduct.getId());
        dto.setInventoryId(inventoryProduct.getInventory().getId());
        dto.setProductId(inventoryProduct.getProduct().getId());
        dto.setProductName(inventoryProduct.getProduct().getName());
        dto.setQuantity(inventoryProduct.getQuantity());
        dto.setMinimumStock(inventoryProduct.getMinimumStock());
        dto.setMaximumStock(inventoryProduct.getMaximumStock());
        dto.setUnitPrice(inventoryProduct.getUnitPrice());
        dto.setStatus(inventoryProduct.getStatus());
        return dto;
    }
} 