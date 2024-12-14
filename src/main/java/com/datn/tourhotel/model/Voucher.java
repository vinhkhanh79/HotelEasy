package com.datn.tourhotel.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // Mã voucher

    @Column(nullable = false)
    private String name; // Tên voucher

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createDate; // Ngày tạo

    @Column(nullable = false)
    private Boolean isDelete; // Trạng thái (ACTIVE, INACTIVE)

    @Column(nullable = false)
    private Double discount; // Giá trị giảm giá (số tiền hoặc phần trăm)

    @Column(nullable = true)
    private Integer maxUsageLimit; // Giới hạn số lượng vé phát hành

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime startDate; // Thời gian bắt đầu áp dụng

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime endDate; // Thời gian kết thúc áp dụng

    @Column(nullable = true)
    private String description; // Chi tiết voucher
    
    @Column(nullable = true)
    private String createdBy; // Người tạo voucher

    // Loại bỏ liên kết với VoucherDetail
    @Override
    public String toString() {
        return "Voucher{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", createDate=" + createDate +
                ", isDelete='" + isDelete + '\'' +
                ", discount=" + discount +
                ", maxUsageLimit=" + maxUsageLimit +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                '}';
    }
}
