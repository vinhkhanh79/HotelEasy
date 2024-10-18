package com.datn.tourhotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    @Query("SELECT c FROM Customer c WHERE c.user.username = :username")
    Optional<Customer> findByUsername(@Param("username") String username);

}