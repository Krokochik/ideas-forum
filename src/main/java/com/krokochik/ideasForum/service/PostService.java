package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.Post;
import com.krokochik.ideasForum.repository.PostRepository;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    PostRepository postRepository;

    private final String[] tags = {"simple", "required", "important", "actual"};

    PostService() {
        Arrays.sort(tags);
    }

    public Optional<String> formatTagsForEntry(@NotNull String nonFormattedTags) {
        StringBuilder builder = new StringBuilder();
        String[] tags = nonFormattedTags.toLowerCase().split(", ");

        for (int i = 0; i < tags.length; i++) {
            boolean tagExists = Arrays.binarySearch(this.tags, tags[i]) >= 0;
            if (tagExists) {
                builder.append(i).append(" ");
            } else return Optional.empty();
        }

        return Optional.of(builder.toString().trim());
    }

    public Optional<String> formatTagsForDisplay(@NotNull String nonFormattedTags) {
        StringBuilder builder = new StringBuilder();
        String[] tags = nonFormattedTags.split(" ");

        for (int i = 0; i < tags.length; i++) {
            if (Integer.parseInt(tags[i]) < this.tags.length && Integer.parseInt(tags[i]) >= 0) {
                builder.append(this.tags[Integer.parseInt(tags[i])].substring(0, 1).toUpperCase())
                        .append(this.tags[Integer.parseInt(tags[i])].substring(1))
                        .append(i < tags.length - 1 ? ", " : "");
            }
            else return Optional.empty();
        }

        return Optional.of(builder.toString());
    }

    public Post[] findAllPostsWithTitle(String title, boolean strict) {
        return postRepository.findAllPostsWithTitle(title).length >= 1 ? postRepository.findAllPostsWithTitle(title) : null;
    }

    public Post[] findAllPostsByAuthor(@NotNull String authorName) {
        return postRepository.findAllPostsByAuthor(authorName); //!!!!!!!!!
    }

    @SneakyThrows
    public void save(@NotNull Post post) {

        if (post.getFulltextTitle() == null) {

        }
        if (post.getFulltextContent() == null) {

        }
        postRepository.save(post);
    }

    public Optional<Post> findPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post[] getAllPosts(boolean desc) {
        if (desc)
            return postRepository.getAllPostsDESC();
        return postRepository.getAllPostsASC();
    }

    public Post[] getAllPosts() {
        return getAllPosts(false);
    }

    public Post[] findAllPostsWithTags(String tags) {
        ArrayList<Post> posts = new ArrayList<>();
        String[] filterTags = tags.split(" ");
        Arrays.sort(filterTags);

        for (Post post : getAllPosts()) {
            for (int i = 0; i < filterTags.length; i++) {
                String currentTag = filterTags[i];
                if (!(Arrays.binarySearch(post.getTags().split(" "), currentTag) >= 0))
                    break;
                if (i == filterTags.length - 1)
                    posts.add(post);
            }
        }

        return posts.toArray(new Post[]{});
    }
}
