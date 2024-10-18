package com.datn.tourhotel.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class HotelDTO {

    private Long id;

    @NotBlank(message = "Hotel name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z0-9 ]+$", message = "Hotel name must only contain letters and numbers")
    private String name;
    
    @NotBlank(message = "Hotel describe cannot be empty")
    private String describe;
    
    private String img;
    
    private String img2;
    
    private String img3;

    @Valid
    private AddressDTO addressDTO;

    @Valid
    private List<RoomDTO> roomDTOs = new ArrayList<>();

    private String managerUsername;

}
