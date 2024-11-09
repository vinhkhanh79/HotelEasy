package com.datn.tourhotel.security;

import com.datn.tourhotel.model.Role;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.repository.UserRepository;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email =null;
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String pictureUrl =null;
        String name =null;
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            pictureUrl = (String) attributes.get("picture");
        } else if ("facebook".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            // Kiểm tra kiểu dữ liệu để lấy URL của ảnh
            if (attributes.get("picture") instanceof Map) {
                Map<String, Object> pictureMap = (Map<String, Object>) attributes.get("picture");
                if (pictureMap.get("data") instanceof Map) {
                    Map<String, Object> pictureData = (Map<String, Object>) pictureMap.get("data");
                    pictureUrl = (String) pictureData.get("url");
                }
            }
        }
        System.out.println(email);
        System.out.println(registrationId);
        System.out.println(pictureUrl);
        System.out.println(name);
        User existingUser = userRepository.findByEmail(email);

        if(existingUser == null) {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setImg(pictureUrl);
            user.setCreatedDate(LocalDateTime.now());
            user.setBirthday(null);
            user.setRole(new Role(2L,RoleType.CUSTOMER));
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(email));
            user.setLastName(name);
            user.setPhone(null);

            userRepository.save(user);
        }

        return oAuth2User;
    }
}
