package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.datn.tourhotel.model.*;
import com.datn.tourhotel.model.dto.AddressDTO;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.BookingInitiationDTO;
import com.datn.tourhotel.model.dto.RoomSelectionDTO;
import com.datn.tourhotel.model.enums.BookingStatus;
import com.datn.tourhotel.repository.BookingRepository;
import com.datn.tourhotel.service.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final PaymentService paymentService;
    private final CustomerService customerService;
    private final HotelService hotelService;


    @Override
    @Transactional
    public Booking saveBooking(BookingInitiationDTO bookingInitiationDTO, Long userId) {
        validateBookingDates(bookingInitiationDTO.getCheckinDate(), bookingInitiationDTO.getCheckoutDate());

        Customer customer = customerService.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with user ID: " + userId));

        Hotel hotel = hotelService.findHotelById(bookingInitiationDTO.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + bookingInitiationDTO.getHotelId()));

        Booking booking = mapBookingInitDtoToBookingModel(bookingInitiationDTO, customer, hotel);
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public BookingDTO confirmBooking(BookingInitiationDTO bookingInitiationDTO, Long customerId) {
        Booking savedBooking = this.saveBooking(bookingInitiationDTO, customerId);
        Payment savedPayment = paymentService.savePayment(bookingInitiationDTO, savedBooking);
        savedBooking.setPayment(savedPayment);
        savedBooking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(savedBooking);
        availabilityService.updateAvailabilities(bookingInitiationDTO.getHotelId(), bookingInitiationDTO.getCheckinDate(),
                bookingInitiationDTO.getCheckoutDate(), bookingInitiationDTO.getRoomSelections());
        return mapBookingModelToBookingDto(savedBooking);
    }

    @Override
    public List<BookingDTO> findAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::mapBookingModelToBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO findBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));
        return mapBookingModelToBookingDto(booking);
    }

    @Override
    public List<BookingDTO> findBookingsByCustomerId(Long customerId) {
        List<Booking> bookingDTOs = bookingRepository.findBookingsByCustomerId(customerId);
        return bookingDTOs.stream()
                .map(this::mapBookingModelToBookingDto)
                .sorted(Comparator.comparing(BookingDTO::getCheckinDate))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO findBookingByIdAndCustomerId(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findBookingByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));
        return mapBookingModelToBookingDto(booking);
    }

    @Override
    public List<BookingDTO> findBookingsByManagerId(Long managerId) {
        List<Hotel> hotels = hotelService.findAllHotelsByManagerId(managerId);
        return hotels.stream()
                .flatMap(hotel -> bookingRepository.findBookingsByHotelId(hotel.getId()).stream())
                .map(this::mapBookingModelToBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO findBookingByIdAndManagerId(Long bookingId, Long managerId) {
        Booking booking = bookingRepository.findBookingByIdAndHotel_HotelManagerId(bookingId, managerId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId + " and manager ID: " + managerId));
        return mapBookingModelToBookingDto(booking);
    }

    private void validateBookingDates(LocalDate checkinDate, LocalDate checkoutDate) {
        if (checkinDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        if (checkoutDate.isBefore(checkinDate.plusDays(1))) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }
    
    @Override
    public BookingDTO confirmBookingFromPayment(String confirmationNumber) {
        // Find the booking by the confirmation number
        Booking booking = bookingRepository.findByConfirmationNumber(confirmationNumber);
        if (booking != null) {
            // Update the payment status, if necessary
            // booking.getPayment().setPaymentStatus(PaymentStatus.SUCCESS); // Example
            // Save the updated booking
            bookingRepository.save(booking);
            return mapBookingModelToBookingDto(booking);
        }
        return null; // Handle case where booking is not found
    }
    @Override
    public BookingDTO mapBookingModelToBookingDto(Booking booking) {
        AddressDTO addressDto = AddressDTO.builder()
                .addressLine(booking.getHotel().getAddress().getAddressLine())
                .city(booking.getHotel().getAddress().getCity())
                .country(booking.getHotel().getAddress().getCountry())
                .build();

        List<RoomSelectionDTO> roomSelections = booking.getBookedRooms().stream()
                .map(room -> RoomSelectionDTO.builder()
                        .roomType(room.getRoomType())
                        .count(room.getCount())
                        .build())
                .collect(Collectors.toList());

        User customerUser = booking.getCustomer().getUser();

        return BookingDTO.builder()
                .id(booking.getId())
                .confirmationNumber(booking.getConfirmationNumber())
                .bookingDate(booking.getBookingDate())
                .customerId(booking.getCustomer().getId())
                .hotelId(booking.getHotel().getId())
                .checkinDate(booking.getCheckinDate())
                .checkoutDate(booking.getCheckoutDate())
                .roomSelections(roomSelections)
                .totalPrice(booking.getPayment().getTotalPrice())
                .hotelName(booking.getHotel().getName())
                .hotelAddress(addressDto)
                .customerName(customerUser.getName() + " " + customerUser.getLastName())
                .customerEmail(customerUser.getEmail())
                .customerPhone(customerUser.getPhone())
                .paymentStatus(booking.getPayment().getPaymentStatus())
                .paymentMethod(booking.getPayment().getPaymentMethod())
                .status(booking.getStatus())
                .build();
    }

    private Booking mapBookingInitDtoToBookingModel(BookingInitiationDTO bookingInitiationDTO, Customer customer, Hotel hotel) {
        Booking booking = Booking.builder()
                .customer(customer)
                .hotel(hotel)
                .checkinDate(bookingInitiationDTO.getCheckinDate())
                .checkoutDate(bookingInitiationDTO.getCheckoutDate())
                .status(BookingStatus.PENDING)
                .build();

        for (RoomSelectionDTO roomSelection : bookingInitiationDTO.getRoomSelections()) {
            if (roomSelection.getCount() > 0) {
                BookedRoom bookedRoom = BookedRoom.builder()
                        .booking(booking)
                        .roomType(roomSelection.getRoomType())
                        .count(roomSelection.getCount())
                        .build();
                booking.getBookedRooms().add(bookedRoom);
            }
        }

        return booking;
    }

    @Override
    @Transactional
    public void requestRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Only completed bookings can request refund");
        }
        
        booking.setStatus(BookingStatus.REQUESTING_REFUND);
        bookingRepository.save(booking);
        
        // Có thể thêm logic gửi email thông báo cho admin/staff
    }

    @Override
    @Transactional
    public void confirmRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        
        if (booking.getStatus() != BookingStatus.REQUESTING_REFUND) {
            throw new IllegalStateException("Only bookings with REQUESTING_REFUND status can be confirmed for refund");
        }
        
        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);
        
        // Có thể thêm logic gửi email thông báo cho khách hàng
    }

}
