package com.datn.tourhotel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.datn.tourhotel.model.enums.PaymentMethod;
import com.datn.tourhotel.model.enums.PaymentStatus;
import com.datn.tourhotel.model.enums.BookingStatus;
import com.datn.tourhotel.model.enums.BookingType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;
    private String confirmationNumber;
    private LocalDateTime bookingDate;
    private Long customerId;
    private Long hotelId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<RoomSelectionDTO> roomSelections = new ArrayList<>();
    private BigDecimal totalPrice;
    private String hotelName;
    private AddressDTO hotelAddress;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BookingType bookingType;
    
    private String orderInfo;
    private String paymentTime;
    private String transactionId;
    private Long userId;
    
    private BookingStatus status;
    
}
