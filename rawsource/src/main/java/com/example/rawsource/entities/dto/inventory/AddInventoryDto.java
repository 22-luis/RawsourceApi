package com.example.rawsource.entities.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddInventoryDto {
    @NotBlank(message = "El nombre del inventario es obligatorio")
    private String name;
    
    private String description;
} 