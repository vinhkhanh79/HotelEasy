package com.datn.tourhotel.service;

import com.datn.tourhotel.model.Comment;
import com.datn.tourhotel.model.dto.CommentDTO;
import com.datn.tourhotel.model.dto.UserDTO;

import java.util.List;

public interface CommentService {
	 List<CommentDTO> getCommentsByHotelId(Long hotelId);
     void addComment(Long hotelId, Long userId, String content, int rating);
     double getAverageRatingByHotelId(Long hotelId);
     List<CommentDTO> findAllCommentList();
     void deleteComment(Long id);
     List<CommentDTO> findCommentsByHotel(Long hotelId);
}
