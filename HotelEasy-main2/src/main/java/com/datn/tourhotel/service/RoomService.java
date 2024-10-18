package com.datn.tourhotel.service;

import java.util.List;
import java.util.Optional;

import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.Room;
import com.datn.tourhotel.model.dto.RoomDTO;

public interface RoomService {

    Room saveRoom(RoomDTO roomDTO, Hotel hotel);

    List<Room> saveRooms(List<RoomDTO> roomDTOs, Hotel hotel);

    Optional<Room> findRoomById(Long id);

    List<Room> findRoomsByHotelId(Long hotelId);

    Room updateRoom(RoomDTO roomDTO);

    void deleteRoom(Long id);

    Room mapRoomDtoToRoom(RoomDTO roomDTO, Hotel hotel);

    RoomDTO mapRoomToRoomDto(Room room);

}