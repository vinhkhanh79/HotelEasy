package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.HotelManager;
import com.datn.tourhotel.model.User;

import java.util.Optional;

@Repository
public interface HotelManagerRepository extends JpaRepository<HotelManager, Long> {

    Optional<HotelManager> findByUser(User user);

    @Query("SELECT COUNT(c) FROM HotelManager c")
    Long countHotelManagers();  // đổi tên phương thức
}
