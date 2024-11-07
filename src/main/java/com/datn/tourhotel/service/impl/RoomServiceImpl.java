package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.Room;
import com.datn.tourhotel.model.dto.RoomDTO;
import com.datn.tourhotel.repository.RoomRepository;
import com.datn.tourhotel.service.RoomService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final Cloudinary cloudinary;

    @Override
    public Room saveRoom(RoomDTO roomDTO, Hotel hotel, MultipartFile roomImage1, MultipartFile roomImage2, MultipartFile roomImage3) {
        log.info("Attempting to save a new room: {}", roomDTO);
        Room room = mapRoomDtoToRoom(roomDTO, hotel);

        // Upload ảnh phòng nếu có
        if (roomImage1 != null && !roomImage1.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(roomImage1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                room.setImg(uploadResult.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        if (roomImage2 != null && !roomImage2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(roomImage2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                room.setImg2(uploadResult2.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        if (roomImage3 != null && !roomImage3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(roomImage3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                room.setImg3(uploadResult3.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        room = roomRepository.save(room);
        log.info("Successfully saved room with ID: {}", room.getId());
        return room;
    }

    @Override
    public List<Room> saveRooms(List<RoomDTO> roomDTOs, Hotel hotel) {
        log.info("Attempting to save rooms: {}", roomDTOs);
        List<Room> rooms = roomDTOs.stream()
                .map(roomDTO -> saveRoom(roomDTO, hotel, null, null, null)) // save each room
                .collect(Collectors.toList());
        log.info("Successfully saved rooms: {}", rooms);
        return rooms;
    }

    @Override
    public Optional<Room> findRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public List<Room> findRoomsByHotelId(Long hotelId) {
        return null;
    }

    @Override
    public Room updateRoom(RoomDTO roomDTO, MultipartFile roomImage1, MultipartFile roomImage2, MultipartFile roomImage3) {
        log.info("Attempting to update room with ID: {}", roomDTO.getId());
        Room existingRoom = roomRepository.findById(roomDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        log.info("Room with ID: {} found", roomDTO.getId());

        existingRoom.setRoomType(roomDTO.getRoomType());
        existingRoom.setRoomCount(roomDTO.getRoomCount());
        existingRoom.setPricePerNight(roomDTO.getPricePerNight());

        // Upload ảnh phòng nếu có
        if (roomImage1 != null && !roomImage1.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(roomImage1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingRoom.setImg(uploadResult.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        if (roomImage2 != null && !roomImage2.isEmpty()) {
            try {
                Map uploadResult2 = cloudinary.uploader().upload(roomImage2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingRoom.setImg2(uploadResult2.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        if (roomImage3 != null && !roomImage3.isEmpty()) {
            try {
                Map uploadResult3 = cloudinary.uploader().upload(roomImage3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                existingRoom.setImg3(uploadResult3.get("url").toString());
            } catch (IOException e) {
                log.error("Room image upload failed: {}", e.getMessage());
                throw new RuntimeException("Room image upload failed", e);
            }
        }

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Successfully updated room with ID: {}", existingRoom.getId());
        return updatedRoom;
    }

    @Override
    public void deleteRoom(Long id) {

    }

    @Override
    public Room mapRoomDtoToRoom(RoomDTO roomDTO, Hotel hotel) {
        log.debug("Mapping RoomDTO to Room: {}", roomDTO);
        Room room = Room.builder()
                .hotel(hotel)
                .roomType(roomDTO.getRoomType())
                .img(roomDTO.getImg())
                .img2(roomDTO.getImg2())
                .img3(roomDTO.getImg3())
                .roomCount(roomDTO.getRoomCount())
                .pricePerNight(roomDTO.getPricePerNight())
                .build();
        log.debug("Mapped Room: {}", room);
        return room;

    }

    @Override
    public RoomDTO mapRoomToRoomDto(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .roomType(room.getRoomType())
                .img(room.getImg())
                .img2(room.getImg2())
                .img3(room.getImg3())
                .roomCount(room.getRoomCount())
                .pricePerNight(room.getPricePerNight())
                .build();
    }
}
