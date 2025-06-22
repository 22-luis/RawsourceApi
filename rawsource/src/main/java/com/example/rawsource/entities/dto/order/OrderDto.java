package com.example.rawsource.entities.dto.order;

import java.lang.Thread.State;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.example.rawsource.entities.Status;
import com.example.rawsource.entities.dto.item.ItemDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private UUID id;
    private UUID buyerId;
    private String buyerName;
    private LocalDate date;
    private Status status;
    private List<ItemDto> items;
    private BigDecimal totalOrder;
    
}
