package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
