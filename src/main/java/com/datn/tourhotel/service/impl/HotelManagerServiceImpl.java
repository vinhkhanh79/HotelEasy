package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.datn.tourhotel.model.HotelManager;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.repository.HotelManagerRepository;
import com.datn.tourhotel.repository.HotelRepository;
import com.datn.tourhotel.service.HotelManagerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelManagerServiceImpl implements HotelManagerService {

    private final HotelManagerRepository hotelManagerRepository;
    private final HotelRepository hotelRepository;

    @Override
    public HotelManager findByUser(User user) {
        log.info("Attempting to find HotelManager for user ID: {}", user.getId());
        return hotelManagerRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("HotelManager not found for user " + user.getUsername()));
    }
    
    @Override
    public long countHotelManagers() {
        log.info("Counting total HotelManagers");
        return hotelManagerRepository.count();
    }
    
    @Override
    public long countHotels() {
        log.info("Counting total Hotels");
        return hotelRepository.count();
    }
    
    @Override
    public long countHotelsByManager(Long managerId) {
        log.info("Counting total Hotels by manager");
        return hotelRepository.countHotelsByManagerId(managerId);
    }
}
