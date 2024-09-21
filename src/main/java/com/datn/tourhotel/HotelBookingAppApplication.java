package com.datn.tourhotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

@SpringBootApplication
public class HotelBookingAppApplication {

    public static void main(String[] args) {SpringApplication.run(HotelBookingAppApplication.class, args);
    }
    @Bean
    public Cloudinary cloudinary() {
			Cloudinary c = new Cloudinary(ObjectUtils.asMap(
					"cloud_name", "dliwvet1v",
					"api_key", "562754662323418",
					"api_secret", "undefined",
					"secure", true));
			return c;
    }
}
