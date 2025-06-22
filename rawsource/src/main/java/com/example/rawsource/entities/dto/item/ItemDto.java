package com.example.rawsource.entities.dto.item;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private UUID id;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
