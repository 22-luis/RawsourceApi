package com.example.rawsource.entities.dto.inventory;

import com.example.rawsource.entities.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryProductDto {
    private UUID id;
    private Integer quantity;
    private Integer minimumStock;
    private Integer maximumStock;
    private BigDecimal unitPrice;
    private Status status;
}
