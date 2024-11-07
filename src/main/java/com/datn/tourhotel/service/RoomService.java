package com.datn.tourhotel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.Room;
import com.datn.tourhotel.model.dto.RoomDTO;

public interface RoomService {

    Room saveRoom(RoomDTO roomDTO, Hotel hotel, MultipartFile roomImage1, MultipartFile roomImage2, MultipartFile roomImage3);

    List<Room> saveRooms(List<RoomDTO> roomDTOs, Hotel hotel);

    Optional<Room> findRoomById(Long id);

    List<Room> findRoomsByHotelId(Long hotelId);

    Room updateRoom(RoomDTO roomDTO, MultipartFile multipartFile, MultipartFile multipartFile2, MultipartFile multipartFile3);

    void deleteRoom(Long id);

    Room mapRoomDtoToRoom(RoomDTO roomDTO, Hotel hotel);

    RoomDTO mapRoomToRoomDto(Room room);

}
