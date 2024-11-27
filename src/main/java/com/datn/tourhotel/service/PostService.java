package com.datn.tourhotel.service;

import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;
import com.datn.tourhotel.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PostService {
    public Post savePost(Post post);
    public List<Post> getAllPosts();
    public Optional<Post> getPostById(Long id);
    Optional<Post> findById(Long id);
    public List<Post> getPostsByCreatedBy(User user);


}
