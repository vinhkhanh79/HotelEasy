package com.datn.tourhotel.service.impl;

import com.datn.tourhotel.model.Comment;
import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.CommentDTO;
import com.datn.tourhotel.repository.CommentRepository;
import com.datn.tourhotel.repository.HotelRepository;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.service.CommentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    
    @Override
    public List<CommentDTO> getCommentsByHotelId(Long hotelId) {
        List<Comment> comments = commentRepository.findByHotelIdOrderByCreatedDateDesc(hotelId);
        return comments.stream()
        		.filter(comment -> comment.isDelete() == false)
        		.map(comment -> CommentDTO.builder()
                .id(comment.getId())
                .name(comment.getUser().getName())
                .img(comment.getUser().getImg())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdDate(comment.getCreatedDate())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void addComment(Long hotelId, Long userId, String content, int rating) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .hotel(hotel)
                .user(user)
                .content(content)
                .rating(rating)
                .build();

        commentRepository.save(comment);
    }
    @Override
    public double getAverageRatingByHotelId(Long hotelId) {
        List<Comment> comments = commentRepository.findByHotelIdOrderByCreatedDateDesc(hotelId);
        if (comments.isEmpty()) {
            return 0.0; // Return 0 if there are no comments
        }
        double totalRating = comments.stream().mapToInt(Comment::getRating).sum();
        return totalRating / comments.size();
    }
    
    @Override
    public List<CommentDTO> findAllCommentList() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream()
        		.filter(comment -> comment.isDelete() == false)
        		.map(comment -> CommentDTO.builder()
        		.id(comment.getId())
                .name(comment.getUser().getName())
                .username(comment.getUser().getUsername())
                .img(comment.getUser().getImg())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdDate(comment.getCreatedDate())
                .hotelName(comment.getHotel().getName())
                .isDelete(comment.isDelete())
                .build()).toList();
    }
    @Override
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setDelete(true); // Đánh dấu là đã xóa
        commentRepository.save(comment);
    }
    
    @Override
    public List<CommentDTO> findCommentsByHotel(Long hotelId) {
        // Assuming the Comment entity has a hotel field that links to the Hotel entity
        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        return comments.stream()
        		.filter(comment -> comment.isDelete() == false)
        		.map(comment -> CommentDTO.builder()
                .id(comment.getId())
                .name(comment.getUser().getName())
                .username(comment.getUser().getUsername())
                .img(comment.getUser().getImg())
                .content(comment.getContent())
                .rating(comment.getRating())
                .createdDate(comment.getCreatedDate())
                .isDelete(comment.isDelete())
                .build()).toList();
    }


}
