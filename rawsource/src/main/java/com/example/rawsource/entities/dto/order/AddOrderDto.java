package com.example.rawsource.entities.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.example.rawsource.entities.dto.item.AddItemDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddOrderDto {
        private UUID buyerId;
        private List<AddItemDto> items;
}
