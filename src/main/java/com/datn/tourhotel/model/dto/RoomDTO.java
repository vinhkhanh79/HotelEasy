package com.datn.tourhotel.model.dto;

import com.datn.tourhotel.model.enums.RoomType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private Long id;

    private Long hotelId;

    private RoomType roomType;
    
    private String img;

    @NotNull(message = "Room count cannot be empty")
    @PositiveOrZero(message = "Room count must be 0 or more")
    private Integer roomCount;

    @NotNull(message = "Price cannot be empty")
    @PositiveOrZero(message = "Price per night must be 0 or more")
    private Double pricePerNight;

}