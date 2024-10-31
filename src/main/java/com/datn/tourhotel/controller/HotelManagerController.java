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
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelRegistrationDTO;
import com.datn.tourhotel.model.dto.RoomDTO;
import com.datn.tourhotel.model.enums.RoomType;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@Slf4j
public class HotelManagerController {
	
	@Autowired
	private MessageSource messageSource;
    private final HotelService hotelService;
    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        return "hotelmanager/dashboard";
    }
    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<HotelDTO> hotels = hotelService.findAllHotels();
        model.addAttribute("hotels", hotels);
        return "redirect:/index?language=" + request.getParameter("language");
    }
    @GetMapping("/hotels/add")
    public String showAddHotelForm(Model model) {
        HotelRegistrationDTO hotelRegistrationDTO = new HotelRegistrationDTO();

        // Initialize roomDTOs with SINGLE and DOUBLE room types
        RoomDTO singleRoom = new RoomDTO(null, null, RoomType.SINGLE, null, null, null, 0, 0.0);
        RoomDTO doubleRoom = new RoomDTO(null, null, RoomType.DOUBLE, null,null, null, 0, 0.0);
        RoomDTO suiteRoom = new RoomDTO(null, null, RoomType.SUITE, null, null, null, 0, 0.0);
        hotelRegistrationDTO.getRoomDTOs().add(singleRoom);
        hotelRegistrationDTO.getRoomDTOs().add(doubleRoom);
        hotelRegistrationDTO.getRoomDTOs().add(suiteRoom);

        model.addAttribute("hotel", hotelRegistrationDTO);
        return "hotelmanager/hotels-add";
    }

    @PostMapping("/hotels/add")
    public String addHotel(@Valid @ModelAttribute("hotel") HotelRegistrationDTO hotelRegistrationDTO,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("imageFile2") MultipartFile imageFile2,
            @RequestParam("imageFile3") MultipartFile imageFile3,
            @RequestParam("roomImages1") List<MultipartFile> roomImages1,
            @RequestParam("roomImages2") List<MultipartFile> roomImages2,
            @RequestParam("roomImages3") List<MultipartFile> roomImages3,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Hotel creation failed due to validation errors: {}", result.getAllErrors());
            return "hotelmanager/hotels-add";
        }
        try {
            hotelService.saveHotel(hotelRegistrationDTO, imageFile, imageFile2, imageFile3, roomImages1, roomImages2, roomImages3);
            redirectAttributes.addFlashAttribute("message", "Hotel " + hotelRegistrationDTO.getName() + " added successfully");
            return "redirect:/manager/hotels";
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());
            return "hotelmanager/hotels-add";
        }
    }

    @GetMapping("/hotels")
    public String listHotels(Model model) {
        Long managerId = getCurrentManagerId();
        List<HotelDTO> hotelList = hotelService.findAllHotelDtosByManagerId(managerId);
        model.addAttribute("hotels", hotelList);
        return "hotelmanager/hotels";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id, Model model) {
        Long managerId = getCurrentManagerId();
        HotelDTO hotelDTO = hotelService.findHotelByIdAndManagerId(id, managerId);
        model.addAttribute("hotel", hotelDTO);
        return "hotelmanager/hotels-edit";
    }

    @PostMapping("/hotels/edit/{id}")
    public String editHotel(@PathVariable Long id, 
                            @Valid @ModelAttribute("hotel") HotelDTO hotelDTO,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            @RequestParam("imageFile2") MultipartFile imageFile2,
                            @RequestParam("imageFile3") MultipartFile imageFile3,
                            @RequestParam(value = "roomImages", required = false) List<MultipartFile> roomImages,
                            @RequestParam(value = "roomImages2", required = false) List<MultipartFile> roomImages2,
                            @RequestParam(value = "roomImages3", required = false) List<MultipartFile> roomImages3,
                            BindingResult result, 
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "hotelmanager/hotels-edit";
        }
        try {
            Long managerId = getCurrentManagerId();  // Lấy ID của quản lý hiện tại
            hotelDTO.setId(id);  // Set ID cho HotelDTO

            // Cập nhật hotel cùng với ảnh phòng
            hotelService.updateHotelByManagerId(hotelDTO, managerId, imageFile, imageFile2, imageFile3, roomImages, roomImages2, roomImages3);

            redirectAttributes.addFlashAttribute("message", "Hotel ID: " + id + " updated successfully");
            return "redirect:/manager/hotels";
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());
            return "hotelmanager/hotels-edit";
        } catch (EntityNotFoundException e) {
            result.rejectValue("id", "hotel.notfound", e.getMessage());
            return "hotelmanager/hotels-edit";
        } catch (Exception e) {
            // Thêm xử lý ngoại lệ tổng quát nếu cần
            result.reject("error", "An error occurred while updating the hotel: " + e.getMessage());
            return "hotelmanager/hotels-edit";
        }
    }




    @PostMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        Long managerId = getCurrentManagerId();
        hotelService.deleteHotelByIdAndManagerId(id, managerId);
        return "redirect:/manager/hotels";
    }

    @GetMapping("/bookings")
    public String listBookings(Model model, RedirectAttributes redirectAttributes) {
        try {
            Long managerId = getCurrentManagerId();
            List<BookingDTO> bookingDTOs = bookingService.findBookingsByManagerId(managerId);
            model.addAttribute("bookings", bookingDTOs);

            return "hotelmanager/bookings";
        } catch (EntityNotFoundException e) {
            log.error("No bookings found for the provided manager ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Bookings not found. Please try again later.");
            return "redirect:/manager/dashboard";
        } catch (Exception e) {
            log.error("An error occurred while listing bookings", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/manager/dashboard";
        }
    }

    @GetMapping("/bookings/{id}")
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Long managerId = getCurrentManagerId();
            BookingDTO bookingDTO = bookingService.findBookingByIdAndManagerId(id, managerId);
            model.addAttribute("bookingDTO", bookingDTO);

            LocalDate checkinDate = bookingDTO.getCheckinDate();
            LocalDate checkoutDate = bookingDTO.getCheckoutDate();
            long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
            model.addAttribute("days", durationDays);

            return "hotelmanager/bookings-details";
        } catch (EntityNotFoundException e) {
            log.error("No booking found with the provided ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found. Please try again later.");
            return "redirect:/manager/dashboard";
        } catch (Exception e) {
            log.error("An error occurred while displaying booking details", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/manager/dashboard";
        }
    }

    private Long getCurrentManagerId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserByUsername(username).getHotelManager().getId();
    }
}
