package com.krokochik.ideasforum.repository;

import com.krokochik.ideasforum.model.db.PersistentRememberMe;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RememberMeRepository extends JpaRepository<PersistentRememberMe, String> {
    @Override
    <S extends PersistentRememberMe> @NotNull S save(@NotNull S entity);

    @NotNull
    @Override
    Optional<PersistentRememberMe> findById(@NotNull String id);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE
            FROM persistent_logins
            WHERE username = ?1
            """, nativeQuery = true)
    void deleteForUsername(String username);
}

