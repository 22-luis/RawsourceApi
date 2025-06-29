package com.example.rawsource.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.rawsource.entities.*;
import com.example.rawsource.entities.dto.item.AddItemDto;
import com.example.rawsource.entities.dto.item.AddItemToOrderDto;
import com.example.rawsource.exceptions.BadRequestException;
import com.example.rawsource.exceptions.ForbiddenException;
import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.repositories.ProductRepository;
import com.example.rawsource.utils.SecureLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.rawsource.entities.dto.item.ItemDto;
import com.example.rawsource.repositories.ItemRepository;
import com.example.rawsource.repositories.OrderRepository;
import com.example.rawsource.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ItemService(ItemRepository itemRepository, OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository){
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public ItemDto addItemToOrder(UUID orderId, AddItemToOrderDto addItemToOrderDto){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Status.PENDING) {
            throw new BadRequestException("Only pending orders can be modified");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only modify your own orders");
        }

        Product product = productRepository.findProductById(addItemToOrderDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", addItemToOrderDto.getProductId()));

        Item existingItem = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity = existingItem.getQuantity() + addItemToOrderDto.getQuantity();

            if (newQuantity <= 0) {
                order.getItems().remove(existingItem);
                itemRepository.delete(existingItem);
            } else {
                existingItem.setQuantity(newQuantity);
            }
        } else {
            if (addItemToOrderDto.getQuantity() > 0) {
                Item item = new Item();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(addItemToOrderDto.getQuantity());
                item.setPrice(product.getPrice());

                order.getItems().add(item);
                itemRepository.save(item);
            }
        }

        BigDecimal total = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalOrder(total);

        if (existingItem != null && existingItem.getQuantity() > 0) {
            return convertToDto(existingItem);
        }

        return null;
    }

    public ItemDto addItemToOrder(UUID orderId, AddItemDto addItemDto){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Status.PENDING) {
            throw new BadRequestException("Only pending orders can be modified");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only modify your own orders");
        }

        Product product = productRepository.findProductById(addItemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", addItemDto.getProductId()));

        Item existingItem = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity = existingItem.getQuantity() + addItemDto.getQuantity();

            if (newQuantity <= 0) {
                order.getItems().remove(existingItem);
                itemRepository.delete(existingItem);
            } else {
                existingItem.setQuantity(newQuantity);
            }
        } else {
            if (addItemDto.getQuantity() > 0) {
                Item item = new Item();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(addItemDto.getQuantity());
                item.setPrice(product.getPrice());

                order.getItems().add(item);
                itemRepository.save(item);
            }
        }

        BigDecimal total = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalOrder(total);

        if (existingItem != null && existingItem.getQuantity() > 0) {
            return convertToDto(existingItem);
        }

        return null;
    }

    public ItemDto getItemById(String id) {
        UUID itemId = UUID.fromString(id);
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        return convertToDto(item);
    }

    public List<ItemDto> getItemsByOrder(UUID orderId) {
        SecureLogger.info("Buscando items de la orden con ID: {}", orderId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        SecureLogger.info("Order encontrada con fecha {}", order.getDate());

        if (currentUser.getRole() == Role.BUYER &&
            !order.getBuyer().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only view your own orders");
        }

        if (currentUser.getRole() == Role.PROVIDER) {
            boolean hasProduct = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getProvider().getId().equals(currentUser.getId()));
            if (!hasProduct) {
                throw new SecurityException("You don't have products in this order");
            }
        }

        List<Item> items = itemRepository.findByOrder(order);
        SecureLogger.info("Número de items encontrados: {}", items.size());

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ItemDto convertToDto(Item item){
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
