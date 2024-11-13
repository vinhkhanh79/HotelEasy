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

import com.datn.tourhotel.model.dto.*;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.EmailService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.UserService;
import com.datn.tourhotel.service.VNPayService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@org.springframework.stereotype.Controller

@RequiredArgsConstructor
@Slf4j
public class BookingController {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private final HotelService hotelService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final BookingService bookingService;
    @Autowired
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
            return "redirect:/search";
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
        	redirectAttributes.addFlashAttribute("errorMessage", "Payment failed. Please try again.");
            model.addAttribute("orderId", orderInfo);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("paymentTime", paymentTime);
            model.addAttribute("transactionId", transactionId);
            return "/booking/orderfail";
        } else {
        	redirectAttributes.addFlashAttribute("orderId", orderInfo);
            redirectAttributes.addFlashAttribute("totalPrice", totalPrice);
            redirectAttributes.addFlashAttribute("paymentTime", paymentTime);
            redirectAttributes.addFlashAttribute("transactionId", transactionId);

            // Handle booking confirmation
            bookingService.confirmBookingFromPayment(orderInfo);
            redirectAttributes.addFlashAttribute("bookingDTO", bookingDTO);
            String subject = "Booking Confirmation - Thank you for booking with us!";
            String emailContent = String.format(
                    "<!DOCTYPE html>\n"
                            + "<html lang=\"en\">\n"
                            + "<head>\n"
                            + "    <meta charset=\"UTF-8\">\n"
                            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                            + "    <title>Booking Confirmation</title>\n"
                            + "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n"
                            + "    <link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css\" rel=\"stylesheet\">\n"
                            + "    <style>\n"
                            + "        body {\n"
                            + "            background-color: #f3f4f6;\n"
                            + "            font-family: 'Helvetica Neue', Arial, sans-serif;\n"
                            + "            color: #333;\n"
                            + "        }\n"
                            + "        .card {\n"
                            + "            border: none;\n"
                            + "            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n"
                            + "            margin-top: 20px;\n"
                            + "        }\n"
                            + "        .card-header {\n"
                            + "            background: linear-gradient(135deg, #3b82f6, #60a5fa);\n"
                            + "            color: white;\n"
                            + "            text-align: center;\n"
                            + "            padding: 30px;\n"
                            + "        }\n"
                            + "        .card-header img {\n"
                            + "            width: 80px;\n"
                            + "            margin-bottom: 15px;\n"
                            + "        }\n"
                            + "        .card-title {\n"
                            + "            font-size: 28px;\n"
                            + "            font-weight: 600;\n"
                            + "            color: #444;\n"
                            + "            margin-bottom: 15px;\n"
                            + "        }\n"
                            + "        .table {\n"
                            + "            font-size: 16px;\n"
                            + "        }\n"
                            + "        .table th {\n"
                            + "            background-color: #10b981;\n"
                            + "            color: white;\n"
                            + "            padding: 14px;\n"
                            + "        }\n"
                            + "        .table td {\n"
                            + "            padding: 14px;\n"
                            + "            color: #555;\n"
                            + "        }\n"
                            + "        .table-icon {\n"
                            + "            color: #10b981;\n"
                            + "            margin-right: 8px;\n"
                            + "        }\n"
                            + "        .btn-primary {\n"
                            + "            background: linear-gradient(135deg, #3b82f6, #60a5fa);\n"
                            + "            border: none;\n"
                            + "            font-size: 18px;\n"
                            + "            padding: 12px 30px;\n"
                            + "            border-radius: 30px;\n"
                            + "        }\n"
                            + "        .btn-primary:hover {\n"
                            + "            background: linear-gradient(135deg, #60a5fa, #3b82f6);\n"
                            + "        }\n"
                            + "        .footer {\n"
                            + "            text-align: center;\n"
                            + "            font-size: 14px;\n"
                            + "            color: #6b7280;\n"
                            + "            padding: 20px;\n"
                            + "        }\n"
                            + "        .footer a {\n"
                            + "            color: #3b82f6;\n"
                            + "            text-decoration: none;\n"
                            + "            margin: 0 10px;\n"
                            + "        }\n"
                            + "    </style>\n"
                            + "</head>\n"
                            + "<body>\n"
                            + "    <div class=\"container py-5\">\n"
                            + "        <div class=\"card\">\n"
                            + "            <div class=\"card-header\">\n"
                            + "                <img src=\"https://res.cloudinary.com/dliwvet1v/image/upload/v1729759384/i7xwpwwwsmwikzk5v8hm.png\" alt=\"Company Logo\">\n"
                            + "                <h1>Booking Confirmation</h1>\n"
                            + "            </div>\n"
                            + "            <div class=\"card-body\">\n"
                            + "                <h2 class=\"card-title text-center\">Your Booking Details</h2>\n"
                            + "                <p class=\"text-center text-muted\">Thank you for choosing us! Your booking is now confirmed.</p>\n"
                            + "                <table class=\"table table-bordered\">\n"
                            + "                    <thead>\n"
                            + "                        <tr>\n"
                            + "                            <th>Detail</th>\n"
                            + "                            <th>Information</th>\n"
                            + "                        </tr>\n"
                            + "                    </thead>\n"
                            + "                    <tbody>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-receipt table-icon\"></i>Order ID</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-money-bill-wave table-icon\"></i>Total Price</td>\n"
                            + "                            <td>%s VND</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-clock table-icon\"></i>Payment Time</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-hashtag table-icon\"></i>Transaction ID</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-user table-icon\"></i>Customer Name</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-calendar-check table-icon\"></i>Check-in Date</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                        <tr>\n"
                            + "                            <td><i class=\"fas fa-calendar-times table-icon\"></i>Check-out Date</td>\n"
                            + "                            <td>%s</td>\n"
                            + "                        </tr>\n"
                            + "                    </tbody>\n"
                            + "                </table>\n"
                            + "                <div class=\"text-center mt-4\">\n"
                            + "                    <a href=\"#\" class=\"btn btn-primary\">Visit Our Site</a>\n"
                            + "                </div>\n"
                            + "            </div>\n"
                            + "            <div class=\"footer\">\n"
                            + "                <p>&copy; 2024 HotelEasy. All Rights Reserved.</p>\n"
                            + "                <p>No. 99, Thao Dien, Thu Duc, Ho Chi Minh City</p>\n"
                            + "                <p><a href=\"#\">Privacy Policy</a> | <a href=\"#\">Terms of Service</a></p>\n"
                            + "            </div>\n"
                            + "        </div>\n"
                            + "    </div>\n"
                            + "</body>\n"
                            + "</html>",
                    orderInfo,
                    totalPrice,
                    paymentTime,
                    transactionId,
                    bookingDTO.getCustomerName(),
                    bookingDTO.getCheckinDate(),
                    bookingDTO.getCheckoutDate()
            );

            emailService.sendBookingConfirmation(bookingDTO.getCustomerEmail(), subject, emailContent);
            return "redirect:/booking/confirmation"; // Redirect to confirmation page
        }
    }

    @GetMapping("/booking/confirmation")
    public String showConfirmationPage(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        BookingDTO bookingDTO = (BookingDTO) model.asMap().get("bookingDTO");

        if (bookingDTO == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your session has expired or the booking process was not completed properly. Please start a new search.");
            return "redirect:/search";
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
        }
        UserDTO userDTO = userService.findUserDTOByUsername(username);
        log.info("Fetched logged in user ID: {}", userDTO.getId());
        return userDTO.getId();
    }
}
