package com.datn.tourhotel.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordForgotDTO {
	
	@NotBlank(message = "Email is required")
	private String email;
}
