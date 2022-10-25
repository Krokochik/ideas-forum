package com.krokochik.ideasForum.repository;

import com.krokochik.ideasForum.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Override
    <S extends Post> S save(S entity);

    @Override
    Optional<Post> findById(Long aLong);

    @Transactional
    @Query(value = """
            SELECT * FROM public.pst
            ORDER BY id ASC""", nativeQuery = true)
    Post[] getAllPostsASC();

    @Transactional
    @Query(value = """
            SELECT * FROM public.pst
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
            WHERE pst.author=?1
            ORDER BY id ASC""", nativeQuery = true)
    Post[] findAllPostsByAuthor(String authorName);

}

