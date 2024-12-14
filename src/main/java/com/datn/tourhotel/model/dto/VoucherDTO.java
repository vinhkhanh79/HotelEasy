package com.datn.tourhotel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherDTO {
    private Long id; // ID của voucher
    private String code; // Mã voucher
    private String name; // Tên voucher
    private Boolean isDelete;
    private Double discount; // Giá trị giảm giá (phần trăm hoặc số tiền)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createDate; // Ngày tạo
    private Integer maxUsageLimit; // Giới hạn số lượng vé phát hành
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate; // Thời gian bắt đầu áp dụng
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate; // Thời gian kết thúc áp dụng
    private String description; // Mô tả chi tiết voucher
    private String createdBy; // Lưu tên người dùng đã tạo voucher

}
