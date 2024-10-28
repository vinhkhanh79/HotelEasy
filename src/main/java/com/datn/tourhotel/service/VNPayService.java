package com.datn.tourhotel.service;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
	 String createOrder(BigDecimal total, String orderInfor, String baseUrl);
	 int orderReturn(HttpServletRequest request);

}
