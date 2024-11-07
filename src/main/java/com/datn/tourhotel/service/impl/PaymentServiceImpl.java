package com.datn.tourhotel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.Payment;
import com.datn.tourhotel.model.dto.BookingInitiationDTO;
import com.datn.tourhotel.model.enums.Currency;
import com.datn.tourhotel.model.enums.PaymentMethod;
import com.datn.tourhotel.model.enums.PaymentStatus;
import com.datn.tourhotel.repository.PaymentRepository;
import com.datn.tourhotel.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment savePayment(BookingInitiationDTO bookingInitiationDTO, Booking booking) {
        Payment payment = Payment.builder()
                .booking(booking)
                .totalPrice(bookingInitiationDTO.getTotalPrice())
                .paymentStatus(PaymentStatus.PENDING) // Assuming the payment is completed
                .paymentMethod(PaymentMethod.CREDIT_CARD) // Default to CREDIT_CARD
                .currency(Currency.VND) // Default to VND
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with transaction ID: {}", savedPayment.getTransactionId());

        return savedPayment;
    }
}
