package com.datn.tourhotel.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HotelSearchDTO {

    @NotBlank(message = "City cannot be empty")
    private String addressLine;

    @NotNull(message = "Check-in date cannot be empty")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkinDate;

    @NotNull(message = "Check-out date cannot be empty")
    private LocalDate checkoutDate;
}
