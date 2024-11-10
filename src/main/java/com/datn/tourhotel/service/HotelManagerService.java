package com.datn.tourhotel.service;

import com.datn.tourhotel.model.HotelManager;
import com.datn.tourhotel.model.User;

public interface HotelManagerService {

    HotelManager findByUser(User user);
    
    long countHotelManagers();
    
    long countHotels();
    
    long countHotelsByManager(Long managerId);
}
