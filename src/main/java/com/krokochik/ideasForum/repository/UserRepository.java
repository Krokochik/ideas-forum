package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    <S extends User> S save(S entity);

    User findByUsername(String username);

    @Override
    Optional<User> findById(Long aLong);
}

