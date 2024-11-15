package com.datn.tourhotel.service;

import java.math.BigDecimal;
import java.util.List;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.Payment;
import com.datn.tourhotel.model.dto.BookingInitiationDTO;

public interface PaymentService {

    Payment savePayment(BookingInitiationDTO bookingInitiationDTO, Booking booking);
    
    public BigDecimal getEarningsByPeriodAdmin(String period);
    
    public BigDecimal getEarningsByPeriod(Long managerId, String period);
    
    List<BigDecimal> getEarningsPerDayInYearAdmin();
    
    List<BigDecimal> getEarningsPerDayInYear(Long managerId);
    
    List<BigDecimal> getEarningsPerWeekInYearAdmin();
    
    List<BigDecimal> getEarningsPerMonthInYearAdmin();
    
    List<BigDecimal> getEarningsPerWeekInYear(Long managerId);
    	
    List<BigDecimal> getEarningsPerMonthInYear(Long managerId);
}
