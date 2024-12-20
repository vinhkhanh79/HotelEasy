package com.datn.tourhotel.controller;

import com.datn.tourhotel.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.tourhotel.exception.UsernameAlreadyExistsException;
import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelSearchDTO;
import com.datn.tourhotel.model.dto.PasswordChangeDTO;
import com.datn.tourhotel.model.dto.PasswordForgotDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.UserRegistrationDTO;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.security.RedirectUtil;
import com.datn.tourhotel.service.HotelSearchService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.PostService;
import com.datn.tourhotel.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	@Autowired
	private MessageSource messageSource;
    private final UserService userService;
    private final HotelService hotelService;
    private final UserRepository userRepository;
    private final PostService postService;
    UserDetails userDetails;

    @GetMapping("/")
    public String homePage(Authentication authentication, Model model, HttpServletRequest request, @ModelAttribute("hotelSearchDTO") HotelSearchDTO hotelSearchDTO) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        String redirect = getAuthenticatedUserRedirectUrl(authentication);
        List<HotelDTO> hotels = hotelService.findAllHotels();
        List<Post> posts = postService.getAllPosts(); // Lấy danh sách bài viết mới nhất
        model.addAttribute("posts", posts);
        model.addAttribute("hotels", hotels);
        if (redirect != null) return redirect;
        log.debug("Accessing home page");
        System.out.println(hotels); 
        return "index";
    }
    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        // Fetch the post data from the database
        Optional<Post> post = postService.findById(id);

        if (post.isPresent()) {
            // Add post data to the model
            model.addAttribute("post", post.get());
        } else {
            // Handle the case where the post is not found
            model.addAttribute("error", "Post not found");
        }

        // Return the view name
        return "post/post";
    }


    @GetMapping("/login")
    public String loginPage(Authentication authentication, HttpServletRequest request) {
        String language = (String) request.getSession().getAttribute("language");
        
        // Set a default language if none is set
        if (language == null) {
            language = "en"; // or any default language you prefer
            request.getSession().setAttribute("language", language);
        }

        String redirect = getAuthenticatedUserRedirectUrl(authentication);
        if (redirect != null) return redirect;
        log.debug("Accessing login page");
        return "account/login";
    }

    
    @GetMapping("/register/customer")
    public String showCustomerRegistrationForm(@ModelAttribute("user") UserRegistrationDTO registrationDTO, Authentication authentication) {
        String redirect = getAuthenticatedUserRedirectUrl(authentication);
        if (redirect != null) return redirect;
        log.info("Showing customer registration form");
        return "account/register-customer";
    }

    @PostMapping("/register/customer")
    public String registerCustomerAccount(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                                          BindingResult result,
                                          Model model) {

        if (userService.usernameExists(registrationDTO.getUsername())) {
            result.rejectValue("username", "username.duplicate", "Username is already taken!");
        }
        
        if (userService.emailExists(registrationDTO.getEmail())) {
            result.rejectValue("email", "email.duplicate", "Email is already taken!");
        }

        if (result.hasErrors()) {
            return "account/register-customer"; // Stay on the registration page and show errors
        }
        
        registrationDTO.setRoleType(RoleType.CUSTOMER);

        // Proceed with registration if no errors
        try {
            userService.saveUser(registrationDTO);
            model.addAttribute("successMessage", "Registration successful. You can now log in.");
            return "account/register-customer"; // Stay on the registration page and show success message
        } catch (Exception e) {
            // Handle unexpected errors
            result.reject("error.unexpected", "An unexpected error occurred. Please try again.");
            return "account/register-customer";
        }
    }

    @GetMapping("/register/manager")
    public String showManagerRegistrationForm(@ModelAttribute("user") UserRegistrationDTO registrationDTO, Authentication authentication, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        String redirect = getAuthenticatedUserRedirectUrl(authentication);
        if (redirect != null) return redirect;
        log.info("Showing manager registration form");
        return "account/register-manager";
    }

    @PostMapping("/register/manager")
    public String registerManagerAccount(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO, BindingResult result) {
        log.info("Attempting to register manager account: {}", registrationDTO.getUsername());
        registrationDTO.setRoleType(RoleType.HOTEL_MANAGER);
        return registerUser(registrationDTO, result, "account/register-manager", "register/manager");
    }
    
    @GetMapping("/customer/changePass")
    public String showChangePasswordForm(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "account/changePass";
    }

    @PostMapping("/customer/changePass")
    public String changePassword(
            @Valid @ModelAttribute("passwordChangeDTO") PasswordChangeDTO passwordChangeDTO,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Password change failed due to validation error: {}", result.getAllErrors());
            return "account/changePass";
        }

        if (!passwordChangeDTO.isPasswordsMatching()) {
            result.rejectValue("confirmNewPassword", "error.passwordChangeDTO", "New password and confirmation do not match.");
            return "account/changePass";
        }

        try {

            if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
                DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
                userDetails = new CustomUserDetails(oauth2User);
            } else if (authentication.getPrincipal() instanceof UserDetails) {
                userDetails = (UserDetails) authentication.getPrincipal();
            } else {
                throw new IllegalStateException("Unknown principal type");
            }

            String username = userDetails.getUsername();
            userService.changePassword(username, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("success", "Password change successful for user: " + username);
            return "redirect:/customer/changePass?success";

        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            result.rejectValue("currentPassword", "error.passwordChangeDTO", "Current password is incorrect.");
            return "account/changePass";
        }
    }
    
    @GetMapping("/admin/changePass")
    public String showChangePasswordFormAdmin(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "account/changePassAdmin";
    }

    @PostMapping("/admin/changePass")
    public String changePasswordAdmin(
            @Valid @ModelAttribute("passwordChangeDTO") PasswordChangeDTO passwordChangeDTO,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Password change failed due to validation error: {}", result.getAllErrors());
            return "account/changePassAdmin";
        }

        if (!passwordChangeDTO.isPasswordsMatching()) {
            result.rejectValue("confirmNewPassword", "error.passwordChangeDTO", "New password and confirmation do not match.");
            return "account/changePassAdmin";
        }

        try {

            if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
                DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
                userDetails = new CustomUserDetails(oauth2User);
            } else if (authentication.getPrincipal() instanceof UserDetails) {
                userDetails = (UserDetails) authentication.getPrincipal();
            } else {
                throw new IllegalStateException("Unknown principal type");
            }

            String username = userDetails.getUsername();
            userService.changePassword(username, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("success", "Password change successful for user: " + username);
            return "redirect:/admin/changePass?success";

        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            result.rejectValue("currentPassword", "error.passwordChangeDTO", "Current password is incorrect.");
            return "account/changePassAdmin";
        }
    }
    
    @GetMapping("/manager/changePass")
    public String showChangePasswordFormManager(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "account/changePassManager";
    }

    @PostMapping("/manager/changePass")
    public String changePasswordManager(
            @Valid @ModelAttribute("passwordChangeDTO") PasswordChangeDTO passwordChangeDTO,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Password change failed due to validation error: {}", result.getAllErrors());
            return "account/changePassManager";
        }

        if (!passwordChangeDTO.isPasswordsMatching()) {
            result.rejectValue("confirmNewPassword", "error.passwordChangeDTO", "New password and confirmation do not match.");
            return "account/changePassManager";
        }

        try {

            if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
                DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
                userDetails = new CustomUserDetails(oauth2User);
            } else if (authentication.getPrincipal() instanceof UserDetails) {
                userDetails = (UserDetails) authentication.getPrincipal();
            } else {
                throw new IllegalStateException("Unknown principal type");
            }

            String username = userDetails.getUsername();
            userService.changePassword(username, passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("success", "Password change successful for user: " + username);
            return "redirect:/manager/changePass?success";

        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            result.rejectValue("currentPassword", "error.passwordChangeDTO", "Current password is incorrect.");
            return "account/changePassManager";
        }
    }

    
    @GetMapping("/forgotPass/customer")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("passwordForgotDTO", new PasswordForgotDTO());
        return "account/forgotPass"; 
    }

    @PostMapping("/forgotPass/customer")
    public String processForgotPassword(@ModelAttribute("passwordForgotDTO") PasswordForgotDTO passwordForgotDTO,
                                        BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation error occurred.");
            return "redirect:/forgotPass/customer";
        }

        try {
            userService.forgotPassword(passwordForgotDTO.getEmail());
            redirectAttributes.addFlashAttribute("success", "Password reset email sent successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User with this email was not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process your request. Please try again.");
        }

        return "redirect:/forgotPass/customer";
    }
    
    private String registerUser(UserRegistrationDTO registrationDTO, BindingResult result, String view, String redirectUrl) {
        if (result.hasErrors()) {
            log.warn("Registration failed due to validation errors: {}", result.getAllErrors());
            return view;
        }
        try {
            userService.saveUser(registrationDTO);
            log.info("User registration successful: {}", registrationDTO.getUsername());
        } catch (UsernameAlreadyExistsException e) {
            log.error("Registration failed due to username already exists: {}", e.getMessage());
            result.rejectValue("username", "user.exists", e.getMessage());
            result.rejectValue("email", "email.exists", e.getMessage());
            return view;
        }
        return "redirect:/" + redirectUrl + "?success";
    }

    private String getAuthenticatedUserRedirectUrl(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String redirectUrl = RedirectUtil.getRedirectUrl(authentication);
            if (redirectUrl != null) {
                return "redirect:" + redirectUrl;
            }
        }
        return null;
    }

}

