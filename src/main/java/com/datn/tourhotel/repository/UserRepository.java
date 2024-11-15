package com.datn.tourhotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datn.tourhotel.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.username = :username AND u.isDeleted = false")
    User findByUsername(String username);
	
	@Query("SELECT u FROM User u WHERE u.isDeleted = false")
	List<User> findAllActiveUsers();
	
	@Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    User findByEmail(String email);
	
	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.isDeleted = false")
    boolean existsByUsername(String username);
	
	Optional<User> findByUsernameAndIsDeletedFalse(String username);
	
	public List<User> findByUsernameContainingIgnoreCase(String username);


}
