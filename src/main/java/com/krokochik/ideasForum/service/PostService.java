package com.krokochik.ideasForum.service;


import com.krokochik.ideasForum.entity.Post;
import com.krokochik.ideasForum.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public void save(Post post){
        postRepository.save(post);
    }

    public PostRepository getPostRepository() {
        return postRepository;
    }

    public void setPostRepository(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
}
