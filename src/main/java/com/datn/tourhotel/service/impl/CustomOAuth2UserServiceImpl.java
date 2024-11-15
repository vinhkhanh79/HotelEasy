package com.datn.tourhotel.service.impl;

import com.datn.tourhotel.model.Customer;
import com.datn.tourhotel.model.Role;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.repository.CustomerRepository;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.security.RandomStringGenerator;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService {
	@PersistenceContext
	private EntityManager entityManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final CustomerRepository customerRepository;
    
    public CustomOAuth2UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.customerRepository = customerRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String pictureUrl = null;
        String name = null;

        // Xử lý đăng nhập bằng Google
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            pictureUrl = (String) attributes.get("picture");
        } 
        // Xử lý đăng nhập bằng Facebook
        else if ("facebook".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");

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

        // Tạo người dùng mới nếu chưa tồn tại
        if (existingUser == null) {
            RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
            String password = randomStringGenerator.generateRandomString();
            System.out.println(password);
            
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setImg(pictureUrl);
            user.setCreatedDate(LocalDateTime.now());
            user.setBirthday(null);
            user.setRole(new Role(2L, RoleType.CUSTOMER)); // Gán vai trò CUSTOMER
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu ngẫu nhiên
            user.setLastName(name);
            user.setPhone(null);
            user.setDeleted(false);
            
            // Lưu người dùng mới vào cơ sở dữ liệu
            userRepository.save(user);
            
            try {
                sendWelcomeEmail(email, email, password); // Gửi email chào mừng
            } catch (Exception e) {
                throw new MailException("send mail fail") {};
            }
        } else {
            // Nếu người dùng đã tồn tại, đảm bảo vai trò vẫn là CUSTOMER
            if (existingUser.getRole() == null || !existingUser.getRole().getRoleType().equals(RoleType.CUSTOMER)) {
                existingUser.setRole(new Role(2L, RoleType.CUSTOMER));
                userRepository.save(existingUser);
            }
        }

        // Lấy danh sách quyền hiện tại của người dùng OAuth2
        List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());

        // Thêm quyền ROLE_CUSTOMER vào danh sách quyền
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        
        saveCustomerIfNotExists(email);

        // Trả về đối tượng DefaultOAuth2User với quyền được cập nhật
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "name");
    }
    private String getEmailFromAttributes(Map<String, Object> attributes, String registrationId) {
        if ("google".equals(registrationId) || "facebook".equals(registrationId)) {
            return (String) attributes.get("email");
        }
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
    }

    private String extractFacebookPictureUrl(Map<String, Object> attributes) {
        if (attributes.get("picture") instanceof Map) {
            Map<String, Object> pictureMap = (Map<String, Object>) attributes.get("picture");
            if (pictureMap.get("data") instanceof Map) {
                Map<String, Object> pictureData = (Map<String, Object>) pictureMap.get("data");
                return (String) pictureData.get("url");
            }
        }
        return null;
    }
    @Transactional
    protected void saveCustomerIfNotExists(String email) {
        User us = userRepository.findByUsername(email);
        if (us == null) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }
        User managedUser = entityManager.merge(us);

        // Check if Customer already exists
        Optional<Customer> existingCustomer = customerRepository.findByUserId(managedUser.getId());
        if (existingCustomer.isEmpty()) {
            Customer cs = new Customer(null, managedUser, null);
            customerRepository.save(cs);
        }
    }

    public void sendWelcomeEmail(String toEmail, String email, String password) throws MailException {
        // Tạo tiêu đề và nội dung HTML cho email
    	String subject = "🎉 Welcome to our website!";
    	String body = String.format(
    	    "<html>" +
    	        "<head>" +
    	        "<style>" +
    	        "body {" +
    	        "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +  // Modern, easy-to-read font
    	        "    background-color: #f9f9f9;" +  // Light, comfortable background color
    	        "    margin: 0;" +
    	        "    padding: 20px;" +
    	        "    text-align: center;" +
    	        "    color: #333;" +
    	        "}" +
    	        ".email-container {" +
    	        "    background-color: #ffffff;" +  // White background for email content
    	        "    max-width: 600px;" +
    	        "    margin: 50px auto;" +
    	        "    padding: 30px;" +
    	        "    border-radius: 10px;" +
    	        "    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);" + // Shadow effect to highlight the border
    	        "    border: 1px solid #e1e1e1;" +  // Light border for separation from the background
    	        "}" +
    	        ".header h2 {" +
    	        "    font-size: 36px;" +
    	        "    color: #4CAF50;" +  // Green title color, creating freshness
    	        "    margin-bottom: 20px;" +
    	        "    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);" +  // Light shadow effect for the title
    	        "}" +
    	        ".content {" +
    	        "    font-size: 18px;" +
    	        "    line-height: 1.8;" +
    	        "    color: #555;" +  // Easy-to-read text color
    	        "    margin-bottom: 20px;" +
    	        "    text-align: left;" +
    	        "}" +
    	        ".content p {" +
    	        "    margin: 15px 0;" +
    	        "}" +
    	        ".content ul {" +
    	        "    list-style-type: none;" +
    	        "    padding-left: 0;" +
    	        "    font-size: 18px;" +
    	        "    color: #444;" +
    	        "}" +
    	        ".content li {" +
    	        "    margin: 8px 0;" +
    	        "    padding-left: 20px;" +
    	        "    text-indent: -20px;" +
    	        "}" +
    	        ".content li::before {" +
    	        "    content: '✔';" +  // Green checkmark for each item
    	        "    color: #4CAF50;" +
    	        "    padding-right: 10px;" +
    	        "}" +
    	        ".button {" +
    	        "    background-color: #4CAF50;" +
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
    	        "    background-color: #45a049;" +  // Hover effect changes background color
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
    	        "<h2>Welcome to our website!</h2>" +
    	        "</div>" +
    	        "<div class='content'>" +
    	        "<p>Hello <strong>%s</strong>,</p>" +
    	        "<p>We are excited to announce that your account has been successfully registered on our website. Start exploring the amazing features and conveniences we offer!</p>" +
    	        "<p><strong>Login information if you wish to use your account and password:</strong></p>" +
    	        "<ul>" +
    	        "<li><strong>username:</strong> %s</li>" +
    	        "<li><strong>password:</strong> %s</li>" +
    	        "</ul>" +
    	        "<p>You can change your password at any time from the account settings page.</p>" +
    	        "<p>Don't forget to visit the website to experience the latest features!</p>" +
    	        "<a href='http://localhost:8080' class='button'>Visit the website now</a>" +
    	        "</div>" +
    	        "<div class='footer'>" +
    	        "<p>We wish you a wonderful experience with us!<br>Our support team is always ready to assist you when needed.</p>" +
    	        "</div>" +
    	        "</div>" +
    	        "</body>" +
    	        "</html>", email, email, password
    	);

        // Tạo đối tượng MimeMessage để gửi email với HTML
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);  // true để hỗ trợ gửi email HTML

            // Cấu hình email
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // Đảm bảo gửi email dưới dạng HTML

            // Gửi email
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MailException("Có lỗi khi gửi email!") {};
        }
    }
}
