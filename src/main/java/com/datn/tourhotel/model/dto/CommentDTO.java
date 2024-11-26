package com.datn.tourhotel.model.dto;

import java.time.LocalDateTime;

import com.datn.tourhotel.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private Long id;
    private String name; // Hiển thị tên người dùng
    private String username; // Hiển thị tên người dùng
    private String img;
    private String roomType;
    private String content;
    private int rating;
    private LocalDateTime createdDate;
    private String hotelName;
    private boolean isDelete;
}


