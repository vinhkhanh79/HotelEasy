package com.datn.tourhotel.service;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.Payment;
import com.datn.tourhotel.model.dto.BookingInitiationDTO;

public interface PaymentService {

    Payment savePayment(BookingInitiationDTO bookingInitiationDTO, Booking booking);
}
