package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.dto.AddressDTO;
import com.datn.tourhotel.model.dto.HotelAvailabilityDTO;
import com.datn.tourhotel.model.dto.RoomDTO;
import com.datn.tourhotel.model.enums.RoomType;
import com.datn.tourhotel.repository.HotelRepository;
import com.datn.tourhotel.service.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelSearchServiceImpl implements HotelSearchService {

    private final HotelRepository hotelRepository;
    private final AddressService addressService;
    private final RoomService roomService;
    private final AvailabilityService availabilityService;

    @Override
    public List<HotelAvailabilityDTO> findAvailableHotelsByaddressLineAndDate(String addressLine, LocalDate checkinDate, LocalDate checkoutDate) {
        validateCheckinAndCheckoutDates(checkinDate, checkoutDate);

        log.info("Attempting to find hotels in {} with available rooms from {} to {}", addressLine, checkinDate, checkoutDate);

        // Number of days between check-in and check-out
        Long numberOfDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);

        // 1. Fetch hotels that satisfy the criteria (min 1 available room throughout the booking range)
        List<Hotel> hotelsWithAvailableRooms = hotelRepository.findHotelsWithAvailableRooms(addressLine, checkinDate, checkoutDate, numberOfDays);

        // 2. Fetch hotels that don't have any availability records for the entire booking range
        List<Hotel> hotelsWithoutAvailabilityRecords = hotelRepository.findHotelsWithoutAvailabilityRecords(addressLine, checkinDate, checkoutDate);

        // 3. Fetch hotels with partial availability; some days with records meeting the criteria and some days without any records
        List<Hotel> hotelsWithPartialAvailabilityRecords = hotelRepository.findHotelsWithPartialAvailabilityRecords(addressLine, checkinDate, checkoutDate, numberOfDays);

        // Combine and deduplicate the hotels using a Set
        Set<Hotel> combinedHotels = new HashSet<>(hotelsWithAvailableRooms);
        combinedHotels.addAll(hotelsWithoutAvailabilityRecords);
        combinedHotels.addAll(hotelsWithPartialAvailabilityRecords);

        log.info("Successfully found {} hotels with available rooms", combinedHotels.size());

        // Convert the combined hotel list to DTOs for the response
        return combinedHotels.stream()
                .map(hotel -> mapHotelToHotelAvailabilityDto(hotel, checkinDate, checkoutDate))
                .collect(Collectors.toList());
    }

    @Override
    public HotelAvailabilityDTO findAvailableHotelById(Long hotelId, LocalDate checkinDate, LocalDate checkoutDate) {
        validateCheckinAndCheckoutDates(checkinDate, checkoutDate);

        log.info("Attempting to find hotel with ID {} with available rooms from {} to {}", hotelId, checkinDate, checkoutDate);

        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (hotelOptional.isEmpty()) {
            log.error("No hotel found with ID: {}", hotelId);
            throw new EntityNotFoundException("Hotel not found");
        }

        Hotel hotel = hotelOptional.get();
        return mapHotelToHotelAvailabilityDto(hotel, checkinDate, checkoutDate);
    }

    @Override
    public HotelAvailabilityDTO mapHotelToHotelAvailabilityDto(Hotel hotel, LocalDate checkinDate, LocalDate checkoutDate) {
        List<RoomDTO> roomDTOs = hotel.getRooms().stream()
                .map(roomService::mapRoomToRoomDto)  // convert each Room to RoomDTO
                .collect(Collectors.toList());

        AddressDTO addressDTO = addressService.mapAddressToAddressDto(hotel.getAddress());
        
        HotelAvailabilityDTO hotelAvailabilityDTO = HotelAvailabilityDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .describe(hotel.getDescribe())
                .addressDTO(addressDTO)
                .roomDTOs(roomDTOs)
                .img(hotel.getImg())  // Set the image field
                .img2(hotel.getImg2())
                .img3(hotel.getImg3())
                .build();
        
        // For each room type, find the minimum available rooms across the date range
        int maxAvailableSingleRooms = hotel.getRooms().stream()
                .filter(room -> room.getRoomType() == RoomType.SINGLE)
                .mapToInt(room -> availabilityService.getMinAvailableRooms(room.getId(), checkinDate, checkoutDate))
                .max()
                .orElse(0); // Assume no single rooms if none match the filter
        hotelAvailabilityDTO.setMaxAvailableSingleRooms(maxAvailableSingleRooms);

        int maxAvailableDoubleRooms = hotel.getRooms().stream()
                .filter(room -> room.getRoomType() == RoomType.DOUBLE)
                .mapToInt(room -> availabilityService.getMinAvailableRooms(room.getId(), checkinDate, checkoutDate))
                .max()
                .orElse(0); // Assume no double rooms if none match the filter
        hotelAvailabilityDTO.setMaxAvailableDoubleRooms(maxAvailableDoubleRooms);
        
        int maxAvailableSuiteRooms = hotel.getRooms().stream()
                .filter(room -> room.getRoomType() == RoomType.SUITE)
                .mapToInt(room -> availabilityService.getMinAvailableRooms(room.getId(), checkinDate, checkoutDate))
                .max()
                .orElse(0); // Assume no double rooms if none match the filter
        hotelAvailabilityDTO.setMaxAvailableSuiteRooms(maxAvailableSuiteRooms);

        return hotelAvailabilityDTO;
    }

    private void validateCheckinAndCheckoutDates(LocalDate checkinDate, LocalDate checkoutDate) {
        if (checkinDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
//        if (checkoutDate.isBefore(checkinDate.plusDays(1))) {
//            throw new IllegalArgumentException("Check-out date must be after check-in date");
//        }
    }
}
