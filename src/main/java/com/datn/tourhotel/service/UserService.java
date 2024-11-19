package com.datn.tourhotel.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.ResetPasswordDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.UserRegistrationDTO;

public interface UserService {

    User saveUser(UserRegistrationDTO registrationDTO);
    
    User saveUser2(UserRegistrationDTO registrationDTO, MultipartFile multipartFile) throws IOException;

    // For registration
    User findUserByUsername(String username);
    
    User findUserByEmail(String email);

    UserDTO findUserDTOByUsername(String username);

    UserDTO findUserById(Long id);

    List<UserDTO> findAllUsers();
    
    List<UserDTO> searchUsersByUsername(String username);
    
    boolean usernameExists(String username);
    
    boolean emailExists(String email);
    
    void updateUser(UserDTO userDTO, MultipartFile multipartFile) throws IOException;
    
    void updateUserImage(Long userId, String imageUrl);

    void updateLoggedInUser(UserDTO userDTO, MultipartFile multipartFile) throws IOException;

    void deleteUserById(Long id);
    
    void changePassword(String username, String currentPassword, String newPassword) throws Exception;
    
    void forgotPassword(String email);

    User resetPassword(ResetPasswordDTO resetPasswordDTO);

}
