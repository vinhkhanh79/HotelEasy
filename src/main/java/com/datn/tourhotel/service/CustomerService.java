package com.datn.tourhotel.service;

import java.util.Optional;

import com.datn.tourhotel.model.Customer;

public interface CustomerService {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByUsername(String username);
    
    Long getCustomerCount();
}
