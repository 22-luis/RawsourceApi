package com.example.rawsource.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.rawsource.exceptions.ResourceNotFoundException;
import com.example.rawsource.utils.SecureLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.rawsource.entities.Item;
import com.example.rawsource.entities.Order;
import com.example.rawsource.entities.Role;
import com.example.rawsource.entities.User;
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

    public ItemService(ItemRepository itemRepository, OrderRepository orderRepository, UserRepository userRepository){
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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
