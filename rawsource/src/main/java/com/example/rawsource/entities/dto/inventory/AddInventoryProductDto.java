package com.example.rawsource.entities.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddInventoryProductDto {
    private UUID productId;
    private Integer quantity;
    private Integer minimumStock;
    private Integer maximumStock;
    private BigDecimal unitPrice;
}
