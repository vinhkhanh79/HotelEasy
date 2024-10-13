package com.datn.tourhotel.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.datn.tourhotel.exception.UsernameAlreadyExistsException;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MyAccountController {
	
	@Autowired
	private MessageSource messageSource;
    private final UserService userService;

    // Customer actions
    @GetMapping("/customer/account")
    public String showCustomerAccount(Model model, HttpServletRequest request){
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        log.debug("Displaying customer account");
        addLoggedInUserDataToModel(model);
        return "customer/account";
    }

    @GetMapping("/customer/account/edit")
    public String showCustomerEditForm(Model model, HttpServletRequest request){
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        log.debug("Displaying customer account edit form");
        addLoggedInUserDataToModel(model);
        return "customer/account-edit";
    }

    @PostMapping("/customer/account/edit")
    public String editCustomerAccount(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult result) {
        log.info("Attempting to edit customer account details for ID: {}", userDTO.getId());
        
     // Lấy thông tin người dùng hiện tại từ service
        UserDTO currentUserDTO = userService.findUserById(userDTO.getId());

        // Nếu người dùng không chọn ảnh mới, giữ nguyên ảnh cũ
        if (userDTO.getImg() == null || userDTO.getImg().isEmpty()) {
            userDTO.setImg(currentUserDTO.getImg()); // Giữ nguyên ảnh cũ
        }
        // Kiểm tra xem form có lỗi không
        if (result.hasErrors()) {
            log.warn("Validation errors occurred while editing customer account");
            return "customer/account-edit";
        }

        try {
            userService.updateLoggedInUser(userDTO);
            log.info("Successfully edited customer account");
        } catch (UsernameAlreadyExistsException e) {
            log.error("Username already exists error", e);
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "customer/account-edit";
        }

        return "redirect:/customer/account?success";
    }



    // Hotel Manager actions
    @GetMapping("/manager/account")
    public String showHotelManagerAccount(Model model, HttpServletRequest request){
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        log.debug("Displaying hotel manager account");
        addLoggedInUserDataToModel(model);
        return "hotelmanager/account";
    }

    @GetMapping("/manager/account/edit")
    public String showHotelManagerEditForm(Model model, HttpServletRequest request){
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        log.debug("Displaying hotel manager account edit form");
        addLoggedInUserDataToModel(model);
        return "hotelmanager/account-edit";
    }

    @PostMapping("/manager/account/edit")
    public String editHotelManagerAccount(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult result) {
        log.info("Attempting to edit hotel manager account details for ID: {}", userDTO.getId());
        
     // Lấy thông tin người dùng hiện tại từ service
        UserDTO currentUserDTO = userService.findUserById(userDTO.getId());

        // Nếu người dùng không chọn ảnh mới, giữ nguyên ảnh cũ
        if (userDTO.getImg() == null || userDTO.getImg().isEmpty()) {
            userDTO.setImg(currentUserDTO.getImg()); // Giữ nguyên ảnh cũ
        }
        if (result.hasErrors()) {
            log.warn("Validation errors occurred while editing hotel manager account");
            return "hotelmanager/account-edit";
        }
        try {
            userService.updateLoggedInUser(userDTO);
            log.info("Successfully edited hotel manager account");
        } catch (UsernameAlreadyExistsException e) {
            log.error("Username already exists error", e);
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "hotelmanager/account-edit";
        }
        return "redirect:/manager/account?success";
    }

    private void addLoggedInUserDataToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserDTO userDTO = userService.findUserDTOByUsername(username);
        log.info("Adding logged in user data to model for user ID: {}", userDTO.getId());
        model.addAttribute("user", userDTO);
    }

}
