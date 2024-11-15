package com.datn.tourhotel.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.datn.tourhotel.exception.UsernameAlreadyExistsException;
import com.datn.tourhotel.model.*;
import com.datn.tourhotel.model.dto.ResetPasswordDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.UserRegistrationDTO;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.repository.CustomerRepository;
import com.datn.tourhotel.repository.HotelManagerRepository;
import com.datn.tourhotel.repository.RoleRepository;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public User saveUser(UserRegistrationDTO registrationDTO) {
        log.info("Attempting to save a new user: {}", registrationDTO.getUsername());

        Optional<User> existingUser = Optional.ofNullable(userRepository.findByUsername(registrationDTO.getUsername()));
        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException("This username is already registered!");
        }
        
        Optional<User> existingUserByEmail = Optional.ofNullable(userRepository.findByEmail(registrationDTO.getEmail()));
        if (existingUserByEmail.isPresent()) {
            throw new UsernameAlreadyExistsException("This email is already registered!");
        }

        User user = mapRegistrationDtoToUser(registrationDTO);
        // Set role mặc định nếu không có
        if (registrationDTO.getRoleType() == null) {
            registrationDTO.setRoleType(RoleType.CUSTOMER);
        }

        // Tạo relationship tương ứng
        try {
        User savedUser = userRepository.save(user);
        if (RoleType.CUSTOMER.equals(registrationDTO.getRoleType())) {
            Customer customer = Customer.builder().user(user).build();
            customerRepository.save(customer);
        } else if (RoleType.HOTEL_MANAGER.equals(registrationDTO.getRoleType())) {
            HotelManager hotelManager = HotelManager.builder().user(user).build();
            hotelManagerRepository.save(hotelManager);
        }

        log.info("Successfully saved new user: {}", registrationDTO.getUsername());
        return savedUser;
	    } catch (Exception e) {
	        log.error("Error during user registration: ", e);
	        throw e;
	    }
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
    
    @Override
    public UserDTO findUserDTOByUsername(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return mapUserToUserDto(user);
    }

    @Override
    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));

        return mapUserToUserDto(user);
    }

    @Override
    public List<UserDTO> findAllUsers() {
        List<User> userList = userRepository.findAllActiveUsers();

        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : userList) {
            UserDTO userDTO = mapUserToUserDto(user);
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }
    
    public List<UserDTO> searchUsersByUsername(String username) {
        List<User> userList = userRepository.findByUsernameContainingIgnoreCase(username);
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : userList) {
            userDTOList.add(mapUserToUserDto(user));
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public void updateUser(UserDTO userDTO, MultipartFile multipartFile) throws IOException {
        log.info("Attempting to update user with ID: {}", userDTO.getId());

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole().getRoleType() == RoleType.ADMIN) {
            throw new IllegalStateException("Cannot modify ADMIN role");
        }

        if (userDTO.getRoleType() == RoleType.ADMIN) {
            throw new IllegalStateException("Cannot assign ADMIN role");
        }

        if (usernameExistsAndNotSameUser(userDTO.getUsername(), user.getId())) {
            throw new UsernameAlreadyExistsException("This username is already registered!");
        }

        String url = user.getImg();
        if (multipartFile != null && !multipartFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
            url = uploadResult.get("url").toString();
        }

        setFormattedDataToUser(user, userDTO, url);

        if (userDTO.getRoleType() != user.getRole().getRoleType()) {
            Role newRole = roleRepository.findByRoleType(userDTO.getRoleType());
            if (newRole == null) {
                throw new IllegalStateException("Role not found");
            }

            if (user.getCustomer() != null) {
                Long customerId = user.getCustomer().getId();
                user.setCustomer(null);
                customerRepository.deleteById(customerId);
            }

            if (user.getHotelManager() != null) {
                Long managerId = user.getHotelManager().getId();
                user.setHotelManager(null);
                hotelManagerRepository.deleteById(managerId);
            }

            user.setRole(newRole);
            user = userRepository.save(user);

            if (userDTO.getRoleType() == RoleType.CUSTOMER) {
                Customer newCustomer = new Customer();
                newCustomer.setUser(user);
                customerRepository.save(newCustomer);
            } else if (userDTO.getRoleType() == RoleType.HOTEL_MANAGER) {
                HotelManager newManager = new HotelManager();
                newManager.setUser(user);
                hotelManagerRepository.save(newManager);
            }
        } else {
            userRepository.save(user);
        }

        log.info("Successfully updated existing user with ID: {}", userDTO.getId());
    }


    @Override
    @Transactional
    public void updateLoggedInUser(UserDTO userDTO, MultipartFile multipartFile) throws IOException {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername);
        log.info("Attempting to update logged in user with ID: {}", loggedInUser.getId());

        if (usernameExistsAndNotSameUser(userDTO.getUsername(), loggedInUser.getId())) {
            throw new UsernameAlreadyExistsException("This username is already registered!");
        }

        // Chỉ upload ảnh mới nếu người dùng chọn file
        String url = loggedInUser.getImg(); // Giữ nguyên ảnh cũ
        if (multipartFile != null && !multipartFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
            url = uploadResult.get("url").toString();
        }

        setFormattedDataToUser(loggedInUser, userDTO, url);
        userRepository.save(loggedInUser);
        log.info("Successfully updated logged in user with ID: {}", loggedInUser.getId());

        // Cập nhật token xác thực
        updateAuthentication(userDTO);
    }

    
    @Override
    @Transactional
    public void updateUserImage(Long userId, String imageUrl) {
        log.info("Updating image for user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setImg(imageUrl);
        userRepository.save(user);
        log.info("Successfully updated image for user with ID: {}", userId);
    }


    @Override
    public void deleteUserById(Long id) {
    	User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        log.info("Attempting to delete user with ID: {}", id);
        user.setDeleted(true);
        userRepository.save(user);
        
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public User resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = auth.getName();

        User user = userRepository.findByUsername(loggedInUsername);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!passwordEncoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        return userRepository.save(user);
    }

    private User mapRegistrationDtoToUser(UserRegistrationDTO registrationDTO) {
        Role userRole = roleRepository.findByRoleType(registrationDTO.getRoleType());
        if (userRole == null) {
            // Nếu không tìm thấy role, mặc định là CUSTOMER
            userRole = roleRepository.findByRoleType(RoleType.CUSTOMER);
        }

        // Kiểm tra mật khẩu xác nhận
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        // Log để debug
        log.info("Creating new user with username: {}", registrationDTO.getUsername());
        log.info("Role type: {}", registrationDTO.getRoleType());
        log.info("User role found: {}", userRole);
        return User.builder()
        		.email(registrationDTO.getEmail())
                .username(registrationDTO.getUsername().trim())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .name(formatText(registrationDTO.getName()))
                .lastName(formatText(registrationDTO.getLastName()))
                .phone(registrationDTO.getPhone())
                .birthday(registrationDTO.getBirthday())
                .img("https://res.cloudinary.com/dliwvet1v/image/upload/v1729755833/user/jvs7bxrioi3iro4fk1c1.png")
                .role(userRole)
                .build();
    }

    private UserDTO mapUserToUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .img(user.getImg())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .role(user.getRole())
                .build();
    }

    private boolean usernameExistsAndNotSameUser(String username, Long userId) {
        Optional<User> existingUserWithSameUsername = Optional.ofNullable(userRepository.findByUsername(username));
        return existingUserWithSameUsername.isPresent() && !existingUserWithSameUsername.get().getId().equals(userId);
    }

    private String formatText(String text) {
        return StringUtils.capitalize(text.trim());
    }

    private void setFormattedDataToUser(User user, UserDTO userDTO, String url) {
        user.setUsername(formatText(userDTO.getUsername()));
        user.setName(formatText(userDTO.getName()));
        user.setLastName(formatText(userDTO.getLastName()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(formatText(userDTO.getPhone()));
        user.setBirthday(userDTO.getBirthday());
        user.setImg(url);
    }
    
    @Override
    public void changePassword(String username, String currentPassword, String newPassword) throws Exception {
    	User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
    	if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
    		throw new Exception("Current password is incorrect");
    	}
    	user.setPassword(passwordEncoder.encode(newPassword));
    	userRepository.save(user);		
    }
    
    @Override
    public void forgotPassword(String email) {
        // Tìm user bằng email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }

        // Tạo mật khẩu mới ngẫu nhiên
        String newPassword = UUID.randomUUID().toString().substring(0, 8); // Mật khẩu có độ dài 8 ký tự

        // Mã hóa mật khẩu mới trước khi lưu
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Lưu mật khẩu mới đã mã hóa vào user
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Gửi email với mật khẩu mới
        sendNewPasswordEmail(user.getEmail(), newPassword);
    }

    private void sendNewPasswordEmail(String email, String newPassword) {
    	String subject = "🔑 Reset Your Password";

    	String body = String.format(
    	        "<html>" +
    	                "<head>" +
    	                "<style>" +
    	                "body {" +
    	                "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +
    	                "    background-color: #f9f9f9;" +
    	                "    margin: 0;" +
    	                "    padding: 20px;" +
    	                "    text-align: center;" +
    	                "    color: #333;" +
    	                "}" +
    	                ".email-container {" +
    	                "    background-color: #ffffff;" +
    	                "    max-width: 600px;" +
    	                "    margin: 50px auto;" +
    	                "    padding: 30px;" +
    	                "    border-radius: 10px;" +
    	                "    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);" +
    	                "    border: 1px solid #e1e1e1;" +
    	                "}" +
    	                ".header h2 {" +
    	                "    font-size: 36px;" +
    	                "    color: #FF5722;" + // Orange color for the header
    	                "    margin-bottom: 20px;" +
    	                "    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);" +
    	                "}" +
    	                ".content {" +
    	                "    font-size: 18px;" +
    	                "    line-height: 1.8;" +
    	                "    color: #555;" +
    	                "    margin-bottom: 20px;" +
    	                "    text-align: left;" +
    	                "}" +
    	                ".content p {" +
    	                "    margin: 15px 0;" +
    	                "}" +
    	                ".button {" +
    	                "    background-color: #FF5722;" +
    	                "    color: white;" +
    	                "    padding: 12px 30px;" +
    	                "    border-radius: 8px;" +
    	                "    font-size: 20px;" +
    	                "    font-weight: bold;" +
    	                "    text-decoration: none;" +
    	                "    transition: background-color 0.3s ease;" +
    	                "    display: inline-block;" +
    	                "    margin-top: 20px;" +
    	                "}" +
    	                ".button:hover {" +
    	                "    background-color: #E64A19;" +
    	                "}" +
    	                ".footer {" +
    	                "    font-size: 14px;" +
    	                "    color: #777;" +
    	                "    margin-top: 40px;" +
    	                "    text-align: center;" +
    	                "    line-height: 1.6;" +
    	                "}" +
    	                "</style>" +
    	                "</head>" +
    	                "<body>" +
    	                "<div class='email-container'>" +
    	                "<div class='header'>" +
    	                "<h2>Password Reset Request</h2>" +
    	                "</div>" +
    	                "<div class='content'>" +
    	                "<p>Hello <strong>%s</strong>,</p>" +
    	                "<p>We've received a request to reset the password for your account.</p>" +
    	                "<p><strong>Your new temporary password is:</strong></p>" +
    	                "<ul>" +
    	                "<li><strong>password:</strong> %s</li>" +
    	                "</ul>" +
    	                "<p>Please use this password to log in and make sure to change it from your account settings for security purposes.</p>" +
    	                "<a href='http://localhost:8080/login' class='button'>Log In Now</a>" +
    	                "</div>" +
    	                "<div class='footer'>" +
    	                "<p>If you didn't request this change, please contact our support team.<br>Thank you for using our service!</p>" +
    	                "</div>" +
    	                "</div>" +
    	                "</body>" +
    	                "</html>", email, newPassword
    	);

    	MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setText(body, true);  // true indicates HTML format
            helper.setTo(email);
            helper.setSubject(subject);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void updateAuthentication(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.getUsername());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleType().name()));

        UserDetails newUserDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
                newUserDetails,
                null,
                newUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

}
