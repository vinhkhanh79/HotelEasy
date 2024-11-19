package com.datn.tourhotel.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
	// Tổng tiền thanh toán trong một ngày
		@Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE CAST(p.paymentDate AS DATE) = CAST(GETDATE() AS DATE)")
		BigDecimal getTotalEarningsTodayAdmin();
	    
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE WEEK(p.paymentDate) = WEEK(CURRENT_DATE) AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisWeekAdmin();

	    // Tổng tiền thanh toán trong tháng hiện tại
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE MONTH(p.paymentDate) = MONTH(CURRENT_DATE) AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisMonthAdmin();

	    // Tổng tiền thanh toán trong năm hiện tại
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisYearAdmin();

	    // Tổng tiền thanh toán tổng cộng
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p")
	    BigDecimal getTotalEarningsAdmin();
	
		@Query("SELECT DAY(p.paymentDate) AS day, SUM(p.totalPrice) AS total FROM Payment p " +
	 	       "WHERE YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
	 	       "GROUP BY DAY(p.paymentDate) " +
	 	       "ORDER BY DAY(p.paymentDate)")
	 	List<Object[]> getTotalEarningsPerDayInYearAdmin();
	 	
	 	@Query("SELECT MONTH(p.paymentDate) AS month, SUM(p.totalPrice) AS total FROM Payment p " +
	 	       "WHERE YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
	 	       "GROUP BY MONTH(p.paymentDate) " +
	 	       "ORDER BY MONTH(p.paymentDate)")
	 	List<Object[]> getTotalEarningsPerMonthInYearAdmin();
	 	
	 	@Query("SELECT WEEK(p.paymentDate) AS week, SUM(p.totalPrice) AS total FROM Payment p " +
	 	       "WHERE YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
	 	       "GROUP BY WEEK(p.paymentDate) " +
	 	       "ORDER BY WEEK(p.paymentDate)")
	 	List<Object[]> getTotalEarningsPerWeekInYearAdmin();
	 	
		// Tổng tiền thanh toán trong một ngày
		@Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.booking.hotel.hotelManager.id = :managerId AND CAST(p.paymentDate AS DATE) = CAST(GETDATE() AS DATE)")
		BigDecimal getTotalEarningsToday(@Param("managerId") Long managerId);
	    
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.booking.hotel.hotelManager.id = :managerId AND WEEK(p.paymentDate) = WEEK(CURRENT_DATE) AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisWeek(@Param("managerId") Long managerId);
	
	    // Tổng tiền thanh toán trong tháng hiện tại
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.booking.hotel.hotelManager.id = :managerId AND MONTH(p.paymentDate) = MONTH(CURRENT_DATE) AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisMonth(@Param("managerId") Long managerId);
	
	    // Tổng tiền thanh toán trong năm hiện tại
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.booking.hotel.hotelManager.id = :managerId AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE)")
	    BigDecimal getTotalEarningsThisYear(@Param("managerId") Long managerId);
	
	    // Tổng tiền thanh toán tổng cộng
	    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.booking.hotel.hotelManager.id = :managerId")
	    BigDecimal getTotalEarnings(@Param("managerId") Long managerId);
	    
	    @Query("SELECT DAY(p.paymentDate) AS day, SUM(p.totalPrice) AS total FROM Payment p " +
	    	       "WHERE p.booking.hotel.hotelManager.id = :managerId " +
	    	       "AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
	    	       "GROUP BY DAY(p.paymentDate) " +
	    	       "ORDER BY DAY(p.paymentDate)")
	    	List<Object[]> getTotalEarningsPerDayInYear(@Param("managerId") Long managerId);
	    	
    	@Query("SELECT MONTH(p.paymentDate) AS month, SUM(p.totalPrice) AS total FROM Payment p " +
 	 	       "WHERE p.booking.hotel.hotelManager.id = :managerId " +
 	 	       "AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
 	 	       "GROUP BY MONTH(p.paymentDate) " +
 	 	       "ORDER BY MONTH(p.paymentDate)")
 	 	List<Object[]> getTotalEarningsPerMonthInYear(@Param("managerId") Long managerId);
 	 	
 	 	@Query("SELECT WEEK(p.paymentDate) AS week, SUM(p.totalPrice) AS total FROM Payment p " +
 	 		   "WHERE p.booking.hotel.hotelManager.id = :managerId " +
 	 	       "AND YEAR(p.paymentDate) = YEAR(CURRENT_DATE) " +
 	 	       "GROUP BY WEEK(p.paymentDate) " +
 	 	       "ORDER BY WEEK(p.paymentDate)")
 	 	List<Object[]> getTotalEarningsPerWeekInYear(@Param("managerId") Long managerId);
 	 	
 	 	@Query("SELECT p.booking.hotel.name AS hotelName, SUM(p.totalPrice) AS totalEarnings FROM Payment p " +
 	 	       "GROUP BY p.booking.hotel.name " +
 	 	       "ORDER BY totalEarnings DESC")
 	 	List<Object[]> getTopHotelsByEarnings();
 	 	
 	 	@Query("SELECT c.user.name AS customerName, SUM(p.totalPrice) AS totalEarnings " +
 	 	       "FROM Payment p " +
 	 	       "JOIN p.booking b " +
 	 	       "JOIN b.customer c " +
 	 	       "GROUP BY c.user.name " +
 	 	       "ORDER BY totalEarnings DESC")
 	 	List<Object[]> getTopCustomersByEarnings();



}
