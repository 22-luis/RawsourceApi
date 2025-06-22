package com.example.rawsource.entities.dto.item;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemDto {
    private UUID productId;
    private Integer quantity;
}
