package com.datn.tourhotel.service.impl;

import com.datn.tourhotel.model.Voucher;
import com.datn.tourhotel.model.dto.VoucherDTO;
import com.datn.tourhotel.repository.VoucherRepository;
import com.datn.tourhotel.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImplement implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;
;
    @Override
    public Voucher saveVoucher(VoucherDTO voucherDTO) {
        // Chuyển đổi DTO thành entity
        Voucher voucher = toEntity(voucherDTO);

        // Lưu vào database
        return voucherRepository.save(voucher);
    }

    // Phương thức chuyển đổi từ DTO sang entity
    private Voucher toEntity(VoucherDTO voucherDTO) {
        Voucher voucher = new Voucher();
        voucher.setCode(voucherDTO.getCode());
        voucher.setName(voucherDTO.getName());
        voucher.setDiscount(voucherDTO.getDiscount());
        voucher.setMaxUsageLimit(voucherDTO.getMaxUsageLimit());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setIsDelete(false); // Mặc định là chưa bị xóa
        voucher.setCreatedBy(voucherDTO.getCreatedBy());
        return voucher;
    }

    @Override
    public void deleteVoucher(Long id) {
        // Tìm kiếm voucher theo ID
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher with ID " + id + " not found"));

        // Đặt isDelete thành true
        voucher.setIsDelete(true);

        // Lưu thay đổi
        voucherRepository.save(voucher);
    }

    @Override
    public void updateVoucher(Long id, VoucherDTO voucherDTO) {
        // Tìm kiếm voucher theo ID
        Voucher existingVoucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher with ID " + id + " not found"));

        // Cập nhật các trường từ DTO vào đối tượng Voucher
        existingVoucher.setCode(voucherDTO.getCode());
        existingVoucher.setName(voucherDTO.getName());
        existingVoucher.setDiscount(voucherDTO.getDiscount());
        existingVoucher.setMaxUsageLimit(voucherDTO.getMaxUsageLimit());
        existingVoucher.setStartDate(voucherDTO.getStartDate());
        existingVoucher.setEndDate(voucherDTO.getEndDate());
        existingVoucher.setDescription(voucherDTO.getDescription());
        

        // Lưu đối tượng đã cập nhật
        voucherRepository.save(existingVoucher);
    }

    @Override
    public List<VoucherDTO> getVouchers() {
        List<Voucher> vouchers = voucherRepository.findAllActiveVouchers(); // Chỉ lấy các voucher chưa bị xóa
        return vouchers.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Voucher getVoucher(Long id) {
        return voucherRepository.findById(id).orElse(null);
    }

    // Phương thức chuyển đổi từ model Voucher sang DTO
    public VoucherDTO toDto(Voucher voucher) {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setId(voucher.getId());
        voucherDTO.setCode(voucher.getCode());
        voucherDTO.setName(voucher.getName());
        voucherDTO.setDiscount(voucher.getDiscount());
        voucherDTO.setCreateDate(voucher.getCreateDate());
        voucherDTO.setMaxUsageLimit(voucher.getMaxUsageLimit());
        voucherDTO.setStartDate(voucher.getStartDate());
        voucherDTO.setEndDate(voucher.getEndDate());
        voucherDTO.setDescription(voucher.getDescription());
        voucherDTO.setCreatedBy(voucher.getCreatedBy());
        return voucherDTO;
    }

    @Override
    public VoucherDTO findVoucherById(Long id) {
        // Tìm kiếm voucher theo ID
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher with ID " + id + " not found"));

        // Chuyển đổi Voucher thành VoucherDTO và trả về
        return toDto(voucher);
    }

    @Override
    public Optional<VoucherDTO> findVoucherByCode(String code) {
        return voucherRepository.findByCodeAndIsDeleteFalse(code)
                .map(this::toDto);
    }
    @Override
    public List<VoucherDTO> getVouchersForUser(String username) {
        List<Voucher> vouchers = voucherRepository.findActiveVouchersByUser(username);
        return vouchers.stream()
                       .map(this::toDto)
                       .toList();
    }
    

    public double calculateDiscount(String voucherCode, double totalPrice) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCodeAndIsDeleteFalse(voucherCode);
        if (optionalVoucher.isEmpty()) {
            throw new IllegalArgumentException("Voucher does not exist or has expired.");
        }

        Voucher voucher = optionalVoucher.get();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Voucher is not valid at this time.");
        }

        if (voucher.getMaxUsageLimit() <= 0) {
            throw new IllegalArgumentException("Vouchers are out of stock.");
        }

        double discount = voucher.getDiscount();

        // Trừ số lượng voucher
        voucher.setMaxUsageLimit(voucher.getMaxUsageLimit() - 1);
        voucherRepository.save(voucher); // Cập nhật số lượng trong database

        // Tính toán giảm giá
        if (discount > 0 && discount <= 100) {
            return discount / 100.0; // Tỷ lệ phần trăm
        }

        return discount / totalPrice; // Số tiền giảm cố định
    }


}
