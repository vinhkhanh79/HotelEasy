package com.datn.tourhotel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import com.cloudinary.Cloudinary;
import com.datn.tourhotel.exception.HotelAlreadyExistsException;
import com.datn.tourhotel.exception.UsernameAlreadyExistsException;
import com.datn.tourhotel.model.Hotel;
import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.CommentDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelRegistrationDTO;
import com.datn.tourhotel.model.dto.RoomDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.VoucherDTO;
import com.datn.tourhotel.model.enums.RoomType;
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.service.BookingService;
import com.datn.tourhotel.service.CommentService;
import com.datn.tourhotel.service.CustomerService;
import com.datn.tourhotel.service.ExcelExportService;
import com.datn.tourhotel.service.HotelManagerService;
import com.datn.tourhotel.service.HotelService;
import com.datn.tourhotel.service.PaymentService;
import com.datn.tourhotel.service.PostService;
import com.datn.tourhotel.service.UserService;
import com.datn.tourhotel.service.VoucherService;
import com.datn.tourhotel.model.Voucher;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final HotelManagerService hotelManagerService;
    private final ExcelExportService excelExportService;
    private final CommentService commentService;
    private final PostService postService;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final VoucherService voucherService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model, @RequestParam(name = "earningsPeriod", required = false, defaultValue = "total") String period) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	Long managerId = getCurrentManagerId();
    	Long customerCount = customerService.getCustomerCount();
        model.addAttribute("customerCount", customerCount);

        Long countHotelsByManager = hotelManagerService.countHotelsByManager(managerId);
        model.addAttribute("countHotelsByManager", countHotelsByManager);
        
     // Fetch earnings for different periods
        BigDecimal earningsToday = paymentService.getEarningsByPeriod(managerId, "day");
        BigDecimal earningsThisWeek = paymentService.getEarningsByPeriod(managerId, "week");
        BigDecimal earningsThisMonth = paymentService.getEarningsByPeriod(managerId, "month");
        BigDecimal earningsThisYear = paymentService.getEarningsByPeriod(managerId, "year");
        BigDecimal earningsTotal = paymentService.getEarningsByPeriod(managerId, "total");

        // Add earnings data to the model for use in the frontend
        model.addAttribute("earningsToday", earningsToday);
        model.addAttribute("earningsThisWeek", earningsThisWeek);
        model.addAttribute("earningsThisMonth", earningsThisMonth);
        model.addAttribute("earningsThisYear", earningsThisYear);
        model.addAttribute("earningsTotal", earningsTotal);
        
        List<BigDecimal> earningsPerDayInYear = paymentService.getEarningsPerDayInYear(managerId);
        model.addAttribute("earningsPerDayInYear", earningsPerDayInYear);
        
     // Tổng thu nhập theo period
        BigDecimal getEarningsByPeriod = paymentService.getEarningsByPeriod(managerId, period);
        if (getEarningsByPeriod == null) {
            getEarningsByPeriod = BigDecimal.ZERO;
        }
     // Format thu nhập theo định dạng "1,500,000"
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedEarnings = decimalFormat.format(getEarningsByPeriod);
        model.addAttribute("formattedEarnings", formattedEarnings);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        List<Object[]> topCustomers = paymentService.getTopCustomersByEarnings();
        List<String> customerNames = new ArrayList<>();
        List<BigDecimal> customerEarnings = new ArrayList<>();

        // Chỉ lấy 10 khách hàng đầu tiên
        int count = Math.min(topCustomers.size(), 10);
        for (int i = 0; i < count; i++) {
            customerNames.add((String) topCustomers.get(i)[0]);
            customerEarnings.add((BigDecimal) topCustomers.get(i)[1]);
        }

        model.addAttribute("topCustomerNames", customerNames);
        model.addAttribute("topCustomerEarnings", customerEarnings);
        return "hotelmanager/dashboard";
    }
    @GetMapping("/report")
    public String report(HttpServletRequest request, Model model, @RequestParam(name = "earningsPeriod", required = false, defaultValue = "total") String period) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	Long managerId = getCurrentManagerId();
    	Long customerCount = customerService.getCustomerCount();
        model.addAttribute("customerCount", customerCount);

        Long countHotelsByManager = hotelManagerService.countHotelsByManager(managerId);
        model.addAttribute("countHotelsByManager", countHotelsByManager);
        
     // Fetch earnings for different periods
        BigDecimal earningsToday = paymentService.getEarningsByPeriod(managerId, "day");
        BigDecimal earningsThisWeek = paymentService.getEarningsByPeriod(managerId, "week");
        BigDecimal earningsThisMonth = paymentService.getEarningsByPeriod(managerId, "month");
        BigDecimal earningsThisYear = paymentService.getEarningsByPeriod(managerId, "year");
        BigDecimal earningsTotal = paymentService.getEarningsByPeriod(managerId, "total");
        
     // Lấy danh sách top khách sạn theo đánh giá 5 sao của manager đó
        List<Object[]> topHotelsWithFiveStarRatings = commentService.getTopHotelsWithFiveStarRatingsByManager(managerId);

        List<String> hotelNames = topHotelsWithFiveStarRatings.stream()
                .map(result -> (String) result[0])
                .collect(Collectors.toList());

        List<Long> fiveStarCounts = topHotelsWithFiveStarRatings.stream()
                .map(result -> (Long) result[1])
                .collect(Collectors.toList());

        model.addAttribute("topHotelNames", hotelNames);
        model.addAttribute("fiveStarCounts", fiveStarCounts);

        // Add earnings data to the model for use in the frontend
        model.addAttribute("earningsToday", earningsToday);
        model.addAttribute("earningsThisWeek", earningsThisWeek);
        model.addAttribute("earningsThisMonth", earningsThisMonth);
        model.addAttribute("earningsThisYear", earningsThisYear);
        model.addAttribute("earningsTotal", earningsTotal);
        
        List<BigDecimal> earningsPerDayInYear = paymentService.getEarningsPerDayInYear(managerId);
        model.addAttribute("earningsPerDayInYear", earningsPerDayInYear);
        
     // Tổng thu nhập theo period
        BigDecimal getEarningsByPeriod = paymentService.getEarningsByPeriod(managerId, period);
        if (getEarningsByPeriod == null) {
            getEarningsByPeriod = BigDecimal.ZERO;
        }
     // Format thu nhập theo định dạng "1,500,000"
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedEarnings = decimalFormat.format(getEarningsByPeriod);
        model.addAttribute("formattedEarnings", formattedEarnings);
        
        List<BigDecimal> earningsPerWeekInYear = paymentService.getEarningsPerWeekInYear(managerId);
        model.addAttribute("earningsPerWeekInYear", earningsPerWeekInYear);
        
        List<BigDecimal> earningsPerMonthInYear = paymentService.getEarningsPerMonthInYear(managerId);
        model.addAttribute("earningsPerMonthInYear", earningsPerMonthInYear);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        List<Object[]> topCustomers = paymentService.getTopCustomersByEarnings();
        List<String> customerNames = new ArrayList<>();
        List<BigDecimal> customerEarnings = new ArrayList<>();

        // Chỉ lấy 10 khách hàng đầu tiên
        int count = Math.min(topCustomers.size(), 10);
        for (int i = 0; i < count; i++) {
            customerNames.add((String) topCustomers.get(i)[0]);
            customerEarnings.add((BigDecimal) topCustomers.get(i)[1]);
        }

        model.addAttribute("topCustomerNames", customerNames);
        model.addAttribute("topCustomerEarnings", customerEarnings);
        return "hotelmanager/report";
    }
    @GetMapping("/posts")
    public String listPosts(Model model, HttpServletRequest request) {
        // Optionally use messages for localization (not necessary for post listing)
        String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        
        // Get the current username
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Get the current user (manager)
        User currentUser = userRepository.findByUsername(currentUsername);
        
        // Get the list of posts created by the current user (manager)
        List<Post> postList = postService.getPostsByCreatedBy(currentUser);
        
        // Add the post list to the model
        model.addAttribute("posts", postList);
        
        // Add current username (if needed for display or authentication)
        model.addAttribute("currentUsername", currentUsername);
        
        // Return the name of the view to display (admin/posts.html)
        return "hotelmanager/posts";
    }


 // Hiển thị form thêm bài post
    @GetMapping("/posts/add")
    public String showAddPostForm(Model model) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("post", new Post());
        return "hotelmanager/posts-add"; // Đây là file HTML hiển thị form
    }

    @PostMapping("/posts/add")
    public String savePost(@ModelAttribute("post") Post post,
                           @RequestParam(value = "imageFile", required = false) MultipartFile image1,
                           @RequestParam(value = "imageFile2", required = false) MultipartFile image2,
                           @RequestParam(value = "imageFile3", required = false) MultipartFile image3,
                           RedirectAttributes redirectAttributes) {
        try {
            // Upload 3 ảnh lên Cloudinary
            if (image1 != null && !image1.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(image1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                post.setImg(uploadResult.get("url").toString()); // Set URL ảnh đầu tiên
            }
            if (image2 != null && !image2.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(image2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                post.setImg2(uploadResult.get("url").toString()); // Set URL ảnh thứ hai
            }
            if (image3 != null && !image3.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(image3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                post.setImg3(uploadResult.get("url").toString()); // Set URL ảnh thứ ba
            }
         // Tự động gán thông tin người tạo (User hiện tại)
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(currentUsername); // Tìm User từ DB
            post.setCreatedBy(currentUser); // Gán User cho bài viết

            // Lưu bài post
            postService.savePost(post);
            
            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Post '" + post.getTitle() + "' has been successfully added!");
        } catch (IOException e) {
            log.error("Image upload failed: {}", e.getMessage());
            throw new RuntimeException("Image upload failed", e);
        }
        return "redirect:/manager/posts"; // Redirect về trang danh sách bài post
    }
    @GetMapping("/posts/edit/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        Optional<Post> optionalPost = postService.getPostById(id);
        if (optionalPost.isPresent()) {
            model.addAttribute("post", optionalPost.get());
            return "hotelmanager/posts-edit"; // Trả về view form chỉnh sửa
        } else {
            return "redirect:/manager/posts"; // Nếu không tìm thấy bài viết, quay lại danh sách
        }
    }

    @PostMapping("/posts/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute("post") Post updatedPost,
                             @RequestParam(value = "imageFile", required = false) MultipartFile image1,
                             @RequestParam(value = "imageFile2", required = false) MultipartFile image2,
                             @RequestParam(value = "imageFile3", required = false) MultipartFile image3,
                             RedirectAttributes redirectAttributes) {
        Optional<Post> optionalPost = postService.getPostById(id);

        if (optionalPost.isPresent()) {
            Post existingPost = optionalPost.get();
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setDescription(updatedPost.getDescription());
            existingPost.setContent(updatedPost.getContent());
            existingPost.setContent2(updatedPost.getContent2());
            existingPost.setContent3(updatedPost.getContent3());
            existingPost.setFigcaption2(updatedPost.getFigcaption2());
            existingPost.setFigcaption3(updatedPost.getFigcaption3());
            existingPost.setCategory(updatedPost.getCategory());
            existingPost.setCategory(updatedPost.getCategory());
            existingPost.setLocation(updatedPost.getLocation());

            try {
                // Upload và cập nhật ảnh mới nếu có
                if (image1 != null && !image1.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(image1.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                    existingPost.setImg(uploadResult.get("url").toString());
                }

                if (image2 != null && !image2.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(image2.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                    existingPost.setImg2(uploadResult.get("url").toString());
                }

                if (image3 != null && !image3.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(image3.getBytes(), Map.of("public_id", UUID.randomUUID().toString()));
                    existingPost.setImg3(uploadResult.get("url").toString());
                }

                // Lưu lại bài viết đã chỉnh sửa
                postService.savePost(existingPost);
                
                // Thêm thông báo thành công
                redirectAttributes.addFlashAttribute("successMessage", "Post '" + updatedPost.getTitle() + "' has been successfully added!");

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Image upload failed", e);
            }
        }

        return "redirect:/manager/posts"; // Quay lại trang danh sách bài viết
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        Optional<Post> optionalPost = postService.getPostById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setDelete(true); // Set isDelete thành true
            postService.savePost(post); // Cập nhật lại bài viết
        }
        return "redirect:/manager/posts"; // Quay lại trang danh sách bài viết
    }
    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<HotelDTO> hotels = hotelService.findAllHotels();
        model.addAttribute("hotels", hotels);
        return "redirect:/index?language=" + request.getParameter("language");
    }
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "username", required = false) String username, Model model, HttpServletRequest request) {
        String message = messageSource.getMessage("hello", null, "default message", request.getLocale());

        List<UserDTO> userDTOList;
        
        if (username != null && !username.isEmpty()) {
            userDTOList = userService.searchUsersByUsername(username);  // Giả sử bạn có phương thức tìm kiếm theo username
        } else {
            userDTOList = userService.findAllUsers();
        }

        model.addAttribute("users", userDTOList);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/users";
    }
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        UserDTO userDTO = userService.findUserById(id);
        model.addAttribute("user", userDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/users-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(value = "multipartFile", required = false) MultipartFile multipartFile) throws IOException {
        try {
            if (result.hasErrors()) {
                return "hotelmanager/users-edit";
            }

            // Lấy thông tin người dùng hiện tại từ service
            UserDTO currentUserDTO = userService.findUserById(id);

            // Kiểm tra nếu người dùng không chọn ảnh mới, giữ nguyên ảnh cũ
            if (multipartFile == null || multipartFile.isEmpty()) {
                userDTO.setImg(currentUserDTO.getImg()); // Giữ nguyên ảnh cũ
            }

            userService.updateUser(userDTO, multipartFile);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            redirectAttributes.addFlashAttribute("updatedUserId", userDTO.getId());

            return "redirect:/hotelmanager/users";
        } catch (UsernameAlreadyExistsException e) {
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "hotelmanager/users-edit";
        } catch (IllegalStateException e) {
            result.rejectValue("roleType", "role.invalid", e.getMessage());
            return "hotelmanager/users-edit";
        } catch (Exception e) {
            result.rejectValue("", "error.general", "An error occurred while updating the user");
            return "hotelmanager/users-edit";
        }
    }


    // Workaround for @DeleteMapping via post method
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/hotelmanager/users";
    }
    @GetMapping("/hotels/add")
    public String showAddHotelForm(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        HotelRegistrationDTO hotelRegistrationDTO = new HotelRegistrationDTO();

        // Initialize roomDTOs with SINGLE and DOUBLE room types
        RoomDTO singleRoom = new RoomDTO(null, null, RoomType.SINGLE, null, null, null, 0, 0.0);
        RoomDTO doubleRoom = new RoomDTO(null, null, RoomType.DOUBLE, null,null, null, 0, 0.0);
        RoomDTO suiteRoom = new RoomDTO(null, null, RoomType.SUITE, null, null, null, 0, 0.0);
        hotelRegistrationDTO.getRoomDTOs().add(singleRoom);
        hotelRegistrationDTO.getRoomDTOs().add(doubleRoom);
        hotelRegistrationDTO.getRoomDTOs().add(suiteRoom);

        model.addAttribute("hotel", hotelRegistrationDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/hotels-add";
    }

    @PostMapping("/hotels/add")
    public String addHotel(
            @Valid @ModelAttribute("hotel") HotelRegistrationDTO hotelRegistrationDTO,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("imageFile2") MultipartFile imageFile2,
            @RequestParam("imageFile3") MultipartFile imageFile3,
            @RequestParam(value = "roomImages1", required = false) List<MultipartFile> roomImages1,
            @RequestParam(value = "roomImages2", required = false) List<MultipartFile> roomImages2,
            @RequestParam(value = "roomImages3", required = false) List<MultipartFile> roomImages3,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            log.warn("Hotel creation failed due to validation errors: {}", result.getAllErrors());
            return "hotelmanager/hotels-add";
        }

        try {
            // Save the hotel
            hotelService.saveHotel(hotelRegistrationDTO, imageFile, imageFile2, imageFile3, roomImages1, roomImages2, roomImages3);
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());
            return "hotelmanager/hotels-add";
        }

        redirectAttributes.addFlashAttribute("message", "Hotel " + hotelRegistrationDTO.getName() + " added successfully");
        return "redirect:/manager/hotels?success";
    }


    @GetMapping("/hotels")
    public String listHotels(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        Long managerId = getCurrentManagerId();
        List<HotelDTO> hotelList = hotelService.findAllHotelDtosByManagerId(managerId);
        model.addAttribute("hotels", hotelList);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/hotels";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        Long managerId = getCurrentManagerId();
        HotelDTO hotelDTO = hotelService.findHotelByIdAndManagerId(id, managerId);
        model.addAttribute("hotel", hotelDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/hotels-edit";
    }

    @PostMapping("/hotels/edit/{id}")
    public String editHotel(
            @PathVariable Long id,
            @Valid @ModelAttribute("hotel") HotelDTO hotelDTO,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("imageFile2") MultipartFile imageFile2,
            @RequestParam("imageFile3") MultipartFile imageFile3,
            @RequestParam(value = "roomImages", required = false) List<MultipartFile> roomImages,
            @RequestParam(value = "roomImages2", required = false) List<MultipartFile> roomImages2,
            @RequestParam(value = "roomImages3", required = false) List<MultipartFile> roomImages3,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            return "hotelmanager/hotels-edit";
        }

        try {
            Long managerId = getCurrentManagerId(); // Lấy ID của quản lý hiện tại
            hotelDTO.setId(id); // Set ID cho HotelDTO

            // Cập nhật hotel cùng với ảnh phòng
            hotelService.updateHotelByManagerId(hotelDTO, managerId, imageFile, imageFile2, imageFile3, roomImages, roomImages2, roomImages3);

            redirectAttributes.addFlashAttribute("message", "Hotel ID: " + id + " updated successfully");
            return "redirect:/manager/hotels?success";
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
    
    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<VoucherDTO> vouchers = voucherService.getVouchersForUser(currentUsername);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/vouchers";
    }

    @GetMapping("/vouchers/add")
    public String showAddVoucherForm(Model model) {
        // Khởi tạo một đối tượng VoucherDTO trống để binding với form
        model.addAttribute("voucher", new VoucherDTO());
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/vouchers-add";
    }
    
    @PostMapping("/vouchers/add")
    public String addVoucher(@Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Voucher creation failed due to validation errors: {}", result.getAllErrors());
            return "hotelmanager/vouchers-add";
        }
        try {
            // Lấy username hiện tại
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            voucherDTO.setCreatedBy(currentUsername); // Gán username người tạo

            // Gọi service để lưu voucher
            voucherService.saveVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher " + voucherDTO.getName() + " added successfully");
            return "redirect:/manager/vouchers";
        } catch (Exception e) {
            result.rejectValue("name", "voucher.exists", e.getMessage());
            return "hotelmanager/vouchers-add";
        }
    }


    @GetMapping("/vouchers/edit/{id}")
    public String showEditVoucherForm(@PathVariable Long id, Model model) {
        VoucherDTO voucherDTO = voucherService.findVoucherById(id);
        model.addAttribute("voucher", voucherDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "hotelmanager/vouchers-edit";
    }

    @PostMapping("/vouchers/edit/{id}")
    public String editVoucher(@PathVariable Long id,
            @Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "hotelmanager/vouchers-edit";
        }
        try {
            voucherService.updateVoucher(id, voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher " + voucherDTO.getName() + " updated successfully");
            return "redirect:/manager/vouchers";
        } catch (Exception e) {
            result.rejectValue("name", "voucher.exists", e.getMessage());
            return "hotelmanager/vouchers-edit";
        }
    }


    @PostMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Voucher existingVoucher = voucherService.getVoucher(id);

        if (!existingVoucher.getCreatedBy().equals(currentUsername)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to delete this voucher.");
            return "redirect:/manager/vouchers";
        }

        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("success", "Voucher deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/manager/vouchers";
    }


    @GetMapping("/bookings")
    public String listBookings(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            Long managerId = getCurrentManagerId();
            List<BookingDTO> bookingDTOs = bookingService.findBookingsByManagerId(managerId);
            model.addAttribute("bookings", bookingDTOs);
            
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            model.addAttribute("currentUsername", currentUsername);

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
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            Long managerId = getCurrentManagerId();
            BookingDTO bookingDTO = bookingService.findBookingByIdAndManagerId(id, managerId);
            model.addAttribute("bookingDTO", bookingDTO);

            LocalDate checkinDate = bookingDTO.getCheckinDate();
            LocalDate checkoutDate = bookingDTO.getCheckoutDate();
            long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
            model.addAttribute("days", durationDays);
            
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            model.addAttribute("currentUsername", currentUsername);

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
    @PostMapping("/bookings/{id}/confirm-refund")
    public String confirmRefund(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmRefund(id);
            redirectAttributes.addFlashAttribute("success", "Refund has been confirmed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to confirm refund: " + e.getMessage());
        }
        return "redirect:/manager/bookings/" + id;
    }
    
    @GetMapping("/comments")
    public String listComments(Model model, HttpServletRequest request, @RequestParam(required = false) Long hotelId) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        // Get the current logged-in manager's hotel ID if necessary
        if (hotelId == null) {
            // You can use authentication or session to get the current hotel if the manager is logged in
            hotelId = getCurrentManagerHotelId(); // Implement this method based on your auth system
        }

        // Retrieve comments for the specific hotel
        List<CommentDTO> commentDTOList = commentService.findCommentsByHotel(hotelId);
        model.addAttribute("comments", commentDTOList);
        return "hotelmanager/comments";
    }

    @PostMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return "redirect:/manager/comments";
    }

    private Long getCurrentManagerId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserByUsername(username).getHotelManager().getId();
    }
    private Long getCurrentManagerHotelId() {
        // Get the currently logged-in user's username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Find the user by username and fetch the associated hotel manager
        User currentUser = userService.findUserByUsername(username);
        
        // Ensure the user is a hotel manager and return the hotel ID of the first hotel in the list
        if (currentUser != null && currentUser.getHotelManager() != null) {
            List<Hotel> hotels = currentUser.getHotelManager().getHotels();
            
            // Check if the manager manages any hotels
            if (hotels != null && !hotels.isEmpty()) {
                return hotels.get(0).getId();  // Get the ID of the first hotel
            } else {
                throw new IllegalStateException("No hotels are assigned to this manager.");
            }
        } else {
            throw new IllegalStateException("The current user is not a hotel manager.");
        }
    }
    @GetMapping("/hotels/export/excel")
    public void exportHotelsToExcel(HttpServletResponse response) throws IOException {
        Long managerId = getCurrentManagerId();
        List<HotelDTO> hotelList = hotelService.findAllHotelDtosByManagerId(managerId);
        excelExportService.exportHotelsToExcel(response, hotelList);
    }
    
    @GetMapping("/bookings/export/excel")
    public void exportBookingsToExcel(HttpServletResponse response) throws IOException {
        Long managerId = getCurrentManagerId();
        List<BookingDTO> bookings = bookingService.findBookingsByManagerId(managerId);
        excelExportService.exportBookingsToExcel(response, bookings);
    }
}
