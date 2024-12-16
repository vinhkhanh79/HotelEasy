package com.datn.tourhotel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.tourhotel.model.Booking;
import com.datn.tourhotel.model.Comment;
import com.datn.tourhotel.model.Customer;
import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.Voucher;
import com.datn.tourhotel.model.dto.CommentDTO;
import com.datn.tourhotel.model.dto.HotelAvailabilityDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelSearchDTO;
import com.datn.tourhotel.repository.BookingRepository;
import com.datn.tourhotel.repository.CommentRepository;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.repository.VoucherRepository;
import com.datn.tourhotel.security.CustomUserDetails;
import com.datn.tourhotel.service.CommentService;
import com.datn.tourhotel.service.HotelSearchService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.PostService;
import com.datn.tourhotel.service.UserService;
import com.datn.tourhotel.service.VoucherService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HotelSearchController {
	
	@Autowired
	private MessageSource messageSource;
    private final HotelSearchService hotelSearchService;
    private final HotelService hotelService;
    private final CommentService commentService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;
    UserDetails userDetails;
    
    @GetMapping("/index")
    public String showSearchForm(@ModelAttribute("hotelSearchDTO") HotelSearchDTO hotelSearchDTO, Model model, HttpServletRequest request) {
    	 String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	 List<HotelDTO> hotels = hotelService.findAllHotels();
    	 List<Post> posts = postService.getAllPosts(); // Lấy danh sách bài viết mới nhất
         model.addAttribute("posts", posts);
         model.addAttribute("hotels", hotels);
        return "index";
    }

    @PostMapping("/index")
    public String findAvailableHotelsByaddressLineAndDate(@Valid @ModelAttribute("hotelSearchDTO") HotelSearchDTO hotelSearchDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "index";
        }
        try {
            validateCheckinAndCheckoutDates(hotelSearchDTO.getCheckinDate(), hotelSearchDTO.getCheckoutDate());
        } catch (IllegalArgumentException e) {
            result.rejectValue("checkoutDate", null, e.getMessage());
            return "index";
        }

        // Redirect to a new GET endpoint with parameters for data fetching. Allows page refreshing
        return String.format("redirect:/search-results?addressLine=%s&checkinDate=%s&checkoutDate=%s", hotelSearchDTO.getAddressLine(), hotelSearchDTO.getCheckinDate(), hotelSearchDTO.getCheckoutDate());
    }

    @GetMapping("/search-results")
    public String showSearchResults(@RequestParam String addressLine, @RequestParam String checkinDate, @RequestParam String checkoutDate, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            LocalDate parsedCheckinDate = LocalDate.parse(checkinDate);
            LocalDate parsedCheckoutDate = LocalDate.parse(checkoutDate);

            validateCheckinAndCheckoutDates(parsedCheckinDate, parsedCheckoutDate);

            log.info("Searching hotels for address line {} between dates {} and {}", addressLine, checkinDate, checkoutDate);
            List<HotelAvailabilityDTO> hotels = hotelSearchService.findAvailableHotelsByaddressLineAndDate(addressLine, parsedCheckinDate, parsedCheckoutDate);

            if (hotels.isEmpty()) {
                model.addAttribute("noHotelsFound", true);
            }

            long durationDays = ChronoUnit.DAYS.between(parsedCheckinDate, parsedCheckoutDate);
            durationDays = durationDays == 0 ? 1 : durationDays; // Nếu cùng ngày, tính là 1 ngày

            model.addAttribute("hotels", hotels);
            model.addAttribute("addressLine", addressLine);
            model.addAttribute("days", durationDays);
            model.addAttribute("checkinDate", checkinDate);
            model.addAttribute("checkoutDate", checkoutDate);

        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided for URL search", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Please use the search form.");
            return "redirect:/index";
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for URL search", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/index";
        } catch (Exception e) {
            log.error("An error occurred while searching for hotels", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/index";
        }

        return "hotelsearch/search-results";
    }

    @GetMapping("/hotel-details/{id}")
    public String showHotelDetails(@PathVariable Long id, @RequestParam String checkinDate, @RequestParam String checkoutDate, @RequestParam(required = false) String language, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request, Authentication authentication) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	if (language != null) {
            Locale locale = new Locale(language);
            request.getSession().setAttribute("language", language);
            request.getSession().setAttribute("locale", locale);
        }
    	try {
            LocalDate parsedCheckinDate = LocalDate.parse(checkinDate);
            LocalDate parsedCheckoutDate = LocalDate.parse(checkoutDate);

            validateCheckinAndCheckoutDates(parsedCheckinDate, parsedCheckoutDate);

            HotelAvailabilityDTO hotelAvailabilityDTO = hotelSearchService.findAvailableHotelById(id, parsedCheckinDate, parsedCheckoutDate);
            
            long durationDays = ChronoUnit.DAYS.between(parsedCheckinDate, parsedCheckoutDate);
            durationDays = durationDays == 0 ? 1 : durationDays; // Nếu cùng ngày, tính là 1 ngày
            
         // Get average rating
            double averageRating = commentService.getAverageRatingByHotelId(id);
            
            // Add average rating to model
            model.addAttribute("averageRating", averageRating);

            // Lấy danh sách bình luận
            List<CommentDTO> comments = commentService.getCommentsByHotelId(id);

            // Kiểm tra điều kiện bình luận
            boolean isLoggedIn = authentication != null && authentication.getPrincipal() != null;
            boolean hasBooked = false;
            boolean hasCommented = false;

            if (isLoggedIn) {
                UserDetails userDetails = getUserDetailsFromAuthentication(authentication);
                User user = userRepository.findByUsername(userDetails.getUsername());
                if (user != null) {
                    // Lấy customerId từ user
                    Customer customer = user.getCustomer();
                    if (customer != null) {
                        List<Booking> bookings = bookingRepository.findBookingsByCustomerAndHotel(customer.getId(), id);
                        hasBooked = !bookings.isEmpty();
                        hasCommented = commentRepository.existsByUserIdAndHotelId(user.getId(), id);
                        log.info("Checking booking for user {} at hotel {}: {}", user.getId(), id, hasBooked);
                    }
                }
            }

            // Thêm các thuộc tính vào model để kiểm tra điều kiện trong view
            model.addAttribute("hotel", hotelAvailabilityDTO);
            model.addAttribute("durationDays", durationDays);
            model.addAttribute("checkinDate", checkinDate);
            model.addAttribute("checkoutDate", checkoutDate);
            model.addAttribute("comments", comments);
            model.addAttribute("isLoggedIn", isLoggedIn);
            model.addAttribute("hasBooked", hasBooked);
            model.addAttribute("hasCommented", hasCommented);

            return "hotelsearch/hotel-details";


        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Please use the search form.");
            return "redirect:/index";
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for URL search", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/index";
        } catch (EntityNotFoundException e) {
            log.error("No hotel found with ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "The selected hotel is no longer available. Please start a new search.");
            return "redirect:/index";
        } catch (Exception e) {
            log.error("An error occurred while searching for hotels", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/index";
        }
    }
    
    @PostMapping("/hotel-details/update-dates")
    public String updateDates(@RequestParam Long hotelId,
                              @RequestParam String checkinDate,
                              @RequestParam String checkoutDate,
                              RedirectAttributes redirectAttributes) {
        try {
            LocalDate parsedCheckinDate = LocalDate.parse(checkinDate);
            LocalDate parsedCheckoutDate = LocalDate.parse(checkoutDate);

            validateCheckinAndCheckoutDates(parsedCheckinDate, parsedCheckoutDate);

            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", hotelId, checkinDate, checkoutDate);

        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Please use the search form.");
            return "redirect:/index";
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/index";
        } catch (Exception e) {
            log.error("An error occurred while updating dates", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/index";
        }
    }
    @GetMapping("/api/vouchers/apply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestParam String code, @RequestParam double totalPrice) {
        Map<String, Object> response = new HashMap<>();
        try {
            double discount = voucherService.calculateDiscount(code, totalPrice);
            double discountAmount = totalPrice * discount; // Tính số tiền giảm
            response.put("success", true);
            response.put("discountAmount", discountAmount);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    @PostMapping("/hotel-details/{id}/add-comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @RequestParam int rating,
                             @RequestParam String checkinDate,
                             @RequestParam String checkoutDate,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        UserDetails userDetails;

        if (authentication == null || authentication.getPrincipal() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You need to log in to add a evaluate.");
            return "redirect:/login";
        }

        if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
            userDetails = new CustomUserDetails(oauth2User);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        } else {
            log.error("Unknown principal type for authentication.");
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid authentication.");
            return "redirect:/login";
        }

        // Parse dates
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate checkin, checkout;
//
//        try {
//            checkin = LocalDate.parse(checkinDate, formatter);
//            checkout = LocalDate.parse(checkoutDate, formatter);
//        } catch (DateTimeParseException e) {
//            log.error("Invalid date format for check-in or check-out.");
//            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format.");
//            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", id, checkinDate, checkoutDate);
//        }
//
//        if (checkout.isBefore(checkin)) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Check-out date must be after check-in date.");
//            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", id, checkinDate, checkoutDate);
//        }
//
//        if (rating < 1 || rating > 5) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Rating must be between 1 and 5.");
//            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", id, checkinDate, checkoutDate);
//        }

        try {
            log.info("Adding evaluate for hotel ID: {}", id);
            log.info("User: {}", userDetails.getUsername());

            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user == null) {
                log.error("User not found for username: {}", userDetails.getUsername());
                redirectAttributes.addFlashAttribute("errorMessage", "User not found. Please log in again.");
                return "redirect:/login";
            }

            commentService.addComment(id, user.getId(), content, rating);
            redirectAttributes.addFlashAttribute("successMessage", "Your evaluate has been added!");
            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", id, checkinDate, checkoutDate);
        } catch (Exception e) {
            log.error("Failed to add evaluate for hotel ID: {}. Error: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while adding your evaluate. Please try again later.");
            return String.format("redirect:/hotel-details/%d?checkinDate=%s&checkoutDate=%s", id, checkinDate, checkoutDate);
        }
    }

    private UserDetails getUserDetailsFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
            return new CustomUserDetails(oauth2User);  // Assuming you have a CustomUserDetails class
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else {
            return null;  // Handle other cases, if necessary
        }
    }


    private void validateCheckinAndCheckoutDates(LocalDate checkinDate, LocalDate checkoutDate) {
        if (checkinDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
//        if (checkoutDate.isBefore(checkinDate.plusDays(1))) {
//            throw new IllegalArgumentException("Check-out date must be after check-in date");
//        }
    }

    private void parseAndValidateBookingDates(String checkinDate, String checkoutDate) {
        LocalDate parsedCheckinDate = LocalDate.parse(checkinDate);
        LocalDate parsedCheckoutDate = LocalDate.parse(checkoutDate);
        validateCheckinAndCheckoutDates(parsedCheckinDate, parsedCheckoutDate);
    }

}