package com.datn.tourhotel.service;

import java.time.LocalDate;
import java.util.List;

import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.dto.HotelAvailabilityDTO;

public interface HotelSearchService {

    List<HotelAvailabilityDTO> findAvailableHotelsByaddressLineAndDate(String addressLine, LocalDate checkinDate, LocalDate checkoutDate);

    HotelAvailabilityDTO findAvailableHotelById(Long hotelId, LocalDate checkinDate, LocalDate checkoutDate);

    HotelAvailabilityDTO mapHotelToHotelAvailabilityDto(Hotel hotel, LocalDate checkinDate, LocalDate checkoutDate);
}
