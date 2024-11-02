package com.datn.tourhotel.model.dto;

import java.time.LocalDate;

import com.datn.tourhotel.model.enums.RoleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    private String img;
    
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?(\\d{10})$", message = "Invalid phone number")
    private String phone;
    
    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private RoleType roleType;

    // Constructor cho việc tạo mới
    public UserRegistrationDTO(String email, String username, String password, String confirmPassword,
                             String name, String lastName, String phone, LocalDate birthday, RoleType roleType) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.birthday = birthday;
        this.roleType = roleType;
        this.img = "http://res.cloudinary.com/dliwvet1v/image/upload/v1730037767/b2f64154-56a7-49cb-8da1-638ca334c90e.jpg"; // Default image
    }
}
