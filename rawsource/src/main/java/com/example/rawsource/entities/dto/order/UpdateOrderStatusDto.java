package com.example.rawsource.entities.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.rawsource.entities.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusDto {
    private Status newStatus;
}
