package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    <S extends User> S save(S entity);

    User findByUsername(String username);

    @Modifying
    @Transactional
    @Query("update User usr set usr.avatar=?1 where usr.id=?2")
    void setAvatarById(byte[] avatar, Long id);


    @Modifying
    @Transactional
    @Query("update User usr set usr.email=?1 where usr.id=?2")
    void setEmailById(String email, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.password=?1 where usr.id=?2")
    void setPasswordById(String password, Long id);

    @Modifying
    @Transactional
    @Query(value = "update user_role set roles=?1 where user_id=?2", nativeQuery = true)
    void setRoleById(String role, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.confirmMailSent=?1 where usr.id=?2")
    void setConfirmMailSentById(boolean sent, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.passwordAbortSent=?1 where usr.id=?2")
    void setPasswordAbortSentById(boolean sent, Long id);


    @Modifying
    @Transactional
    @Query("update User usr set usr.mailConfirmationToken=?1 where usr.id=?2")
    void setMailConfirmationTokenById(String token, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.passwordAbortToken=?1 where usr.id=?2")
    void setPasswordAbortTokenById(String token, Long id);


    @Override
    Optional<User> findById(Long aLong);
}

