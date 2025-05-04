package com.Capstone.EventManagementPortal.repository;

import com.Capstone.EventManagementPortal.model.Role;
import com.Capstone.EventManagementPortal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);  // Inherited from JpaRepository



}
