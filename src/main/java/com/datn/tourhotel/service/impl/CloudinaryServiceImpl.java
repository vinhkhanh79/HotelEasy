package com.datn.tourhotel.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.cloudinary.Cloudinary;
import com.datn.tourhotel.model.User; // Sử dụng đúng entity
import com.datn.tourhotel.repository.UserRepository;
import com.datn.tourhotel.service.CloudinaryService;
import com.datn.tourhotel.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    @Lazy
    private UserService userService;  // Sử dụng UserService

    @Override
    public String uploadImage(MultipartFile file, Long userId) throws IOException {
        Map uploadResult = cloudinary.uploader()
                .upload(file.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString()));

        String url = uploadResult.get("url").toString();
        userService.updateUserImage(userId, url);

        return url;
    }
}
