package com.example.rawsource.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.rawsource.exceptions.ForbiddenException;
import com.example.rawsource.exceptions.BadRequestException;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.rawsource.entities.Item;
import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.InventoryProduct;
import com.example.rawsource.entities.Order;
import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.Role;
import com.example.rawsource.entities.Status;
import com.example.rawsource.entities.User;
import org.springframework.security.core.Authentication;
import com.example.rawsource.entities.dto.item.AddItemDto;
import com.example.rawsource.entities.dto.item.ItemDto;
import com.example.rawsource.entities.dto.order.AddOrderDto;
import com.example.rawsource.entities.dto.order.OrderDto;
import com.example.rawsource.entities.dto.order.SendItemsDto;
import com.example.rawsource.entities.dto.order.UpdateOrderStatusDto;
import com.example.rawsource.repositories.ItemRepository;
import com.example.rawsource.repositories.OrderRepository;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.repositories.UserRepository;
import com.example.rawsource.repositories.InventoryRepository;
import com.example.rawsource.repositories.InventoryProductRepository;
import com.example.rawsource.services.InventoryService;
import com.example.rawsource.services.InventoryProductService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryService inventoryService;
    private final InventoryProductService inventoryProductService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
            ProductRepository productRepository, ItemRepository itemRepository,
            InventoryRepository inventoryRepository, InventoryProductRepository inventoryProductRepository,
            InventoryService inventoryService, InventoryProductService inventoryProductService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryProductRepository = inventoryProductRepository;
        this.inventoryService = inventoryService;
        this.inventoryProductService = inventoryProductService;
    }

    public OrderDto createOrder(AddOrderDto orderInfo) {
        User buyUser = userRepository.findById(orderInfo.getBuyerId())
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

            if(!buyUser.getRole().equals(Role.BUYER)){
                throw new ForbiddenException("Only buyers can create orders");
            }

            if (orderInfo.getItems() == null || orderInfo.getItems().isEmpty()) {
                throw new RuntimeException("Order must have at least one item");
                }

        Order order = new Order();
        order.setBuyer(buyUser);
        order.setStatus(Status.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        List<Item> items = new ArrayList<>();

        for (AddItemDto addItem : orderInfo.getItems()) {
            Product product = productRepository.findById(addItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            BigDecimal price = product.getPrice();

            Item item = new Item();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(addItem.getQuantity());
            item.setPrice(price);

            total = total.add(price.multiply(BigDecimal.valueOf(addItem.getQuantity())));
            items.add(item);
        }

        order.setItems(items);
        order.setTotalOrder(total);

        Order savOrder = orderRepository.save(order);
        return convertToDto(savOrder, null);
    }

    // All Orders
    public List<OrderDto> getAllOrders(){
        return orderRepository.findAll().stream()
        .map(order -> convertToDto(order, null))
        .collect(Collectors.toList());
    }

    // Get Order by ID
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDto(order, null);
    }

    //update status
    public OrderDto updateOrderStatus(UUID orderId, UpdateOrderStatusDto updateDto){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currrentUser = userRepository.findByEmail(username)
            .orElseThrow(()-> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
            Status newStatus = updateDto.getNewStatus();

            if (newStatus == Status.APPROVED){
                if (!currrentUser.getRole().equals(Role.PROVIDER)) {
                    throw new SecurityException("Only provider can aprove orders");
                }
            }
            if (newStatus == Status.CANCELLED) {
                boolean isBuyer = order.getBuyer().getId().equals(currrentUser.getId());
                boolean isProviderInOrder = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getProvider().getId().equals(currrentUser.getId()));
                if (!isBuyer && !isProviderInOrder) {
                    throw new SecurityException("Only Buyer or Provider in this order can cancel the order");
                }
            }

            if (newStatus == Status.PENDING) {
                throw new IllegalArgumentException("Cannot set back to PENDING");
                
            }

            order.setStatus(newStatus);
            orderRepository.save(order);
            return convertToDto(order, null);
       }
    
       public List<OrderDto> getOrdersByBuyer(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User buyer = userRepository.findByEmail(username)
            .orElseThrow(()->new RuntimeException("User not found"));

        if(!buyer.getRole().equals(Role.BUYER)){
            throw new SecurityException("You are not authorized to see orders");
        }

        List<Order> orders = orderRepository.findByBuyer(buyer);
        return orders.stream()
            .map(order -> convertToDto(order, null))
            .collect(Collectors.toList());
       }

       public List<OrderDto> getOrdersByProvider(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User provider = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

            if (!provider.getRole().equals(Role.PROVIDER)){
                throw new SecurityException("You can not view this order");
            }

        List<Order> orders = itemRepository.findOrdersByProvider(provider.getId());
        
        return orders.stream()
            .map(order -> convertToDto(order, provider.getId()))
            .collect(Collectors.toList());
        }

    public OrderDto deliverItems(UUID orderId, SendItemsDto sendItemsDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!currentUser.getRole().equals(Role.PROVIDER)) {
            throw new ForbiddenException("Only providers can deliver items");
        }

        if (order.getStatus() != Status.APPROVED) {
            throw new ForbiddenException("Only approved orders can be delivered");
        }

        boolean hasProducts = order.getItems().stream()
            .anyMatch(item -> item.getProduct().getProvider().getId().equals(currentUser.getId()));
        
        if (!hasProducts) {
            throw new ForbiddenException("You don't have products in this order");
        }

        for (UUID itemId : sendItemsDto.getItemIds()) {
            Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getOrder().getId().equals(orderId)) {
                throw new ForbiddenException("Item does not belong to this order");
            }

            if (!item.getProduct().getProvider().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only deliver your own products");
            }

            // Transferir items del inventario del provider al buyer
            transferItemToBuyer(item, order.getBuyer());
        }

        order.setStatus(Status.DELIVERED);
        orderRepository.save(order);

        return convertToDto(order, currentUser.getId());
    }

    public void deleteOrder(UUID orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validar que el usuario es el buyer de la orden
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own orders");
        }

        // Validar que la orden está en estado PENDING
        if (order.getStatus() != Status.PENDING) {
            throw new BadRequestException("Only pending orders can be deleted");
        }

        // Eliminar la orden (los items se eliminan automáticamente por CASCADE)
        orderRepository.delete(order);
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
                return inventoryRepository.save(newInventory);
            });
        
        // 3. Descontar del inventario del provider
        InventoryProduct providerInventoryProduct = inventoryProductRepository
            .findByInventoryAndProduct(providerInventory, product)
            .orElseThrow(() -> new RuntimeException("Product not found in provider inventory"));
        
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
                return newInventoryProduct;
            });
        
        buyerInventoryProduct.setQuantity(buyerInventoryProduct.getQuantity() + item.getQuantity());
        inventoryProductRepository.save(buyerInventoryProduct);
    }

    private OrderDto convertToDto(Order order, UUID providerId) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());
        dto.setBuyerName(order.getBuyer().getName());
        dto.setDate(order.getDate());
        dto.setStatus(order.getStatus());
        dto.setTotalOrder(order.getTotalOrder());

        Stream<Item> itemsStream = order.getItems().stream();

        if(providerId != null){
            itemsStream = itemsStream
                .filter(item->item.getProduct().getProvider().getId().equals(providerId));             
        }

        List<ItemDto> itemDtos = itemsStream
                .map(item -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(item.getId());
                    itemDto.setProductName(item.getProduct().getName());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPrice(item.getPrice());
                    return itemDto;
                }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }

    

}
