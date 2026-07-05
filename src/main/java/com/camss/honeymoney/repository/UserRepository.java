package com.camss.honeymoney.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.camss.honeymoney.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean exexistsByEmail(String email);
}
