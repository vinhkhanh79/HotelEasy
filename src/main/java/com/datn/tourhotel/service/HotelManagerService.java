package com.datn.tourhotel.service;

import java.util.List;
import java.util.Optional;

import com.datn.tourhotel.model.HotelManager;
import com.datn.tourhotel.model.User;

public interface HotelManagerService {

    HotelManager findByUser(User user);
    
    long countHotelManagers();
    
    long countHotels();
    
    long countHotelsByManager(Long managerId);
    
    Optional<HotelManager> findById(Long id);
    
    List<HotelManager> findAll();
}
