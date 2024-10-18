package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
