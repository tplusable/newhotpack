package com.table.hotpack.repository;

import com.table.hotpack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmail(Long email);


    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}