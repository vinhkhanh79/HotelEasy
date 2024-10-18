package com.datn.tourhotel.service;

import java.util.List;

import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.ResetPasswordDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.UserRegistrationDTO;

public interface UserService {

    User saveUser(UserRegistrationDTO registrationDTO);

    // For registration
    User findUserByUsername(String username);
    
    User findUserByEmail(String email);

    UserDTO findUserDTOByUsername(String username);

    UserDTO findUserById(Long id);

    List<UserDTO> findAllUsers();
    
    boolean usernameExists(String username);
    
    boolean emailExists(String email);
    
    void updateUser(UserDTO userDTO);

    void updateLoggedInUser(UserDTO userDTO);

    void deleteUserById(Long id);
    
    void changePassword(String username, String currentPassword, String newPassword) throws Exception;
    
    void forgotPassword(String email);

    User resetPassword(ResetPasswordDTO resetPasswordDTO);

}
