package com.datn.tourhotel.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelRegistrationDTO;

public interface HotelService {

    Hotel saveHotel(HotelRegistrationDTO hotelRegistrationDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3);

    HotelDTO findHotelDtoByName(String name);

    HotelDTO findHotelDtoById(Long id);

    Optional<Hotel> findHotelById(Long id);

    List<HotelDTO> findAllHotels();

    HotelDTO updateHotel(HotelDTO hotelDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3);

    void deleteHotelById(Long id);

    List<Hotel> findAllHotelsByManagerId(Long managerId);

    List<HotelDTO> findAllHotelDtosByManagerId(Long managerId);

    HotelDTO findHotelByIdAndManagerId(Long hotelId, Long managerId);

    HotelDTO updateHotelByManagerId(HotelDTO hotelDTO, Long managerId, MultipartFile hotelImage, MultipartFile hotelImage2, MultipartFile hotelImage3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3) throws IOException;

    void deleteHotelByIdAndManagerId(Long hotelId, Long managerId);

    HotelDTO mapHotelToHotelDto(Hotel hotel);

}
