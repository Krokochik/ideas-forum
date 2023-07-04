package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.User;
import com.sun.istack.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    <S extends User> @org.jetbrains.annotations.NotNull S save(@org.jetbrains.annotations.NotNull S entity);

    User findByUsername(String username);

    @Transactional
    @Query(value = "select * from usr order by id ASC", nativeQuery = true)
    User[] getAllUsers();

    @Transactional
    @Query(value = "select * from usr where usr.oauth2_id=?1", nativeQuery = true)
    User getUserByOAuth2Id(Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.qrcode=?1 where usr.id=?2")
    void setQRCodeById(byte[] qrcode, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.avatar=?1 where usr.id=?2")
    void setAvatarById(byte[] avatar, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.username=?1 where usr.id=?2")
    void setUsernameById(@NotNull String username, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.nickname=?1 where usr.id=?2")
    void setNicknameById(@NotNull String nickname, Long id);


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
    @Query(value = "update mfa_reset_token set mfa_reset_tokens=?1 where user_id=?2", nativeQuery = true)
    void setMfaResetTokensById(Set<String> tokens, Long id);

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

    @Modifying
    @Transactional
    @Query("update User usr set usr.mfaToken=?1 where usr.id=?2")
    void setMfaTokenById(String token, Long id);

    @Modifying
    @Transactional
    @Query("update User usr set usr.mfaConnected=?1 where usr.id=?2")
    void setMfaConnectedById(boolean connected, Long id);

    @Override
    @org.jetbrains.annotations.NotNull
    Optional<User> findById(@org.jetbrains.annotations.NotNull Long id);
}

