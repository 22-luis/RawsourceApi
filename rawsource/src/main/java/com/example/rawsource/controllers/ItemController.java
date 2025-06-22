package com.example.rawsource.controllers;

import java.util.List;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rawsource.entities.dto.item.ItemDto;
import com.example.rawsource.services.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    
}
