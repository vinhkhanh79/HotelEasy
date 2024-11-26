package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Booking;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByCustomerId(Long customerId);

    Optional<Booking> findBookingByIdAndCustomerId(Long bookingId, Long customerId);

    List<Booking> findBookingsByHotelId(Long hotelId);

    Optional<Booking> findBookingByIdAndHotel_HotelManagerId(Long bookingId, Long hotelManagerId);
    
    Booking findByConfirmationNumber(String confirmationNumber);
    
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId AND b.hotel.id = :hotelId AND b.status = 'COMPLETED'")
    List<Booking> findBookingsByCustomerAndHotel(@Param("customerId") Long customerId, @Param("hotelId") Long hotelId);


}
