package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
