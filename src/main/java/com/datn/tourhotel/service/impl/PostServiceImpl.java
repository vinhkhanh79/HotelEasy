package com.datn.tourhotel.service.impl;

import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.repository.PostRepository;
import com.datn.tourhotel.service.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    // Thêm bài post mới
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    // Tìm bài post theo ID
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }
    
    @Override
    public List<Post> getAllPosts() {
        return postRepository.findByIsDeleteFalse(); // Lấy các bài viết chưa bị xóa
    }
    // Implement the findById method
    @Override
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id); // Fetch the post by ID from the repository
    }
    public List<Post> getPostsByCreatedBy(User user) {
        return postRepository.findByCreatedBy(user);
    }

}
