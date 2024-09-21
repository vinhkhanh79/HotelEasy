package com.datn.tourhotel.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDTO {
	
	@NotBlank(message = "Current password is required")
	private String currentPassword;
	
	@NotBlank(message = "New password is required")
	@Size(min = 6, message = "New password must be at least 6 characters long")
	private String newPassword;
	
	@NotBlank(message = "confirm new password is required")
	private String confirmNewPassword;
	
	public boolean isPasswordsMatching() {
		return newPassword.equals(confirmNewPassword);
	}
}
