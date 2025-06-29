package com.example.rawsource.controllers;

import java.util.List;
import java.util.UUID;

import com.example.rawsource.entities.dto.item.AddItemToOrderDto;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.rawsource.entities.dto.item.ItemDto;
import com.example.rawsource.services.ItemService;


@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('BUYER', 'PROVIDER')")
    public ResponseEntity<List<ItemDto>> getItemByOrder(@PathVariable UUID orderId){
        List<ItemDto> items = itemService.getItemsByOrder(orderId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/order/{orderId}/modify")
    public ResponseEntity<?> modifyOrderItems(@PathVariable UUID orderId, @RequestBody AddItemToOrderDto modifyItemDto) {
        ItemDto result = itemService.addItemToOrder(orderId, modifyItemDto);

        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok("Item removed from order");
        }
    }
    
}
