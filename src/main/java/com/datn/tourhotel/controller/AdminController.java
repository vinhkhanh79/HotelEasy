package com.datn.tourhotel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.tourhotel.exception.HotelAlreadyExistsException;
import com.datn.tourhotel.exception.UsernameAlreadyExistsException;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.CustomerService;
import com.datn.tourhotel.service.HotelManagerService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.PaymentService;
import com.datn.tourhotel.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
	
	@Autowired
	private MessageSource messageSource;
    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final CustomerService customerService;
    private final HotelManagerService hotelManagerService;
    private final PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request, @RequestParam(name = "earningsPeriod", required = false, defaultValue = "total") String period) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	Long customerCount = customerService.getCustomerCount();
        model.addAttribute("customerCount", customerCount);

        Long countHotelManagers = hotelManagerService.countHotelManagers();
        model.addAttribute("countHotelManagers", countHotelManagers);
        
        BigDecimal earningsTodayAdmin = paymentService.getEarningsByPeriodAdmin("day");
        BigDecimal earningsThisWeekAdmin = paymentService.getEarningsByPeriodAdmin("week");
        BigDecimal earningsThisMonthAdmin = paymentService.getEarningsByPeriodAdmin("month");
        BigDecimal earningsThisYearAdmin = paymentService.getEarningsByPeriodAdmin("year");
        BigDecimal earningsTotalAdmin = paymentService.getEarningsByPeriodAdmin("total");

        // Add earnings data to the model for use in the frontend
        model.addAttribute("earningsToday", earningsTodayAdmin);
        model.addAttribute("earningsThisWeek", earningsThisWeekAdmin);
        model.addAttribute("earningsThisMonth", earningsThisMonthAdmin);
        model.addAttribute("earningsThisYear", earningsThisYearAdmin);
        model.addAttribute("earningsTotal", earningsTotalAdmin);
        
        List<BigDecimal> earningsPerDayInYear = paymentService.getEarningsPerDayInYearAdmin();
        model.addAttribute("earningsPerDayInYear", earningsPerDayInYear);
        
     // Tổng thu nhập theo period
        BigDecimal getEarningsByPeriod = paymentService.getEarningsByPeriodAdmin(period);
        if (getEarningsByPeriod == null) {
            getEarningsByPeriod = BigDecimal.ZERO;
        }
     // Format thu nhập theo định dạng "1,500,000"
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedEarnings = decimalFormat.format(getEarningsByPeriod);
        model.addAttribute("formattedEarnings", formattedEarnings);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        model.addAttribute("greetingMessage", message);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<UserDTO> userDTOList = userService.findAllUsers();
        model.addAttribute("users", userDTOList);
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        UserDTO userDTO = userService.findUserById(id);
        model.addAttribute("user", userDTO);
        return "admin/users-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(value = "multipartFile", required = false) MultipartFile multipartFile) throws IOException {
        try {
            if (result.hasErrors()) {
                return "admin/users-edit";
            }

            // Lấy thông tin người dùng hiện tại từ service
            UserDTO currentUserDTO = userService.findUserById(id);

            // Kiểm tra nếu người dùng không chọn ảnh mới, giữ nguyên ảnh cũ
            if (multipartFile == null || multipartFile.isEmpty()) {
                userDTO.setImg(currentUserDTO.getImg()); // Giữ nguyên ảnh cũ
            }

            userService.updateUser(userDTO, multipartFile);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            redirectAttributes.addFlashAttribute("updatedUserId", userDTO.getId());

            return "redirect:/admin/users";
        } catch (UsernameAlreadyExistsException e) {
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "admin/users-edit";
        } catch (IllegalStateException e) {
            result.rejectValue("roleType", "role.invalid", e.getMessage());
            return "admin/users-edit";
        } catch (Exception e) {
            result.rejectValue("", "error.general", "An error occurred while updating the user");
            return "admin/users-edit";
        }
    }


    // Workaround for @DeleteMapping via post method
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/hotels")
    public String listHotels(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<HotelDTO> hotelDTOList = hotelService.findAllHotels();
        model.addAttribute("hotels", hotelDTOList);
        return "admin/hotels";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        HotelDTO hotelDTO = hotelService.findHotelDtoById(id);
        model.addAttribute("hotel", hotelDTO);
        return "admin/hotels-edit";
    }

    @PostMapping("/hotels/edit/{id}")
    public String editHotel(@PathVariable Long id, @Valid @ModelAttribute("hotel") HotelDTO hotelDTO,
            BindingResult result,
    		@RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("imageFile2") MultipartFile imageFile2,
            @RequestParam("imageFile3") MultipartFile imageFile3,
            @RequestParam(value = "roomImages", required = false) List<MultipartFile> roomImages,
            @RequestParam(value = "roomImages2", required = false) List<MultipartFile> roomImages2,
            @RequestParam(value = "roomImages3", required = false) List<MultipartFile> roomImages3,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/hotels-edit";
        }
        try {
            hotelService.updateHotel(hotelDTO, imageFile, imageFile2, imageFile3, roomImages, roomImages2, roomImages3);
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());
            return "admin/hotels-edit";
        }

        redirectAttributes.addFlashAttribute("updatedHotelId", hotelDTO.getId());
        return "redirect:/admin/hotels?success";
    }

    @PostMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotelById(id);
        return "redirect:/admin/hotels";
    }

    @GetMapping("/bookings")
    public String listBookings(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<BookingDTO> bookingDTOList = bookingService.findAllBookings();
        model.addAttribute("bookings", bookingDTOList);
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            BookingDTO bookingDTO = bookingService.findBookingById(id);
            model.addAttribute("bookingDTO", bookingDTO);

            LocalDate checkinDate = bookingDTO.getCheckinDate();
            LocalDate checkoutDate = bookingDTO.getCheckoutDate();
            long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
            model.addAttribute("days", durationDays);

            return "admin/booking-details";
        } catch (EntityNotFoundException e) {
            log.error("No booking found with the provided ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found. Please try again later.");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            log.error("An error occurred while displaying booking details", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/admin/dashboard";
        }
    }
    @PostMapping("/bookings/{id}/confirm-refund")
    public String confirmRefund(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmRefund(id);
            redirectAttributes.addFlashAttribute("success", "Refund has been confirmed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to confirm refund: " + e.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }
}
