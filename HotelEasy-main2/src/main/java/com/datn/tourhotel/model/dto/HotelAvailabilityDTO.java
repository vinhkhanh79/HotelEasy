package com.datn.tourhotel.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class HotelAvailabilityDTO {

    private Long id;

    private String name;
    
    private String img;
    
    private String img2;
    
    private String img3;
    
    private String describe;

    private AddressDTO addressDTO;

    private List<RoomDTO> roomDTOs = new ArrayList<>();

    private Integer maxAvailableSingleRooms;

    private Integer maxAvailableDoubleRooms;
    
    private Integer maxAvailableSuiteRooms;

}
