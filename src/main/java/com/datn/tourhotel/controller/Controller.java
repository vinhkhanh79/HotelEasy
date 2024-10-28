//package com.datn.tourhotel.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.datn.tourhotel.model.dto.BookingDTO;
//import com.datn.tourhotel.model.dto.BookingInitiationDTO;
//import com.datn.tourhotel.model.dto.UserDTO;
//import com.datn.tourhotel.service.BookingService;
//import com.datn.tourhotel.service.UserService;
//import com.datn.tourhotel.service.VNPayService;
//import java.math.BigDecimal;
//
//@org.springframework.stereotype.Controller
//public class Controller {
//
//    @Autowired
//    private VNPayService vnPayService;
//    @Autowired
//    private BookingService bookingService;
//
//    @GetMapping("/payment")
//    public String home() {
//        return "index";
//    }
//
//    @PostMapping("/submitOrder")
//    public String submitOrder(@RequestParam("amount") BigDecimal orderTotal,
//                              @RequestParam("orderInfo") String orderInfo,
//                              HttpServletRequest request) {
//        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
//        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);
//        return "redirect:" + vnpayUrl;
//    }
//
//    @GetMapping("/vnpay-payment")
//    public String paymentReturn(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
//        int paymentStatus = vnPayService.orderReturn(request);
//
//        String orderInfo = request.getParameter("vnp_OrderInfo");
//        String paymentTime = request.getParameter("vnp_PayDate");
//        String transactionId = request.getParameter("vnp_TransactionNo");
//        String totalPrice = request.getParameter("vnp_Amount");
//
//        model.addAttribute("orderId", orderInfo);
//        model.addAttribute("totalPrice", totalPrice);
//        model.addAttribute("paymentTime", paymentTime);
//        model.addAttribute("transactionId", transactionId);
//        
//        return paymentStatus == 1 ? "/booking/orderfail" : "/booking/ordersuccess";
//    }
//}