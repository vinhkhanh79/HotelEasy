package com.datn.tourhotel.repository;

import com.datn.tourhotel.model.Comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByHotelIdOrderByCreatedDateDesc(Long hotelId);
	boolean existsByUserIdAndHotelId(Long userId, Long hotelId);
	List<Comment> findByHotelId(Long hotelId);
	@Query("SELECT c.hotel.name, COUNT(c) AS fiveStarCount FROM Comment c " +
		       "WHERE c.rating = 5 AND c.isDelete = false " +
		       "GROUP BY c.hotel.id, c.hotel.name " +
		       "ORDER BY fiveStarCount DESC")
		List<Object[]> findTopHotelsWithFiveStarRatings(Pageable pageable);
		@Query("SELECT c.hotel.name, COUNT(c) AS fiveStarCount " +
			       "FROM Comment c " +
			       "JOIN c.hotel h " +
			       "WHERE c.rating = 5 AND c.isDelete = false " +
			       "AND h.hotelManager.id = :managerId " +  // Corrected this line
			       "GROUP BY c.hotel.id, c.hotel.name " +
			       "ORDER BY fiveStarCount DESC")
			List<Object[]> findTopHotelsWithFiveStarRatingsByManager(@Param("managerId") Long managerId, Pageable pageable);



}
