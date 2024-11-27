package com.datn.tourhotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 5000)
    private String title;
    
    @Column(unique = true, nullable = false, length = 5000)
    private String description;
    
    @Column(unique = true, nullable = false, length = 5000)
    private String content;
    
    @Column(unique = true, nullable = true, length = 5000)
    private String content2;
    
    @Column(unique = true, nullable = true, length = 5000)
    private String content3;
    
    @Column(unique = true, nullable = false, length = 5000)
    private String category;
    
    @Column(unique = true, nullable = true)
    private String img;
    
    @Column(unique = true, nullable = true)
    private String img2;
    
    @Column(unique = true, nullable = true)
    private String img3;
    
    @Column(unique = true, nullable = true, length = 5000)
    private String figcaption;
    
    @Column(unique = true, nullable = true, length = 5000)
    private String figcaption2;
    
    @Column(unique = true, nullable = true, length = 5000)
    private String figcaption3;
    
    @Column(unique = true, nullable = true)
    private String location;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private User createdBy;
    
    @CreationTimestamp
    @Column(unique = true, nullable = true)
    private LocalDateTime createdDate;
    
    private boolean isDelete = false;
}
