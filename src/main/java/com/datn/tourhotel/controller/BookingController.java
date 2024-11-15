package com.datn.tourhotel.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.tourhotel.model.Customer;
import com.datn.tourhotel.model.dto.*;
import com.datn.tourhotel.model.enums.BookingStatus;
import com.datn.tourhotel.model.enums.PaymentStatus;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.CustomerService;
import com.datn.tourhotel.service.EmailService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.UserService;
import com.datn.tourhotel.service.VNPayService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;


@org.springframework.stereotype.Controller

@RequiredArgsConstructor
@Slf4j
public class BookingController {

    @Autowired
    private MessageSource messageSource;
    private final HotelService hotelService;
    private final UserService userService;
    private final CustomerService customerService;
    private final BookingService bookingService;
    private final VNPayService vnPayService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/booking/initiate")
    public String initiateBooking(@ModelAttribute BookingInitiationDTO bookingInitiationDTO, HttpSession session) {
        session.setAttribute("bookingInitiationDTO", bookingInitiationDTO);
        log.debug("BookingInitiationDTO set in session: {}", bookingInitiationDTO);
        return "redirect:/booking/payment";
    }

    @GetMapping("/booking/payment")
    public String showPaymentPage(Model model, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        BookingInitiationDTO bookingInitiationDTO = (BookingInitiationDTO) session.getAttribute("bookingInitiationDTO");
        log.debug("BookingInitiationDTO retrieved from session: {}", bookingInitiationDTO);

        if (bookingInitiationDTO == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your session has expired. Please start a new search.");
            return "redirect:/index";
        }
       
        BigDecimal totalPrice = bookingInitiationDTO.getTotalPrice();
        String formattedTotalPrice = totalPrice.stripTrailingZeros().toPlainString();
        model.addAttribute("totalPrice", formattedTotalPrice);

        HotelDTO hotelDTO = hotelService.findHotelDtoById(bookingInitiationDTO.getHotelId());

        model.addAttribute("bookingInitiationDTO", bookingInitiationDTO);
        model.addAttribute("hotelDTO", hotelDTO);
        model.addAttribute("paymentCardDTO", new PaymentCardDTO());

        return "booking/payment";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(@RequestParam("amount") BigDecimal orderTotal,
                              @RequestParam("orderInfo") String orderInfo,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();
        BookingInitiationDTO bookingInitiationDTO = (BookingInitiationDTO) session.getAttribute("bookingInitiationDTO");
        UserDTO userDTO = userService.findUserById(getLoggedInUserId());
        HotelDTO hotelDTO = hotelService.findHotelDtoById(bookingInitiationDTO.getHotelId());
        Long userId = (Long) session.getAttribute("userId");
        
        // Tìm user hiện tại
        Optional<Customer> customer = customerService.findByUserId(getLoggedInUserId());

        if (customer.isPresent()) {
            Long roleId = customer.get().getUser().getRole().getId();
            if (roleId == 1 || roleId == 3) {
                redirectAttributes.addFlashAttribute("errorMessage", "Manager and admin don't have permission to add booking.");
                return "redirect:/booking/payment";
            }
        } else {
            // Thêm chi tiết lỗi tại đây
            String errorMessage = "User not found or unauthorized. Possible reasons: ";
            
            // Kiểm tra thêm cho OAuth2 user (Google, Facebook)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) auth;
                String email = (String) oauth2Token.getPrincipal().getAttributes().get("email");
                errorMessage += "OAuth2 user with email: " + email + " not found in the system.";
            } else {
                errorMessage += "Regular user with username: " + auth.getName() + " not found.";
            }

            // Log chi tiết lỗi để debug dễ hơn
            log.error(errorMessage);

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/booking/payment";
        }

        BookingDTO bookingDTO = bookingService.confirmBooking(bookingInitiationDTO, getLoggedInUserId());
        redirectAttributes.addFlashAttribute("bookingDTO", bookingDTO);

        session.setAttribute("bookingDTO", bookingDTO);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);

        session.setAttribute("orderInfo", orderInfo);
        session.setAttribute("amount", orderTotal);

        return "redirect:" + vnpayUrl;
    }


    @GetMapping("/vnpay-payment")
    public String paymentReturn(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        
     // Lấy BookingDTO từ phiên
        HttpSession session = request.getSession();
        BookingDTO bookingDTO = (BookingDTO) session.getAttribute("bookingDTO");

        // If payment is successful
        if (paymentStatus == 1) {
        	bookingService.updateBookingStatus(bookingDTO.getId(), BookingStatus.COMPLETED);
        	bookingService.updatePaymentStatus(bookingDTO.getId(), PaymentStatus.COMPLETED);
        	redirectAttributes.addFlashAttribute("orderId", orderInfo);
            redirectAttributes.addFlashAttribute("totalPrice", totalPrice);
            redirectAttributes.addFlashAttribute("paymentTime", paymentTime);
            redirectAttributes.addFlashAttribute("transactionId", transactionId);
            // Handle booking confirmation
            bookingService.confirmBookingFromPayment(orderInfo);
            redirectAttributes.addFlashAttribute("bookingDTO", bookingDTO);
         // Chuyển đổi totalPrice thành số nguyên (giả sử totalPrice hiện tại là số với đơn vị VND)
            long totalPriceLong = Long.parseLong(totalPrice) / 100; // Giả sử giá trị trả về là số nhân với 100 (do VNPAY thường trả về như vậy)

            // Format theo định dạng tiền tệ của Việt Nam
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedTotalPrice = currencyFormat.format(totalPriceLong);

            String subject = "Booking Confirmation - Thank you for booking with us!";
            String emailContent = String.format(
                    "<!DOCTYPE html>\n"
                            + "<html lang=\"en\">\n"
                            + "<head>\n"
                            + "    <meta charset=\"UTF-8\">\n"
                            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                            + "    <title>Confirm Your Booking</title>\n"
                            + "    <style>\n"
                            + "        body {\n"
                            + "            font-family: Arial, sans-serif;\n"
                            + "            background-color: #f4f4f4;\n"
                            + "            margin: 0;\n"
                            + "            padding: 0;\n"
                            + "        }\n"
                            + "        .container {\n"
                            + "            max-width: 600px;\n"
                            + "            margin: 0 auto;\n"
                            + "            background-color: #ffffff;\n"
                            + "            padding: 20px;\n"
                            + "            border-radius: 8px;\n"
                            + "            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n"
                            + "        }\n"
                            + "        .header {\n"
                            + "            text-align: center;\n"
                            + "            padding: 10px;\n"
                            + "        }\n"
                            + "        .header img {\n"
                            + "            width: 120px;\n"
                            + "        }\n"
                            + "        .content {\n"
                            + "            text-align: center;\n"
                            + "            padding: 20px;\n"
                            + "        }\n"
                            + "        .content h1 {\n"
                            + "            color: #333;\n"
                            + "            font-size: 24px;\n"
                            + "            margin-bottom: 10px;\n"
                            + "        }\n"
                            + "        .content p {\n"
                            + "            color: #666;\n"
                            + "            font-size: 16px;\n"
                            + "            margin-bottom: 20px;\n"
                            + "        }\n"
                            + "        .button {\n"
                            + "            text-align: center;\n"
                            + "        }\n"
                            + "        .button a {\n"
                            + "            background-color: #4a90e2;\n"
                            + "            color: white;\n"
                            + "            text-decoration: none;\n"
                            + "            padding: 12px 24px;\n"
                            + "            border-radius: 4px;\n"
                            + "            font-size: 16px;\n"
                            + "            display: inline-block;\n"
                            + "        }\n"
                            + "        .footer {\n"
                            + "            text-align: center;\n"
                            + "            padding: 20px;\n"
                            + "            color: #999;\n"
                            + "            font-size: 14px;\n"
                            + "        }\n"
                            + "        .footer a {\n"
                            + "            color: #4a90e2;\n"
                            + "            text-decoration: none;\n"
                            + "        }\n"
                            + "    </style>\n"
                            + "</head>\n"
                            + "<body>\n"
                            + "    <div class=\"container\">\n"
                            + "        <div class=\"header\">\n"
                            + "            <img src=\"https://res.cloudinary.com/dliwvet1v/image/upload/v1729759384/i7xwpwwwsmwikzk5v8hm.png\" alt=\"Company Logo\">\n"
                            + "        </div>\n"
                            + "        <div class=\"content\">\n"
                            + "            <h1>Confirm Your Booking</h1>\n"
                            + "            <p>We are thrilled to inform you that your booking has been confirmed!</p>\n"
                            + "            <p>Order ID: %s<br>\n"
                            + "               Total Price: %s VND<br>\n"
                            + "               Payment Time: %s<br>\n"
                            + "               Transaction ID: %s<br>\n"
                            + "               Booking Information:<br>\n"
                            + "               Customer Name: %s<br>\n"
                            + "               Check-in Date: %s<br>\n"
                            + "               Check-out Date: %s<br>\n"
                            + "            </p>\n"
                            + "            <p>If you have any questions or need further assistance, feel free to contact us.</p>\n"
                            + "            <p>Thank you for choosing our services! We look forward to welcoming you.</p>\n"
                            + "            <p>Best regards,<br>The Hotel Booking Team</p>\n"
                            + "        </div>\n"
                            + "        <div class=\"footer\">\n"
                            + "            <p>Style Casual © 2021 Style Casual, Inc. All Rights Reserved.</p>\n"
                            + "            <p>4562 Hazy Panda Limits, Chair Crossing, Kentucky, US, 607898</p>\n"
                            + "            <p><a href=\"#\">Visit Us</a> | <a href=\"#\">Privacy Policy</a> | <a href=\"#\">Terms of Use</a></p>\n"
                            + "        </div>\n"
                            + "    </div>\n"
                            + "</body>\n"
                            + "</html>",
                    orderInfo, // Thông tin đơn hàng
                    formattedTotalPrice, // Tổng số tiền
                    paymentTime, // Thời gian thanh toán
                    transactionId, // Mã giao dịch
                    bookingDTO.getCustomerName(), // Tên khách hàng
                    bookingDTO.getCheckinDate(), // Ngày check-in
                    bookingDTO.getCheckoutDate() // Ngày check-out
            );

            
            emailService.sendBookingConfirmation(bookingDTO.getCustomerEmail(), subject, emailContent);
            return "redirect:/booking/confirmation"; // Redirect to confirmation page
        } else {
        	bookingService.updateBookingStatus(bookingDTO.getId(), BookingStatus.PENDING);
        	bookingService.updatePaymentStatus(bookingDTO.getId(), PaymentStatus.FAILED);
        	redirectAttributes.addFlashAttribute("errorMessage", "Payment failed. Please try again.");
            model.addAttribute("orderId", orderInfo);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("paymentTime", paymentTime);
            model.addAttribute("transactionId", transactionId);
            return "/booking/orderfail";
        }
    }

    @GetMapping("/booking/confirmation")
    public String showConfirmationPage(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        BookingDTO bookingDTO = (BookingDTO) model.asMap().get("bookingDTO");

        if (bookingDTO == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your session has expired or the booking process was not completed properly. Please start a new search.");
            return "redirect:/index";
        }
        String orderId = (String) model.asMap().get("orderId");
        String totalPrice = (String) model.asMap().get("totalPrice");
        String paymentTime = (String) model.asMap().get("paymentTime");
        String transactionId = (String) model.asMap().get("transactionId");

        LocalDate checkinDate = bookingDTO.getCheckinDate();
        LocalDate checkoutDate = bookingDTO.getCheckoutDate();
        long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);

        model.addAttribute("days", durationDays);
        model.addAttribute("bookingDTO", bookingDTO);
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return "booking/confirmation";
    }

    private Long getLoggedInUserId() {
    	String username = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) auth;
            username = (String) oauth2Token.getPrincipal().getAttributes().get("email");
        }else{
            username = auth.getName();
        }        UserDTO userDTO = userService.findUserDTOByUsername(username);
        log.info("Fetched logged in user ID: {}", userDTO.getId());
        return userDTO.getId();
    }
}
