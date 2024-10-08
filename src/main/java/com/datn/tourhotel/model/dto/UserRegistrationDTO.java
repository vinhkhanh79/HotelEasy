package com.datn.tourhotel.model.dto;

import java.time.LocalDate;

import com.datn.tourhotel.model.enums.RoleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationDTO {
	@NotBlank(message = "Email address cannot be empty")
    @Email(message = "Invalid email address")
    private String email;
	
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 6, max = 20, message = "Username must be between 6 to 20 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 20, message = "Password must be between 6 to 20 characters")
    private String password;
    
    @NotBlank(message = "Confirm password cannot be empty")
    @Size(min = 6, max = 20, message = "Confirm password must be between 6 to 20 characters")
    private String confirmPassword;

    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z ]+$", message = "Name must only contain letters")
    private String name;

    @NotBlank(message = "Last name cannot be empty")
    @Pattern(regexp = "^(?!\\s*$)[A-Za-z ]+$", message = "Last name must only contain letters")
    private String lastName;
    
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?(\\d{10})$", message = "Invalid phone number")
    private String phone; 
    
    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday; 

    private RoleType roleType;

}
