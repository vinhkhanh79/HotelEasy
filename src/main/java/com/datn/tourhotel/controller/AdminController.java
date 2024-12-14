package com.datn.tourhotel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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
import com.datn.tourhotel.model.Comment;
import com.datn.tourhotel.model.HotelManager;
import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.CommentDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.HotelRegistrationAdminDTO;
import com.datn.tourhotel.model.dto.HotelRegistrationDTO;
import com.datn.tourhotel.model.dto.RoomDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.model.dto.UserRegistrationDTO;
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
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
	
	@Autowired
	private MessageSource messageSource;
    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final CustomerService customerService;
    private final HotelManagerService hotelManagerService;
    private final PaymentService paymentService;
    private final ExcelExportService excelExportService;
    private final CommentService commentService;
    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
    private final VoucherService voucherService;
    @Autowired
    private PostService postService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request, @RequestParam(name = "earningsPeriod", required = false, defaultValue = "total") String period) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	Long customerCount = customerService.getCustomerCount();
        model.addAttribute("customerCount", customerCount);

        Long countHotelManagers = hotelManagerService.countHotelManagers();
        model.addAttribute("countHotelManagers", countHotelManagers);
        
        BigDecimal earningsTodayAdmin = paymentService.getEarningsByPeriodAdmin("day");
        BigDecimal earningsThisWeekAdmin = paymentService.getEarningsByPeriodAdmin("week");
        BigDecimal earningsThisMonthAdmin = paymentService.getEarningsByPeriodAdmin("month");
        BigDecimal earningsThisYearAdmin = paymentService.getEarningsByPeriodAdmin("year");
        BigDecimal earningsTotalAdmin = paymentService.getEarningsByPeriodAdmin("total");

        // Add earnings data to the model for use in the frontend
        model.addAttribute("earningsToday", earningsTodayAdmin);
        model.addAttribute("earningsThisWeek", earningsThisWeekAdmin);
        model.addAttribute("earningsThisMonth", earningsThisMonthAdmin);
        model.addAttribute("earningsThisYear", earningsThisYearAdmin);
        model.addAttribute("earningsTotal", earningsTotalAdmin);
        
        List<BigDecimal> earningsPerDayInYear = paymentService.getEarningsPerDayInYearAdmin();
        model.addAttribute("earningsPerDayInYear", earningsPerDayInYear);
        
     // Tổng thu nhập theo period
        BigDecimal getEarningsByPeriod = paymentService.getEarningsByPeriodAdmin(period);
        if (getEarningsByPeriod == null) {
            getEarningsByPeriod = BigDecimal.ZERO;
        }
     // Format thu nhập theo định dạng "1,500,000"
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedEarnings = decimalFormat.format(getEarningsByPeriod);
        model.addAttribute("formattedEarnings", formattedEarnings);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        model.addAttribute("greetingMessage", message);
        
        List<Object[]> topHotelsByEarnings = paymentService.getTopHotelsByEarnings();
        List<String> hotelNames = new ArrayList<>();
        List<BigDecimal> hotelEarnings = new ArrayList<>();

        for (Object[] result : topHotelsByEarnings) {
            hotelNames.add((String) result[0]);
            hotelEarnings.add((BigDecimal) result[1]);
        }

        model.addAttribute("hotelNames", hotelNames);
        model.addAttribute("hotelEarnings", hotelEarnings);
        return "admin/dashboard";
    }

    @GetMapping("/report")
    public String report(Model model, HttpServletRequest request, @RequestParam(name = "earningsPeriod", required = false, defaultValue = "total") String period) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
    	Long customerCount = customerService.getCustomerCount();
        model.addAttribute("customerCount", customerCount);

        Long countHotelManagers = hotelManagerService.countHotelManagers();
        model.addAttribute("countHotelManagers", countHotelManagers);
        
        BigDecimal earningsTodayAdmin = paymentService.getEarningsByPeriodAdmin("day");
        BigDecimal earningsThisWeekAdmin = paymentService.getEarningsByPeriodAdmin("week");
        BigDecimal earningsThisMonthAdmin = paymentService.getEarningsByPeriodAdmin("month");
        BigDecimal earningsThisYearAdmin = paymentService.getEarningsByPeriodAdmin("year");
        BigDecimal earningsTotalAdmin = paymentService.getEarningsByPeriodAdmin("total");
        
     // Lấy danh sách Top 10 khách sạn có nhiều đánh giá 5 sao nhất
        List<Object[]> topHotelsWithFiveStarRatings = commentService.getTopHotelsWithFiveStarRatings();
        
        List<String> hotelNames = new ArrayList<>();

//        List<String> hotelNames = topHotelsWithFiveStarRatings.stream()
//                .map(result -> (String) result[0])
//                .collect(Collectors.toList());

        List<Long> fiveStarCounts = topHotelsWithFiveStarRatings.stream()
                .map(result -> (Long) result[1])
                .collect(Collectors.toList());

        model.addAttribute("topHotelNames", hotelNames);
        model.addAttribute("fiveStarCounts", fiveStarCounts);

        // Add earnings data to the model for use in the frontend
        model.addAttribute("earningsToday", earningsTodayAdmin);
        model.addAttribute("earningsThisWeek", earningsThisWeekAdmin);
        model.addAttribute("earningsThisMonth", earningsThisMonthAdmin);
        model.addAttribute("earningsThisYear", earningsThisYearAdmin);
        model.addAttribute("earningsTotal", earningsTotalAdmin);
        
        List<BigDecimal> earningsPerDayInYear = paymentService.getEarningsPerDayInYearAdmin();
        model.addAttribute("earningsPerDayInYear", earningsPerDayInYear);
        
     // Tổng thu nhập theo period
        BigDecimal getEarningsByPeriod = paymentService.getEarningsByPeriodAdmin(period);
        if (getEarningsByPeriod == null) {
            getEarningsByPeriod = BigDecimal.ZERO;
        }
     // Format thu nhập theo định dạng "1,500,000"
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedEarnings = decimalFormat.format(getEarningsByPeriod);
        model.addAttribute("formattedEarnings", formattedEarnings);
        
        List<BigDecimal> earningsPerWeekInYear = paymentService.getEarningsPerWeekInYearAdmin();
        model.addAttribute("earningsPerWeekInYear", earningsPerWeekInYear);
        
        List<BigDecimal> earningsPerMonthInYear = paymentService.getEarningsPerMonthInYearAdmin();
        model.addAttribute("earningsPerMonthInYear", earningsPerMonthInYear);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        model.addAttribute("greetingMessage", message);
        List<Object[]> topHotelsByEarnings = paymentService.getTopHotelsByEarnings();
        
        List<BigDecimal> hotelEarnings = new ArrayList<>();

        for (Object[] result : topHotelsByEarnings) {
            hotelNames.add((String) result[0]);
            hotelEarnings.add((BigDecimal) result[1]);
        }

        model.addAttribute("hotelNames", hotelNames);
        model.addAttribute("hotelEarnings", hotelEarnings);
        
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
        return "admin/report";
    }
    @GetMapping("/posts")
    public String listPosts(Model model, HttpServletRequest request) {
        // Optionally use messages for localization (not necessary for post listing)
        String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        
        // Get the list of posts
        List<Post> postList = postService.getAllPosts();
        
        // Add the post list to the model
        model.addAttribute("posts", postList);
        
        // Add current username (if needed for display or authentication)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        // Return the name of the view to display (admin/posts.html)
        return "admin/posts";
    }

 // Hiển thị form thêm bài post
    @GetMapping("/posts/add")
    public String showAddPostForm(Model model) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("post", new Post());
        return "admin/posts-add"; // Đây là file HTML hiển thị form
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
        return "redirect:/admin/posts"; // Redirect về trang danh sách bài post
    }
    @GetMapping("/posts/edit/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        Optional<Post> optionalPost = postService.getPostById(id);
        if (optionalPost.isPresent()) {
            model.addAttribute("post", optionalPost.get());
            return "admin/posts-edit"; // Trả về view form chỉnh sửa
        } else {
            return "redirect:/admin/posts"; // Nếu không tìm thấy bài viết, quay lại danh sách
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

        return "redirect:/admin/posts"; // Quay lại trang danh sách bài viết
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        Optional<Post> optionalPost = postService.getPostById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setDelete(true); // Set isDelete thành true
            postService.savePost(post); // Cập nhật lại bài viết
        }
        return "redirect:/admin/posts"; // Quay lại trang danh sách bài viết
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
        return "admin/users";
    }
    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO()); // Khởi tạo đối tượng mới
        return "admin/users-add"; // Trả về view cho form thêm người dùng
    }
    @PostMapping("/users/add")
    public String addUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                          BindingResult result,
                          @RequestParam(value = "multipartFile", required = false) MultipartFile multipartFile,
                          RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser2(registrationDTO, multipartFile);
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
            return "redirect:/admin/users";
        } catch (UsernameAlreadyExistsException e) {
            // Ghi log lỗi khi username/email đã tồn tại
            result.rejectValue("username", "user.exists", e.getMessage());
            log.error("Username or Email already exists: {}", e.getMessage(), e); // In thêm stack trace
            return "admin/users-add";
        } catch (Exception e) {
            // Ghi log lỗi tổng quát
            log.error("Unexpected error occurred while adding user: {}", e.getMessage(), e); // In lỗi chi tiết
            redirectAttributes.addFlashAttribute("error", "Failed to add user! See logs for more details.");
            return "admin/users-add";
        }
    }


    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        UserDTO userDTO = userService.findUserById(id);
        model.addAttribute("user", userDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/users-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(value = "multipartFile", required = false) MultipartFile multipartFile) throws IOException {
        try {
            if (result.hasErrors()) {
                return "admin/users-edit";
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

            return "redirect:/admin/users";
        } catch (UsernameAlreadyExistsException e) {
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "admin/users-edit";
        } catch (IllegalStateException e) {
            result.rejectValue("roleType", "role.invalid", e.getMessage());
            return "admin/users-edit";
        } catch (Exception e) {
            result.rejectValue("", "error.general", "An error occurred while updating the user");
            return "admin/users-edit";
        }
    }


    // Workaround for @DeleteMapping via post method
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/hotels")
    public String listHotels(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<HotelDTO> hotelDTOList = hotelService.findAllHotels();
        model.addAttribute("hotels", hotelDTOList);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/hotels";
    }
    
    @GetMapping("/hotels/add")
    public String showAddHotelForm(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        HotelRegistrationAdminDTO hotelRegistrationAdminDTO = new HotelRegistrationAdminDTO();

        // Initialize roomDTOs with SINGLE and DOUBLE room types
        RoomDTO singleRoom = new RoomDTO(null, null, RoomType.SINGLE, null, null, null, 0, 0.0);
        RoomDTO doubleRoom = new RoomDTO(null, null, RoomType.DOUBLE, null,null, null, 0, 0.0);
        RoomDTO suiteRoom = new RoomDTO(null, null, RoomType.SUITE, null, null, null, 0, 0.0);
        hotelRegistrationAdminDTO.getRoomDTOs().add(singleRoom);
        hotelRegistrationAdminDTO.getRoomDTOs().add(doubleRoom);
        hotelRegistrationAdminDTO.getRoomDTOs().add(suiteRoom);
        
        List<HotelManager> managers = hotelManagerService.findAll(); // Lấy tất cả hotel managers
        model.addAttribute("managers", managers); // Truyền danh sách đến view
        model.addAttribute("hotel", new HotelRegistrationDTO());

        model.addAttribute("hotel", hotelRegistrationAdminDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/hotels-add";
    }

    @PostMapping("/hotels/add")
    public String addHotel(
            @Valid @ModelAttribute("hotel") HotelRegistrationAdminDTO hotelRegistrationAdminDTO,
            BindingResult result,
            @RequestParam Long managerId,
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

            // Add the manager list back to the model to display in the form
            List<HotelManager> managers = hotelManagerService.findAll();
            model.addAttribute("managers", managers);

            return "admin/hotels-add";
        }

        try {
            // Get the HotelManager object from the database
            HotelManager manager = hotelManagerService.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

            // Save the hotel
            hotelService.saveHotelAdmin(hotelRegistrationAdminDTO, imageFile, imageFile2, imageFile3, roomImages1, roomImages2, roomImages3, manager);

        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());

            // Repopulate the manager list in case of specific errors
            List<HotelManager> managers = hotelManagerService.findAll();
            model.addAttribute("managers", managers);

            return "admin/hotels-add";
        }

        redirectAttributes.addFlashAttribute("addedHotelName", hotelRegistrationAdminDTO.getName());
        return "redirect:/admin/hotels?success";
    }



    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id, Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        HotelDTO hotelDTO = hotelService.findHotelDtoById(id);
        model.addAttribute("hotel", hotelDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        
        List<HotelManager> managers = hotelManagerService.findAll(); // Lấy tất cả hotel managers
        model.addAttribute("managers", managers); // Truyền danh sách đến view
        return "admin/hotels-edit";
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
            // Repopulate managers for the view
            List<HotelManager> managers = hotelManagerService.findAll();
            model.addAttribute("managers", managers); // Re-add the manager list to the model
            return "admin/hotels-edit";
        }

        try {
            HotelManager manager = hotelManagerService.findById(hotelDTO.getManagerId())
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found"));
            hotelDTO.setManager(manager);
            hotelService.updateHotel(hotelDTO, imageFile, imageFile2, imageFile3, roomImages, roomImages2, roomImages3);
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());

            // Repopulate managers again in case of specific errors
            List<HotelManager> managers = hotelManagerService.findAll();
            model.addAttribute("managers", managers);
            return "admin/hotels-edit";
        }

        redirectAttributes.addFlashAttribute("updatedHotelId", hotelDTO.getId());
        return "redirect:/admin/hotels?success";
    }


    @PostMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotelById(id);
        return "redirect:/admin/hotels";
    }
    
    @GetMapping("/vouchers/add")
    public String showAddVoucherForm(Model model) {
        // Khởi tạo một đối tượng VoucherDTO trống để binding với form
        model.addAttribute("voucher", new VoucherDTO());
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/vouchers-add";
    }
    @PostMapping("/vouchers/add")
    public String addVoucher(@Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Voucher creation failed due to validation errors: {}", result.getAllErrors());
            return "admin/vouchers-add";
        }
        try {
            // Lấy username hiện tại
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            voucherDTO.setCreatedBy(currentUsername);
            model.addAttribute("currentUsername", currentUsername);

            // Gọi service để lưu voucher
            voucherService.saveVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher " + voucherDTO.getName() + " added successfully");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            result.rejectValue("name", "voucher.exists", e.getMessage());
            return "admin/vouchers-add";
        }
    }

    @GetMapping("/vouchers/edit/{id}")
    public String showEditVoucherForm(@PathVariable Long id, Model model) {
        VoucherDTO voucherDTO = voucherService.findVoucherById(id);
        model.addAttribute("voucher", voucherDTO);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/vouchers-edit";
    }

    @PostMapping("/vouchers/edit/{id}")
    public String editVoucher(@PathVariable Long id,
            @Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/vouchers-edit";
        }
        try {
            voucherService.updateVoucher(id, voucherDTO);
            redirectAttributes.addFlashAttribute("success", "Voucher " + voucherDTO.getName() + " updated successfully");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            result.rejectValue("name", "voucher.exists", e.getMessage());
            return "admin/vouchers-edit";
        }
    }

    @PostMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id); // Truyền id vào service
            redirectAttributes.addFlashAttribute("success", "Voucher deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
        List<VoucherDTO> vouchers = voucherService.getVouchers(); // Lấy các voucher chưa bị xóa
        model.addAttribute("vouchers", vouchers);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/vouchers";
    }

    @GetMapping("/bookings")
    public String listBookings(Model model, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        List<BookingDTO> bookingDTOList = bookingService.findAllBookings();
        model.addAttribute("bookings", bookingDTOList);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	String message = messageSource.getMessage("hello", null, "default message", request.getLocale());
        try {
            BookingDTO bookingDTO = bookingService.findBookingById(id);
            model.addAttribute("bookingDTO", bookingDTO);

            LocalDate checkinDate = bookingDTO.getCheckinDate();
            LocalDate checkoutDate = bookingDTO.getCheckoutDate();
            long durationDays = ChronoUnit.DAYS.between(checkinDate, checkoutDate);
            model.addAttribute("days", durationDays);
            
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            model.addAttribute("currentUsername", currentUsername);

            return "admin/bookings-details";
        } catch (EntityNotFoundException e) {
            log.error("No booking found with the provided ID", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found. Please try again later.");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            log.error("An error occurred while displaying booking details", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
            return "redirect:/admin/dashboard";
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
        return "redirect:/admin/bookings/" + id;
    }
    @GetMapping("/comments")
    public String listComments(Model model, HttpServletRequest request) {
    	String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("currentUsername", currentUsername);
        String message = messageSource.getMessage("hello", null, "default message", request.getLocale());

        List<CommentDTO> commentDTOList = commentService.findAllCommentList();
        model.addAttribute("comments", commentDTOList);
        return "admin/comments";
    }
    @PostMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return "redirect:/admin/comments";
    }


    @GetMapping("/hotels/export/excel")
    public void exportHotelsToExcel(HttpServletResponse response) throws IOException {
        List<HotelDTO> hotelList = hotelService.findAllHotels();
        excelExportService.exportHotelsToExcel(response, hotelList);
    }
    
    @GetMapping("/bookings/export/excel")
    public void exportBookingsToExcel(HttpServletResponse response) throws IOException {
        List<BookingDTO> bookings = bookingService.findAllBookings();
        excelExportService.exportBookingsToExcel(response, bookings);
    }
    
    @GetMapping("/users/export/excel")
    public void exportUsersToExcel(HttpServletResponse response) throws IOException {
        List<UserDTO> users = userService.findAllUsers();
        excelExportService.exportUsersToExcel(response, users);
    }

}
