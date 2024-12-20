package com.datn.tourhotel.service;

import java.util.List;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.BookingInitiationDTO;
import com.datn.tourhotel.model.enums.BookingStatus;
import com.datn.tourhotel.model.enums.PaymentStatus;

public interface BookingService {

    Booking saveBooking(BookingInitiationDTO bookingInitiationDTO, Long customerId);

    BookingDTO confirmBooking(BookingInitiationDTO bookingInitiationDTO, Long customerId);

    List<BookingDTO> findAllBookings();

    BookingDTO findBookingById(Long id);

    List<BookingDTO> findBookingsByCustomerId(Long customerId);

    BookingDTO findBookingByIdAndCustomerId(Long bookingId, Long customerId);

    List<BookingDTO> findBookingsByManagerId(Long managerId);

    BookingDTO findBookingByIdAndManagerId(Long bookingId, Long managerId);

    BookingDTO mapBookingModelToBookingDto(Booking booking);
    
    BookingDTO confirmBookingFromPayment(String confirmationNumber);
    
    void requestRefund(Long bookingId);

    void confirmRefund(Long bookingId);
    
    void updateBookingStatus(Long bookingId, BookingStatus status);
    
    void updatePaymentStatus(Long bookingId, PaymentStatus paymentStatus);

}
