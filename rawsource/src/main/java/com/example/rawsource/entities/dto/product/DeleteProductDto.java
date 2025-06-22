package com.example.rawsource.entities.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Data
public class DeleteProductDto {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private UUID providerId;

}