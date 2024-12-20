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
    public Hotel saveHotel(HotelRegistrationDTO hotelRegistrationDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3) {
        log.info("Attempting to save a new hotel: {}", hotelRegistrationDTO.toString());

        Optional<Hotel> existingHotel = hotelRepository.findByNameAndIsDeleteFalse(hotelRegistrationDTO.getName());
        if (existingHotel.isPresent()) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

     // Nếu không tồn tại hotel, tạo mới hotel
        Hotel hotel = mapHotelRegistrationDtoToHotel(hotelRegistrationDTO);
        
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
        
     // Upload và update rooms
        List<RoomDTO> roomDTOs = hotelRegistrationDTO.getRoomDTOs();
        if (roomDTOs != null && roomImages1 != null && roomImages2 != null && roomImages3 != null) {
            for (int i = 0; i < roomDTOs.size(); i++) {
                RoomDTO roomDTO = roomDTOs.get(i);
                MultipartFile roomImage1 = (roomImages1.size() > i) ? roomImages1.get(i) : null;
                MultipartFile roomImage2 = (roomImages2.size() > i) ? roomImages2.get(i) : null;
                MultipartFile roomImage3 = (roomImages3.size() > i) ? roomImages3.get(i) : null;

                // Upload images to Cloudinary
                Map<String, String> uploadResult = null;
                try {
                    if (roomImage1 != null && !roomImage1.isEmpty()) {
                        uploadResult = cloudinary.uploader().upload(roomImage1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg(uploadResult.get("url").toString());
                    }
                } catch (IOException e) {
                    log.error("Room image upload failed: {}", e.getMessage());
                    throw new RuntimeException("Room image upload failed", e);
                }

                try {
                    if (roomImage2 != null && !roomImage2.isEmpty()) {
                        uploadResult = cloudinary.uploader().upload(roomImage2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg2(uploadResult.get("url").toString());
                    }
                } catch (IOException e) {
                    log.error("Room image upload failed: {}", e.getMessage());
                    throw new RuntimeException("Room image upload failed", e);
                }

                try {
                    if (roomImage3 != null && !roomImage3.isEmpty()) {
                        uploadResult = cloudinary.uploader().upload(roomImage3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg3(uploadResult.get("url").toString());
                    }
                } catch (IOException e) {
                    log.error("Room image upload failed: {}", e.getMessage());
                    throw new RuntimeException("Room image upload failed", e);
                }

                roomService.saveRoom(roomDTO, hotel,roomImage1,roomImage2,roomImage3);
            }
        } else {
            log.warn("RoomDTO list size and room images list size do not match. Rooms will not be updated.");
        }

        // Lưu lần cuối hotel với thông tin đầy đủ
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Successfully saved new hotel with ID: {}", hotel.getId());
        return savedHotel;
    }
    @Transactional
    public Hotel saveHotelAdmin(HotelRegistrationAdminDTO hotelRegistrationAdminDTO, 
                                MultipartFile multipartFile, 
                                MultipartFile multipartFile2, 
                                MultipartFile multipartFile3, 
                                List<MultipartFile> roomImages1, 
                                List<MultipartFile> roomImages2, 
                                List<MultipartFile> roomImages3, 
                                HotelManager hotelManager) {
        log.info("Attempting to save a new hotel: {}", hotelRegistrationAdminDTO.toString());

        // Kiểm tra xem tên khách sạn đã tồn tại chưa
        Optional<Hotel> existingHotel = hotelRepository.findByNameAndIsDeleteFalse(hotelRegistrationAdminDTO.getName());
        if (existingHotel.isPresent()) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

        // Tạo mới Hotel từ DTO
        Hotel hotel = mapHotelRegistrationAdminDtoToHotel(hotelRegistrationAdminDTO);

        // Upload hình ảnh khách sạn lên Cloudinary
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg(uploadResult.get("url").toString());
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Upload hình ảnh thứ hai cho khách sạn
        if (multipartFile2 != null && !multipartFile2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(multipartFile2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg2(uploadResult2.get("url").toString());
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Upload hình ảnh thứ ba cho khách sạn
        if (multipartFile3 != null && !multipartFile3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(multipartFile3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                hotel.setImg3(uploadResult3.get("url").toString());
            } catch (IOException e) {
                log.error("Image upload failed: {}", e.getMessage());
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Lưu địa chỉ
        Address savedAddress = addressService.saveAddress(hotelRegistrationAdminDTO.getAddressDTO());
        hotel.setAddress(savedAddress);

        // Set HotelManager vào khách sạn
        hotel.setHotelManager(hotelManager);

        // Lưu khách sạn vào cơ sở dữ liệu
        hotel = hotelRepository.save(hotel);

        // Upload và cập nhật thông tin các phòng
        List<RoomDTO> roomDTOs = hotelRegistrationAdminDTO.getRoomDTOs();
        if (roomDTOs != null && roomImages1 != null && roomImages2 != null && roomImages3 != null) {
            for (int i = 0; i < roomDTOs.size(); i++) {
                RoomDTO roomDTO = roomDTOs.get(i);
                MultipartFile roomImage1 = (roomImages1.size() > i) ? roomImages1.get(i) : null;
                MultipartFile roomImage2 = (roomImages2.size() > i) ? roomImages2.get(i) : null;
                MultipartFile roomImage3 = (roomImages3.size() > i) ? roomImages3.get(i) : null;

                // Upload hình ảnh phòng 1
                if (roomImage1 != null && !roomImage1.isEmpty()) {
                    try {
                        Map<String, String> uploadResult = cloudinary.uploader().upload(roomImage1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg(uploadResult.get("url").toString());
                    } catch (IOException e) {
                        log.error("Room image upload failed: {}", e.getMessage());
                        throw new RuntimeException("Room image upload failed", e);
                    }
                }

                // Upload hình ảnh phòng 2
                if (roomImage2 != null && !roomImage2.isEmpty()) {
                    try {
                        Map<String, String> uploadResult = cloudinary.uploader().upload(roomImage2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg2(uploadResult.get("url").toString());
                    } catch (IOException e) {
                        log.error("Room image upload failed: {}", e.getMessage());
                        throw new RuntimeException("Room image upload failed", e);
                    }
                }

                // Upload hình ảnh phòng 3
                if (roomImage3 != null && !roomImage3.isEmpty()) {
                    try {
                        Map<String, String> uploadResult = cloudinary.uploader().upload(roomImage3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                        roomDTO.setImg3(uploadResult.get("url").toString());
                    } catch (IOException e) {
                        log.error("Room image upload failed: {}", e.getMessage());
                        throw new RuntimeException("Room image upload failed", e);
                    }
                }

                // Lưu thông tin phòng vào cơ sở dữ liệu
                roomService.saveRoom(roomDTO, hotel, roomImage1, roomImage2, roomImage3);
            }
        }

        // Lưu lại khách sạn với thông tin đầy đủ
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Successfully saved new hotel with ID: {}", hotel.getId());
        return savedHotel;
    } 			

    @Override
    public HotelDTO findHotelDtoByName(String name) {
        Hotel hotel = hotelRepository.findByNameAndIsDeleteFalse(name)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }

    @Override
    public HotelDTO findHotelDtoById(Long id) {
        Hotel hotel = hotelRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }

    @Override
    public Optional<Hotel> findHotelById(Long id) {
        return hotelRepository.findByIdAndIsDeleteFalse(id);
    }

    @Override
    public List<HotelDTO> findAllHotels() {
        // Fetch only hotels that are not marked as deleted
        List<Hotel> hotels = hotelRepository.findAllByIsDeleteFalse();
        
        // Convert the list of hotels to DTOs
        return hotels.stream()
                .map(this::mapHotelToHotelDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public HotelDTO updateHotel(HotelDTO hotelDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3){
        log.info("Attempting to update hotel with ID: {}", hotelDTO.getId());

        Hotel existingHotel = hotelRepository.findByIdAndIsDeleteFalse(hotelDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        if (hotelNameExistsAndNotSameHotel(hotelDTO.getName(), hotelDTO.getId())) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

        existingHotel.setName(hotelDTO.getName());
        existingHotel.setHotelManager(hotelDTO.getManager());
        
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

        existingHotel.setName(hotelDTO.getName());
        existingHotel.setDescribe(hotelDTO.getDescribe());
        
        List<RoomDTO> roomDTOs = hotelDTO.getRoomDTOs();
        if (roomDTOs != null && roomImages1 != null && roomImages2 != null && roomImages3 != null) {
            for (int i = 0; i < roomDTOs.size(); i++) {
                RoomDTO roomDTO = roomDTOs.get(i);
                MultipartFile roomImage1 = (roomImages1.size() > i) ? roomImages1.get(i) : null;
                MultipartFile roomImage2 = (roomImages2.size() > i) ? roomImages2.get(i) : null;
                MultipartFile roomImage3 = (roomImages3.size() > i) ? roomImages3.get(i) : null;

                // Update each room with respective images
                Room updatedRoom = roomService.updateRoom(roomDTO, roomImage1, roomImage2, roomImage3);
                roomDTO.setImg(updatedRoom.getImg());
                roomDTO.setImg2(updatedRoom.getImg2());
                roomDTO.setImg3(updatedRoom.getImg3());
            }
        } else {
            log.warn("RoomDTO list size and room images list size do not match. Rooms will not be updated.");
        }

        hotelRepository.save(existingHotel);
        log.info("Successfully updated existing hotel with ID: {}", hotelDTO.getId());
        return mapHotelToHotelDto(existingHotel);
    }

    @Override
    public void deleteHotelById(Long id) {
        log.info("Attempting to delete hotel with ID: {}", id);
        
        Optional<Hotel> hotelOptional = hotelRepository.findByIdAndIsDeleteFalse(id);
        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            
            // Set the isDelete flag to true to mark the hotel as deleted
            hotel.setIsDelete(true);
            
            // Save the updated hotel object back to the database
            hotelRepository.save(hotel);
            
            log.info("Successfully marked hotel with ID: {} as deleted", id);
        } else {
            log.warn("Hotel with ID: {} not found", id);
        }
    }

    @Override
    public List<Hotel> findAllHotelsByManagerId(Long managerId) {
        List<Hotel> hotels = hotelRepository.findAllByHotelManager_IdAndIsDeleteFalse(managerId);
        return (hotels != null) ? hotels : Collections.emptyList();
    }

    @Override
    public List<HotelDTO> findAllHotelDtosByManagerId(Long managerId) {
        List<Hotel> hotels = hotelRepository.findAllByHotelManager_IdAndIsDeleteFalse(managerId);
        if (hotels != null) {
            return hotels.stream()
                    .map(this::mapHotelToHotelDto)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public HotelDTO findHotelByIdAndManagerId(Long hotelId, Long managerId) {
        Hotel hotel = hotelRepository.findByIdAndHotelManager_IdAndIsDeleteFalse(hotelId, managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        return mapHotelToHotelDto(hotel);
    }
    

    @Override
    @Transactional
    public HotelDTO updateHotelByManagerId(HotelDTO hotelDTO, Long managerId, MultipartFile hotelImage, MultipartFile hotelImage2, MultipartFile hotelImage3, List<MultipartFile> roomImages1, List<MultipartFile> roomImages2, List<MultipartFile> roomImages3) {
        log.info("Attempting to update hotel with ID: {} for Manager ID: {}", hotelDTO.getId(), managerId);

        Hotel existingHotel = hotelRepository.findByIdAndHotelManager_IdAndIsDeleteFalse(hotelDTO.getId(), managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        if (hotelNameExistsAndNotSameHotel(hotelDTO.getName(), hotelDTO.getId())) {
            throw new HotelAlreadyExistsException("This hotel name is already registered!");
        }

        // Upload hotel images if available
        if (hotelImage != null && !hotelImage.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(hotelImage.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg(uploadResult.get("url").toString()); 
            } catch (IOException e) {
                log.error("Hotel image upload failed: {}", e.getMessage());
                throw new RuntimeException("Hotel image upload failed", e);
            }
        }
        
        if (hotelImage2 != null && !hotelImage2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(hotelImage2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg2(uploadResult2.get("url").toString());
            } catch (IOException e) {
                log.error("Hotel image upload failed: {}", e.getMessage());
                throw new RuntimeException("Hotel image upload failed", e);
            }
        }

        if (hotelImage3 != null && !hotelImage3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(hotelImage3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingHotel.setImg3(uploadResult3.get("url").toString());
            } catch (IOException e) {
                log.error("Hotel image upload failed: {}", e.getMessage());
                throw new RuntimeException("Hotel image upload failed", e);
            }
        }

        existingHotel.setName(hotelDTO.getName());
        existingHotel.setDescribe(hotelDTO.getDescribe());

        Address updatedAddress = addressService.updateAddress(hotelDTO.getAddressDTO());
        existingHotel.setAddress(updatedAddress);

        // Update rooms
        List<RoomDTO> roomDTOs = hotelDTO.getRoomDTOs();
        if (roomDTOs != null && roomImages1 != null && roomImages2 != null && roomImages3 != null) {
            for (int i = 0; i < roomDTOs.size(); i++) {
                RoomDTO roomDTO = roomDTOs.get(i);
                MultipartFile roomImage1 = (roomImages1.size() > i) ? roomImages1.get(i) : null;
                MultipartFile roomImage2 = (roomImages2.size() > i) ? roomImages2.get(i) : null;
                MultipartFile roomImage3 = (roomImages3.size() > i) ? roomImages3.get(i) : null;

                // Update each room with respective images
                Room updatedRoom = roomService.updateRoom(roomDTO, roomImage1, roomImage2, roomImage3);
                roomDTO.setImg(updatedRoom.getImg());
                roomDTO.setImg2(updatedRoom.getImg2());
                roomDTO.setImg3(updatedRoom.getImg3());
            }
        } else {
            log.warn("RoomDTO list size and room images list size do not match. Rooms will not be updated.");
        }

        hotelRepository.save(existingHotel);
        log.info("Successfully updated existing hotel with ID: {} for Manager ID: {}", hotelDTO.getId(), managerId);
        return mapHotelToHotelDto(existingHotel);
    }




    @Override
    public void deleteHotelByIdAndManagerId(Long hotelId, Long managerId) {
        log.info("Attempting to delete hotel with ID: {} for Manager ID: {}", hotelId, managerId);
        
        // Find the hotel with the specified ID and manager ID, ensuring it's not marked as deleted
        Hotel hotel = hotelRepository.findByIdAndHotelManager_IdAndIsDeleteFalse(hotelId, managerId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        // Set the isDelete flag to true to mark the hotel as deleted
        hotel.setIsDelete(true);

        // Save the updated hotel object with the isDelete flag set to true
        hotelRepository.save(hotel);
        
        log.info("Successfully marked hotel with ID: {} as deleted for Manager ID: {}", hotelId, managerId);
    }


    private Hotel mapHotelRegistrationDtoToHotel(HotelRegistrationDTO dto) {
        return Hotel.builder()
                .name(formatText(dto.getName()))
                .describe(formatText(dto.getDescribe()))
                .img(dto.getImg())
                .img2(dto.getImg2())
                .img3(dto.getImg3())
                .isDelete(false)
//                .addressDTO(addressDTO)
//                .roomDTOs(roomDTOs)
                .build();
    }
    
    private Hotel mapHotelRegistrationAdminDtoToHotel(HotelRegistrationAdminDTO dto) {
        return Hotel.builder()
                .name(formatText(dto.getName()))
                .describe(formatText(dto.getDescribe()))
                .img(dto.getImg())
                .img2(dto.getImg2())
                .img3(dto.getImg3())
                .isDelete(false)
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
//                .isDelete(hotel.getIsDelete())
                .managerUsername(hotel.getHotelManager().getUser().getUsername())
                .build();
    }

    private boolean hotelNameExistsAndNotSameHotel(String name, Long hotelId) {
        Optional<Hotel> existingHotelWithSameName = hotelRepository.findByNameAndIsDeleteFalse(name);
        return existingHotelWithSameName.isPresent() && !existingHotelWithSameName.get().getId().equals(hotelId);
    }

    private String formatText(String text) {
        return StringUtils.capitalize(text.trim());
    }

}

