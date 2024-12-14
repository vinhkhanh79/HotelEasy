package com.datn.tourhotel.repository;

import com.datn.tourhotel.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // Lấy danh sách tất cả voucher chưa bị xóa
    @Query("SELECT v FROM Voucher v WHERE v.isDelete = false")
    List<Voucher> findAllActiveVouchers();

    // Tìm voucher bằng mã code (không phân biệt trạng thái xóa)
    Voucher findVoucherByCode(String code);

    // Tìm voucher bằng mã code và isDelete = false
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isDelete = false")
    Optional<Voucher> findByCodeAndIsDeleteFalse(@Param("code") String code);
    
    @Query("SELECT v FROM Voucher v WHERE v.isDelete = false AND v.createdBy = :username")
    List<Voucher> findActiveVouchersByUser(@Param("username") String username);
    
    @Modifying
    @Query("UPDATE Voucher v SET v.maxUsageLimit = v.maxUsageLimit - 1 WHERE v.code = :code AND v.isDelete = false AND v.maxUsageLimit > 0")
    int decrementVoucherUsage(@Param("code") String code);

}
