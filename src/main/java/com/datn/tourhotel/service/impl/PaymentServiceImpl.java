package com.datn.tourhotel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public BigDecimal getEarningsByPeriodAdmin(String period) {
        switch (period) {
            case "day":
                return paymentRepository.getTotalEarningsTodayAdmin();
            case "week":
                return paymentRepository.getTotalEarningsThisWeekAdmin();
            case "month":
                return paymentRepository.getTotalEarningsThisMonthAdmin();
            case "year":
                return paymentRepository.getTotalEarningsThisYearAdmin();
            default:
                return paymentRepository.getTotalEarningsAdmin();
        }
    }
    
    public BigDecimal getEarningsByPeriod(Long managerId, String period) {
        switch (period) {
            case "day":
                return paymentRepository.getTotalEarningsToday(managerId);
            case "week":
                return paymentRepository.getTotalEarningsThisWeek(managerId);
            case "month":
                return paymentRepository.getTotalEarningsThisMonth(managerId);
            case "year":
                return paymentRepository.getTotalEarningsThisYear(managerId);
            default:
                return paymentRepository.getTotalEarnings(managerId);
        }
    }
    
    public List<BigDecimal> getEarningsPerDayInYearAdmin() {
        List<Object[]> results = paymentRepository.getTotalEarningsPerDayInYearAdmin();
        List<BigDecimal> earningsPerDay = new ArrayList<>(Collections.nCopies(30, BigDecimal.ZERO)); // Giả định năm có 365 ngày

        for (Object[] result : results) {
            int day = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerDay.set(day - 1, total); // Lưu tổng vào vị trí tương ứng với ngày
        }
        return earningsPerDay;
    }
    
    public List<BigDecimal> getEarningsPerDayInYear(Long managerId) {
        List<Object[]> results = paymentRepository.getTotalEarningsPerDayInYear(managerId);
        List<BigDecimal> earningsPerDay = new ArrayList<>(Collections.nCopies(30, BigDecimal.ZERO)); // Giả định năm có 365 ngày

        for (Object[] result : results) {
            int day = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerDay.set(day - 1, total); // Lưu tổng vào vị trí tương ứng với ngày
        }
        return earningsPerDay;
    }
    
    public List<BigDecimal> getEarningsPerWeekInYearAdmin() {
        List<Object[]> results = paymentRepository.getTotalEarningsPerWeekInYearAdmin();
        List<BigDecimal> earningsPerWeek = new ArrayList<>(Collections.nCopies(52, BigDecimal.ZERO)); // Giả định năm có 52 tuần

        for (Object[] result : results) {
            int week = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerWeek.set(week - 1, total); // Lưu tổng vào vị trí tương ứng với tuần
        }
        return earningsPerWeek;
    }
    
    public List<BigDecimal> getEarningsPerMonthInYearAdmin() {
        List<Object[]> results = paymentRepository.getTotalEarningsPerMonthInYearAdmin();
        List<BigDecimal> earningsPerMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO)); // 12 tháng

        for (Object[] result : results) {
            int month = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerMonth.set(month - 1, total); // Lưu tổng vào vị trí tương ứng với tháng
        }
        return earningsPerMonth;
    }
    
    public List<BigDecimal> getEarningsPerWeekInYear(Long managerId) {
        List<Object[]> results = paymentRepository.getTotalEarningsPerWeekInYear(managerId);
        List<BigDecimal> earningsPerWeek = new ArrayList<>(Collections.nCopies(52, BigDecimal.ZERO)); // Giả định năm có 52 tuần

        for (Object[] result : results) {
            int week = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerWeek.set(week - 1, total); // Lưu tổng vào vị trí tương ứng với tuần
        }
        return earningsPerWeek;
    }
    
    public List<BigDecimal> getEarningsPerMonthInYear(Long managerId) {
        List<Object[]> results = paymentRepository.getTotalEarningsPerMonthInYear(managerId);
        List<BigDecimal> earningsPerMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO)); // 12 tháng

        for (Object[] result : results) {
            int month = (Integer) result[0];
            BigDecimal total = (BigDecimal) result[1];
            earningsPerMonth.set(month - 1, total); // Lưu tổng vào vị trí tương ứng với tháng
        }
        return earningsPerMonth;
    }

}
