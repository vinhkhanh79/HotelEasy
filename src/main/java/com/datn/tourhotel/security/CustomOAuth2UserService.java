package com.datn.tourhotel.security;

import com.datn.tourhotel.model.Role;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.repository.UserRepository;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
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
            RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
            String password = randomStringGenerator.generateRandomString();
            System.out.println(password);
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setImg(pictureUrl);
            user.setCreatedDate(LocalDateTime.now());
            user.setBirthday(null);
            user.setRole(new Role(2L,RoleType.CUSTOMER));
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setLastName(name);
            user.setPhone(null);
            userRepository.save(user);
            try {
                sendWelcomeEmail(email,email,password);
            }catch (Exception e){
                throw new MailException("send mail fail") {};
            }
        }

        return oAuth2User;
    }

    public void sendWelcomeEmail(String toEmail, String email, String password) throws MailException {
        // Tạo tiêu đề và nội dung HTML cho email
        String subject = "🎉 Chào mừng bạn đến với trang web của chúng tôi!";
        String body = String.format(
                "<html>" +
                        "<head>" +
                        "<style>" +
                        "body {" +
                        "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +  // Chọn font chữ hiện đại, dễ đọc
                        "    background-color: #f9f9f9;" +  // Màu nền sáng, dễ chịu
                        "    margin: 0;" +
                        "    padding: 20px;" +
                        "    text-align: center;" +
                        "    color: #333;" +
                        "}" +
                        ".email-container {" +
                        "    background-color: #ffffff;" +  // Nền trắng cho nội dung email
                        "    max-width: 600px;" +
                        "    margin: 50px auto;" +
                        "    padding: 30px;" +
                        "    border-radius: 10px;" +
                        "    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);" + // Hiệu ứng shadow làm rõ viền
                        "    border: 1px solid #e1e1e1;" +  // Viền sáng để tạo độ phân tách với nền
                        "}" +
                        ".header h2 {" +
                        "    font-size: 36px;" +
                        "    color: #4CAF50;" +  // Màu xanh lá cây cho tiêu đề, tạo sự tươi mới
                        "    margin-bottom: 20px;" +
                        "    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);" +  // Hiệu ứng đổ bóng nhẹ cho tiêu đề
                        "}" +
                        ".content {" +
                        "    font-size: 18px;" +
                        "    line-height: 1.8;" +
                        "    color: #555;" +  // Màu chữ dễ đọc
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
                        "    content: '✔';" +  // Dấu kiểm xanh cho mỗi mục
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
                        "    background-color: #45a049;" +  // Hiệu ứng hover làm nền chuyển màu
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
                        "<h2>Chào mừng bạn đến với trang web của chúng tôi!</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Xin chào <strong>%s</strong>,</p>" +
                        "<p>Chúng tôi rất vui mừng thông báo rằng tài khoản của bạn đã được đăng ký thành công trên trang web của chúng tôi. Hãy bắt đầu khám phá các tính năng tuyệt vời và tiện ích mà chúng tôi cung cấp!</p>" +
                        "<p><strong>Thông tin đăng nhập nếu muốn sử dụng tài khoản và mật khẩu:</strong></p>" +
                        "<ul>" +
                        "<li><strong>username:</strong> %s</li>" +
                        "<li><strong>password:</strong> %s</li>" +
                        "</ul>" +
                        "<p>Bạn có thể thay đổi mật khẩu bất cứ lúc nào từ trang cài đặt của tài khoản.</p>" +
                        "<p>Đừng quên truy cập vào trang web để trải nghiệm các tính năng mới nhất!</p>" +
                        "<a href='http://localhost:8080' class='button'>Truy cập trang web ngay</a>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Chúc bạn có những trải nghiệm tuyệt vời cùng chúng tôi!<br>Đội ngũ hỗ trợ luôn sẵn sàng hỗ trợ bạn khi cần.</p>" +
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
