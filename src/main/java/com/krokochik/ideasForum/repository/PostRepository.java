package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.db.Post;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Override
    <S extends Post> @NotNull S save(@NotNull S entity);

    @Override
    @NotNull
    Optional<Post> findById(@NotNull Long aLong);

    @Transactional
    @Query(value = """
            SELECT * FROM pst
            ORDER BY id ASC""", nativeQuery = true)
    Post[] getAllPostsASC();

    @Transactional
    @Query(value = """
            SELECT * FROM pst
            ORDER BY id DESC""", nativeQuery = true)
    Post[] getAllPostsDESC();

    @Transactional
    @Query(value = """
            SELECT * FROM pst
            WHERE pst.title=?1
            ORDER BY id ASC""", nativeQuery = true)
    Post[] findAllPostsWithTitle(String title);

    @Transactional
    @Query(value = """
            SELECT * FROM pst
            WHERE tsquery(?1) @@ (tsvector(pst."fulltext_title") || tsvector(pst."fulltext_content"))
            """, nativeQuery = true)
    Post[] fulltextSearch(String ts_query);

    @Transactional
    @Query(value = """
            SELECT * FROM pst
            WHERE tsquery(?1) @@ tsvector(pst."fulltext_content")
            """, nativeQuery = true)
    Post[] fulltextContentSearch(String ts_query);


    @Transactional
    @Query(value = """
            SELECT * FROM pst
            WHERE pst.author=?1
            ORDER BY id ASC""", nativeQuery = true)
    Post[] findAllPostsByAuthor(String authorName);

}

