package com.datn.tourhotel.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelRegistrationDTO {

    @NotBlank(message = "Hotel name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z0-9 ]+$", message = "Hotel name must only contain letters and numbers")
    private String name;
    
    @NotBlank(message = "Hotel describe cannot be empty")
    private String describe;

    @Valid
    private AddressDTO addressDTO;
    
    private String img;
    
    private String img2;
    
    private String img3;

    @Valid
    private List<RoomDTO> roomDTOs = new ArrayList<>();

}
