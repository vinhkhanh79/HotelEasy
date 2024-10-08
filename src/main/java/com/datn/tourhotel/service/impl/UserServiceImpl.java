package com.datn.tourhotel.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

import java.util.ArrayList;
import java.util.List;
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

        if (RoleType.CUSTOMER.equals(registrationDTO.getRoleType())) {
            Customer customer = Customer.builder().user(user).build();
            customerRepository.save(customer);
        } else if (RoleType.HOTEL_MANAGER.equals(registrationDTO.getRoleType())) {
            HotelManager hotelManager = HotelManager.builder().user(user).build();
            hotelManagerRepository.save(hotelManager);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully saved new user: {}", registrationDTO.getUsername());
        return savedUser;
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
        List<User> userList = userRepository.findAll();

        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : userList) {
            UserDTO userDTO = mapUserToUserDto(user);
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public void updateUser(UserDTO userDTO) {
        log.info("Attempting to update user with ID: {}", userDTO.getId());

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (usernameExistsAndNotSameUser(userDTO.getUsername(), user.getId())) {
            throw new UsernameAlreadyExistsException("This username is already registered!");
        }

        setFormattedDataToUser(user, userDTO);
        userRepository.save(user);
        log.info("Successfully updated existing user with ID: {}", userDTO.getId());
    }

    @Override
    @Transactional
    public void updateLoggedInUser(UserDTO userDTO) {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername);
        log.info("Attempting to update logged in user with ID: {}", loggedInUser.getId());

        if (usernameExistsAndNotSameUser(userDTO.getUsername(), loggedInUser.getId())) {
            throw new UsernameAlreadyExistsException("This username is already registered!");
        }

        setFormattedDataToUser(loggedInUser, userDTO);
        userRepository.save(loggedInUser);
        log.info("Successfully updated logged in user with ID: {}", loggedInUser.getId());

        // Create new authentication token
        updateAuthentication(userDTO);
    }

    @Override
    public void deleteUserById(Long id) {
        log.info("Attempting to delete user with ID: {}", id);
        userRepository.deleteById(id);
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
        return User.builder()
        		.email(registrationDTO.getEmail())
                .username(registrationDTO.getUsername().trim())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .name(formatText(registrationDTO.getName()))
                .lastName(formatText(registrationDTO.getLastName()))
                .phone(registrationDTO.getPhone())
                .birthday(registrationDTO.getBirthday())
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

    private void setFormattedDataToUser(User user, UserDTO userDTO) {
        user.setUsername(formatText(userDTO.getUsername()));
        user.setName(formatText(userDTO.getName()));
        user.setLastName(formatText(userDTO.getLastName()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(formatText(userDTO.getPhone()));
        user.setBirthday(userDTO.getBirthday());
        user.setImg(userDTO.getImg());
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
        // Find user by email
        User user = userRepository.findByEmail(email);
        if (email == null) {
        	throw new UsernameNotFoundException("User with email " + email + " not found");
        }

        // Generate a random reset token
        String resetToken = UUID.randomUUID().toString();

        // Save the token to the user entity (assuming you have a field for it)
        user.setPassword(resetToken);;
        userRepository.save(user);

        // Send email with the reset token
        sendResetTokenEmail(user.getEmail(), resetToken);
    }

    private void sendResetTokenEmail(String email, String resetPassword) {
        String subject = "Password Reset Request";
        String text = "To reset your password, click the link below:\n" + 
                      "http://localhost:8080/reset-password?token=" + resetPassword;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
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
