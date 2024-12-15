package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Hotel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
	
	List<Hotel> findAllByIsDeleteFalse();

    // Find hotel by name, ensuring it's not deleted
    Optional<Hotel> findByNameAndIsDeleteFalse(String name);

    // Find all hotels by hotel manager ID, ensuring they are not deleted
    List<Hotel> findAllByHotelManager_IdAndIsDeleteFalse(Long id);

    // Find hotel by ID and hotel manager ID, ensuring it's not deleted
    Optional<Hotel> findByIdAndHotelManager_IdAndIsDeleteFalse(Long id, Long managerId);
    
    // Count hotels by manager ID, ensuring they are not deleted
    @Query("SELECT COUNT(h) FROM Hotel h WHERE h.hotelManager.id = :managerId AND h.isDelete = false")
    Long countHotelsByManagerId(@Param("managerId") Long managerId);

    // Find hotels by address line, ensuring they are not deleted
    @Query("SELECT h FROM Hotel h WHERE h.address.addressLine = :addressLine AND h.isDelete = false")
    List<Hotel> findHotelsByAddressLine(@Param("addressLine") String addressLine);

    // Find hotels with available rooms, ensuring they are not deleted
    @Query("SELECT h " +
            "FROM Hotel h " +
            "JOIN h.rooms r " +
            "LEFT JOIN Availability a ON a.room.id = r.id " +
            "AND a.date >= :checkinDate AND a.date < :checkoutDate " +
            "WHERE h.address.addressLine = :addressLine " +
            "AND (a IS NULL OR a.availableRooms > 0) " +
            "AND h.isDelete = false " +
            "GROUP BY h.id, h.name, h.address, h.hotelManager, h.img, h.img2, h.img3, h.describe, h.isDelete " +
            "HAVING COUNT(DISTINCT a.date) + SUM(CASE WHEN a IS NULL THEN 1 ELSE 0 END) = :numberOfDays")
    List<Hotel> findHotelsWithAvailableRooms(@Param("addressLine") String addressLine,
                                             @Param("checkinDate") LocalDate checkinDate,
                                             @Param("checkoutDate") LocalDate checkoutDate,
                                             @Param("numberOfDays") Long numberOfDays);

    // Find hotels without availability records, ensuring they are not deleted
    @Query("SELECT h " +
            "FROM Hotel h " +
            "WHERE h.address.addressLine = :addressLine " +
            "AND NOT EXISTS (" +
            "   SELECT 1 " +
            "   FROM Availability a " +
            "   WHERE a.room.hotel.id = h.id " +
            "   AND a.date >= :checkinDate AND a.date < :checkoutDate" +
            ") " +
            "AND h.isDelete = false")
    List<Hotel> findHotelsWithoutAvailabilityRecords(@Param("addressLine") String addressLine,
                                                     @Param("checkinDate") LocalDate checkinDate,
                                                     @Param("checkoutDate") LocalDate checkoutDate);

    // Find hotels with partial availability records, ensuring they are not deleted
    @Query("SELECT h " +
            "FROM Hotel h " +
            "JOIN h.rooms r " +
            "LEFT JOIN Availability a ON r.id = a.room.id " +
            "AND a.date >= :checkinDate AND a.date < :checkoutDate " +
            "WHERE h.address.addressLine = :addressLine " +
            "AND (a IS NULL OR a.availableRooms > 0) " +
            "AND h.isDelete = false " +
            "GROUP BY h.id, h.name, h.address, h.hotelManager, h.img, h.img2, h.img3, h.describe, h.isDelete " +
            "HAVING COUNT(DISTINCT a.date) < :numberOfDays " +
            "AND COUNT(DISTINCT CASE WHEN a.availableRooms > 0 THEN a.date END) > 0")
    List<Hotel> findHotelsWithPartialAvailabilityRecords(@Param("addressLine") String addressLine,
                                                         @Param("checkinDate") LocalDate checkinDate,
                                                         @Param("checkoutDate") LocalDate checkoutDate,
                                                         @Param("numberOfDays") Long numberOfDays);

}
