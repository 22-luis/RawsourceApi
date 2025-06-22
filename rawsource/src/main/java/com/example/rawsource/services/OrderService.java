package com.example.rawsource.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.rawsource.exceptions.ForbiddenException;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.rawsource.entities.Item;
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
import com.example.rawsource.entities.dto.order.UpdateOrderStatusDto;
import com.example.rawsource.repositories.ItemRepository;
import com.example.rawsource.repositories.OrderRepository;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
            ProductRepository productRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
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
                if (!currrentUser.getRole().equals(Role.ADMIN)) {
                    throw new SecurityException("Only admins can aprove orders");
                }
            }
            if (newStatus == Status.CANCELLED) {
                if (!order.getBuyer().getId().equals(currrentUser.getId())) {
                    throw new SecurityException("Only Buyer can cancel the order");
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
