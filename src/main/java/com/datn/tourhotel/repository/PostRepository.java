package com.datn.tourhotel.repository;

import com.datn.tourhotel.model.Post;
import com.datn.tourhotel.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByIsDeleteFalse();
	Optional<Post> findByIdAndIsDeleteFalse(Long id);
	List<Post> findByCreatedByAndIsDeleteFalse(User user);

}
