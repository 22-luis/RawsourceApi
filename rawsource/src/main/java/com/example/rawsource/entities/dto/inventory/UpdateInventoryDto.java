package com.example.rawsource.entities.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryDto {
    @NotNull(message = "El ID es obligatorio")
    private UUID id;
    
    @NotBlank(message = "El nombre del inventario es obligatorio")
    private String name;
    
    private String description;
    
    private Boolean isActive;
} 