package com.datn.tourhotel.repository;

import com.datn.tourhotel.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByHotelIdOrderByCreatedDateDesc(Long hotelId);
	boolean existsByUserIdAndHotelId(Long userId, Long hotelId);
	List<Comment> findByHotelId(Long hotelId);
}
