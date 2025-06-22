package com.example.rawsource.entities.dto.inventory;

import com.example.rawsource.entities.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;
    private UUID userId;
    private String userName;
}
