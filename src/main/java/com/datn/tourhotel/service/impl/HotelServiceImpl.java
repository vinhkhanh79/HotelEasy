package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.datn.tourhotel.exception.HotelAlreadyExistsException;
import com.datn.tourhotel.model.*;
import com.datn.tourhotel.model.dto.*;
import com.datn.tourhotel.repository.HotelRepository;
import com.datn.tourhotel.service.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final AddressService addressService;
    private final RoomService roomService;
    private final UserService userService;
    private final HotelManagerService hotelManagerService;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public Hotel saveHotel(HotelRegistrationDTO hotelRegistrationDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3) {
        log.info("Attempting to save a new hotel: {}", hotelRegistrationDTO.toString());

        Optional<Hotel> existingHotel = hotelRepository.findByName(hotelRegistrationDTO.getName());
        if (existingHotel.isPresent()) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

     // Nếu không tồn tại hotel, tạo mới hotel
        Hotel hotel = mapHotelRegistrationDtoToHotel(hotelRegistrationDTO);
        
     // Upload file lên Cloudinary nếu có MultipartFile
     // Upload file lên Cloudinary nếu có MultipartFile
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg(uploadResult.get("url").toString()); // Set URL của hình ảnh đầu tiên
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }
        
        if (multipartFile2 != null && !multipartFile2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(multipartFile2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg2(uploadResult2.get("url").toString()); // Set URL của hình ảnh thứ hai
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        if (multipartFile3 != null && !multipartFile3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(multipartFile3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg3(uploadResult3.get("url").toString()); // Set URL của hình ảnh thứ ba
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Lưu địa chỉ
        Address savedAddress = addressService.saveAddress(hotelRegistrationDTO.getAddressDTO());
        hotel.setAddress(savedAddress);

        // Lấy thông tin người quản lý hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        HotelManager hotelManager = hotelManagerService.findByUser(userService.findUserByUsername(username));
        hotel.setHotelManager(hotelManager);

        // Lưu hotel để có thể liên kết với các phòng
        hotel = hotelRepository.save(hotel);

        // Lưu các phòng của hotel
        List<Room> savedRooms = roomService.saveRooms(hotelRegistrationDTO.getRoomDTOs(), hotel);
        hotel.setRooms(savedRooms);

        // Lưu lần cuối hotel với thông tin đầy đủ
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Successfully saved new hotel with ID: {}", hotel.getId());
        return savedHotel;
    }

    @Override
    public HotelDTO findHotelDtoByName(String name) {
        Hotel hotel = hotelRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }

    @Override
    public HotelDTO findHotelDtoById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }

    @Override
    public Optional<Hotel> findHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    public List<HotelDTO> findAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .map(this::mapHotelToHotelDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HotelDTO updateHotel(HotelDTO hotelDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3){
        log.info("Attempting to update hotel with ID: {}", hotelDTO.getId());

        Hotel existingHotel = hotelRepository.findById(hotelDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        if (hotelNameExistsAndNotSameHotel(hotelDTO.getName(), hotelDTO.getId())) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

        existingHotel.setName(hotelDTO.getName());
        
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg(uploadResult.get("url").toString()); // Set URL của hình ảnh đầu tiên
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }
        
        if (multipartFile2 != null && !multipartFile2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(multipartFile2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg2(uploadResult2.get("url").toString()); // Set URL của hình ảnh thứ hai
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        if (multipartFile3 != null && !multipartFile3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(multipartFile3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg3(uploadResult3.get("url").toString()); // Set URL của hình ảnh thứ ba
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }


        Address updatedAddress = addressService.updateAddress(hotelDTO.getAddressDTO());
        existingHotel.setAddress(updatedAddress);

        hotelDTO.getRoomDTOs().forEach(roomService::updateRoom);

        hotelRepository.save(existingHotel);
        log.info("Successfully updated existing hotel with ID: {}", hotelDTO.getId());
        return mapHotelToHotelDto(existingHotel);
    }

    @Override
    public void deleteHotelById(Long id) {
        log.info("Attempting to delete hotel with ID: {}", id);
        hotelRepository.deleteById(id);
        log.info("Successfully deleted hotel with ID: {}", id);
    }
    @Override
    public List<Hotel> findAllHotelsByManagerId(Long managerId) {
        List<Hotel> hotels = hotelRepository.findAllByHotelManager_Id(managerId);
        return (hotels != null) ? hotels : Collections.emptyList();
    }

    @Override
    public List<HotelDTO> findAllHotelDtosByManagerId(Long managerId) {
        List<Hotel> hotels = hotelRepository.findAllByHotelManager_Id(managerId);
        if (hotels != null) {
            return hotels.stream()
                    .map(this::mapHotelToHotelDto)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public HotelDTO findHotelByIdAndManagerId(Long hotelId, Long managerId) {
        Hotel hotel = hotelRepository.findByIdAndHotelManager_Id(hotelId, managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }
    

    @Override
    @Transactional
    public HotelDTO updateHotelByManagerId(HotelDTO hotelDTO, Long managerId, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3) {
        log.info("Attempting to update hotel with ID: {} for Manager ID: {}", hotelDTO.getId(), managerId);

        Hotel existingHotel = hotelRepository.findByIdAndHotelManager_Id(hotelDTO.getId(), managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        if (hotelNameExistsAndNotSameHotel(hotelDTO.getName(), hotelDTO.getId())) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

     // Upload file lên Cloudinary nếu có MultipartFile
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg(uploadResult.get("url").toString()); // Set URL của hình ảnh đầu tiên
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }
        
        if (multipartFile2 != null && !multipartFile2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(multipartFile2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg2(uploadResult2.get("url").toString()); // Set URL của hình ảnh thứ hai
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        if (multipartFile3 != null && !multipartFile3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(multipartFile3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg3(uploadResult3.get("url").toString()); // Set URL của hình ảnh thứ ba
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        existingHotel.setName(hotelDTO.getName());
        existingHotel.setDescribe(hotelDTO.getDescribe());

        Address updatedAddress = addressService.updateAddress(hotelDTO.getAddressDTO());
        existingHotel.setAddress(updatedAddress);

        hotelDTO.getRoomDTOs().forEach(roomService::updateRoom);

        hotelRepository.save(existingHotel);
        log.info("Successfully updated existing hotel with ID: {} for Manager ID: {}", hotelDTO.getId(), managerId);
        return mapHotelToHotelDto(existingHotel);
    }



    @Override
    public void deleteHotelByIdAndManagerId(Long hotelId, Long managerId) {
        log.info("Attempting to delete hotel with ID: {} for Manager ID: {}", hotelId, managerId);
        Hotel hotel = hotelRepository.findByIdAndHotelManager_Id(hotelId, managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        hotelRepository.delete(hotel);
        log.info("Successfully deleted hotel with ID: {} for Manager ID: {}", hotelId, managerId);
    }

    private Hotel mapHotelRegistrationDtoToHotel(HotelRegistrationDTO dto) {
        return Hotel.builder()
                .name(formatText(dto.getName()))
                .describe(formatText(dto.getDescribe()))
                .img(dto.getImg())
                .img2(dto.getImg2())
                .img3(dto.getImg3())
//                .addressDTO(addressDTO)
//                .roomDTOs(roomDTOs)
                .build();
    }

    @Override
    public HotelDTO mapHotelToHotelDto(Hotel hotel) {
        List<RoomDTO> roomDTOs = hotel.getRooms().stream()
                .map(roomService::mapRoomToRoomDto)  // convert each Room to RoomDTO
                .collect(Collectors.toList());  // collect results to a list

        AddressDTO addressDTO = addressService.mapAddressToAddressDto(hotel.getAddress());

        return HotelDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .img(hotel.getImg())
                .img2(hotel.getImg2())
                .img3(hotel.getImg3())
                .describe(hotel.getDescribe())
                .addressDTO(addressDTO)
                .roomDTOs(roomDTOs)
                .managerUsername(hotel.getHotelManager().getUser().getUsername())
                .build();
    }

    private boolean hotelNameExistsAndNotSameHotel(String name, Long hotelId) {
        Optional<Hotel> existingHotelWithSameName = hotelRepository.findByName(name);
        return existingHotelWithSameName.isPresent() && !existingHotelWithSameName.get().getId().equals(hotelId);
    }

    private String formatText(String text) {
        return StringUtils.capitalize(text.trim());
    }

}

