package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}

