package com.datn.tourhotel.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
	String uploadImage(MultipartFile file, Long userId) throws IOException;

}
