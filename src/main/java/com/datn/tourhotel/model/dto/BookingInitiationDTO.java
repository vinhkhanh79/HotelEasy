package com.datn.tourhotel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.datn.tourhotel.model.enums.BookingType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInitiationDTO {

    private long hotelId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingType bookingType;
    private long durationDays;
    private List<RoomSelectionDTO> roomSelections = new ArrayList<>();
    private BigDecimal totalPrice;

}
