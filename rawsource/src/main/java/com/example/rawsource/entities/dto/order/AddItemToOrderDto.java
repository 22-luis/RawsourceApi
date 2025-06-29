package com.example.rawsource.entities.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddItemToOrderDto {

    private UUID productId;
    private int quantity;


}
