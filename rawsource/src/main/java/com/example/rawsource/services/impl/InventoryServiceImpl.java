package com.example.rawsource.services.impl;

import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.InventoryProduct;
import com.example.rawsource.entities.Item;
import com.example.rawsource.entities.Order;
import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.Role;
import com.example.rawsource.entities.Status;
import com.example.rawsource.entities.User;
import com.example.rawsource.entities.dto.inventory.AddInventoryDto;
import com.example.rawsource.entities.dto.inventory.DeliverOrderDto;
import com.example.rawsource.entities.dto.inventory.InventoryDto;
import com.example.rawsource.entities.dto.inventory.UpdateInventoryDto;
import com.example.rawsource.repositories.InventoryRepository;
import com.example.rawsource.repositories.InventoryProductRepository;
import com.example.rawsource.repositories.ItemRepository;
import com.example.rawsource.repositories.OrderRepository;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.repositories.UserRepository;
import com.example.rawsource.services.InventoryService;
import com.example.rawsource.utils.SecureLogger;
import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.exceptions.ForbiddenException;
import com.example.rawsource.exceptions.BadRequestException;
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
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private InventoryProductRepository inventoryProductRepository;
    
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
    public void deliverOrder(DeliverOrderDto deliverOrderDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(deliverOrderDto.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validar que el usuario es un provider
        if (!currentUser.getRole().equals(Role.PROVIDER)) {
            throw new ForbiddenException("Only providers can deliver items");
        }

        // Validar que la orden está aprobada
        if (order.getStatus() != Status.APPROVED) {
            throw new BadRequestException("Only approved orders can be delivered");
        }

        // Validar que el provider tiene productos en esta orden
        boolean hasProducts = order.getItems().stream()
            .anyMatch(item -> item.getProduct().getProvider().getId().equals(currentUser.getId()));
        
        if (!hasProducts) {
            throw new ForbiddenException("You don't have products in this order");
        }

        // Transferir todos los items del provider al buyer
        for (Item item : order.getItems()) {
            if (item.getProduct().getProvider().getId().equals(currentUser.getId())) {
                transferItemToBuyer(item, order.getBuyer());
            }
        }

        // Cambiar estado de la orden a DELIVERED
        order.setStatus(Status.DELIVERED);
        orderRepository.save(order);
    }

    private void transferItemToBuyer(Item item, User buyer) {
        Product product = item.getProduct();
        User provider = product.getProvider();
        
        // 1. Obtener inventario del provider
        Inventory providerInventory = inventoryRepository.findByUser(provider)
            .orElseThrow(() -> new RuntimeException("Provider inventory not found"));
        
        // 2. Obtener inventario del buyer (crear si no existe)
        Inventory buyerInventory = inventoryRepository.findByUser(buyer)
            .orElseGet(() -> {
                Inventory newInventory = new Inventory();
                newInventory.setUser(buyer);
                newInventory.setName(buyer.getName() + "'s Inventory");
                newInventory.setDate(LocalDate.now());
                newInventory.setIsActive(true);
                return inventoryRepository.save(newInventory);
            });
        
        // 3. Descontar del inventario del provider
        InventoryProduct providerInventoryProduct = inventoryProductRepository
            .findByInventoryAndProduct(providerInventory, product)
            .orElseGet(() -> {
                InventoryProduct newInventoryProduct = new InventoryProduct();
                newInventoryProduct.setInventory(providerInventory);
                newInventoryProduct.setProduct(product);
                newInventoryProduct.setQuantity(0);
                newInventoryProduct.setDate(LocalDate.now());
                newInventoryProduct.setMinimumStock(0);
                newInventoryProduct.setStatus(Status.ACTIVE);
                return inventoryProductRepository.save(newInventoryProduct);
            });
        
        providerInventoryProduct.setQuantity(providerInventoryProduct.getQuantity() - item.getQuantity());
        inventoryProductRepository.save(providerInventoryProduct);
        
        // 4. Agregar al inventario del buyer
        InventoryProduct buyerInventoryProduct = inventoryProductRepository
            .findByInventoryAndProduct(buyerInventory, product)
            .orElseGet(() -> {
                InventoryProduct newInventoryProduct = new InventoryProduct();
                newInventoryProduct.setInventory(buyerInventory);
                newInventoryProduct.setProduct(product);
                newInventoryProduct.setQuantity(0);
                newInventoryProduct.setDate(LocalDate.now());
                newInventoryProduct.setMinimumStock(0);
                newInventoryProduct.setStatus(Status.ACTIVE);
                return newInventoryProduct;
            });
        
        buyerInventoryProduct.setQuantity(buyerInventoryProduct.getQuantity() + item.getQuantity());
        inventoryProductRepository.save(buyerInventoryProduct);
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