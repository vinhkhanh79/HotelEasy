package com.datn.tourhotel.service;

import com.datn.tourhotel.model.Voucher;
import com.datn.tourhotel.model.dto.VoucherDTO;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    Voucher saveVoucher(VoucherDTO voucherDTO);
    void deleteVoucher(Long id);
    Voucher getVoucher(Long id);
    List<VoucherDTO> getVouchers();
    void updateVoucher(Long id, VoucherDTO voucherDTO);
    VoucherDTO findVoucherById(Long id);
    Optional<VoucherDTO> findVoucherByCode(String code);
    double calculateDiscount(String voucherCode, double totalPrice);
    List<VoucherDTO> getVouchersForUser(String username);
}
