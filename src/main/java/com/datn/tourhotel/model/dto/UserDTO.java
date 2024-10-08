package com.datn.tourhotel.model.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.datn.tourhotel.model.Role;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class UserDTO {

    private Long id;
    
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 6, max = 20, message = "Username must be between 6 to 20 characters")
    private String username;

    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z ]+$", message = "Name must only contain letters")
    private String name;

    @NotBlank(message = "Last name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z ]+$", message = "Last name must only contain letters")
    private String lastName;
    
    private String img;
    
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?(\\d{10})$", message = "Invalid phone number")
    private String phone; 
    
    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be in the past")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthday; 

    private Role role;

}
