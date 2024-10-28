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
}
