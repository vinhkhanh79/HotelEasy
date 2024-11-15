package com.datn.tourhotel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.UserService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
	
	@Autowired
	private MessageSource messageSource;
    private final UserService userService;
    private final BookingService bookingService;
    private final HotelService hotelService;
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth, UserDetails userDetails) {
        System.out.println(auth.getCredentials());
        System.out.println(userDetails.getUsername());
        return "customer/dashboard";
    }

    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<HotelDTO> hotels = hotelService.findAllHotels();
        model.addAttribute("hotels", hotels);
        return "redirect:/index?language=" + request.getParameter("language");
    }

    @GetMapping("/bookings")
    public String listBookings(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            Long customerId = getLoggedInUserIdOrCustomerId();
            List<BookingDTO> bookingDTOs = bookingService.findBookingsByCustomerId(customerId);
            bookingDTOs.sort(Comparator.comparing(BookingDTO::getId).reversed());
            model.addAttribute("bookings", bookingDTOs);
            return "customer/bookings";
        } catch (EntityNotFoundException e) {
            log.error("No customer found with the provided ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found. Please log in again.");
            return "redirect:/account/login";
        } catch (Exception e) {
            log.error("An error occurred while listing bookings", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/index";
        }
    }

    @GetMapping("/bookings/{id}")
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            Long customerId = getLoggedInUserIdOrCustomerId();
            BookingDTO bookingDTO = bookingService.findBookingByIdAndCustomerId(id, customerId);
            model.addAttribute("bookingDTO", bookingDTO);

            LocalDate checkinDate = bookingDTO.getCheckinDate();
            LocalDate checkoutDate = bookingDTO.getCheckoutDate();
            long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
            model.addAttribute("days", durationDays);

            return "customer/bookings-details";
        } catch (EntityNotFoundException e) {
            log.error("No booking found with the provided ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found. Please try again later.");
            return "redirect:/customer/bookings";
        } catch (Exception e) {
            log.error("An error occurred while displaying booking details", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/";
        }
    }
    
    @PostMapping("/bookings/{id}/refund-request")
    public String requestRefund(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.requestRefund(id);
            redirectAttributes.addFlashAttribute("success", "Your refund request has been submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit refund request: " + e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }

    private Long getLoggedInUserIdOrCustomerId() {
        String username = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra loại xác thực (OAuth2 hay thông thường)
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) auth;
            username = (String) oauth2Token.getPrincipal().getAttributes().get("email");
        } else {
            username = auth.getName();
        }

        // Lấy thông tin người dùng từ username
        User user = userService.findUserByUsername(username);
        log.info("Fetched logged in user ID: {}", user.getId());

        // Nếu người dùng là khách hàng, trả về customer ID
        if (user.getCustomer() != null) {
            return user.getCustomer().getId();
        }

        // Trả về user ID
        return user.getId();
    }


}