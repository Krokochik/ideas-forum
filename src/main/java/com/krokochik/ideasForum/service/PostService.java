package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.Post;
import com.krokochik.ideasForum.repository.PostRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    PostRepository postRepository;

    private final String[] tags = {"simple", "required", "important", "actual"};

    PostService() {
        Arrays.sort(tags);
    }

    public String formatTagsForEntry(@NotNull String nonFormattedTags) {
        StringBuilder builder = new StringBuilder();
        String[] tags = nonFormattedTags.toLowerCase().split(" ");

        for (int i = 0; i < tags.length; i++) {
            if (!(Arrays.binarySearch(this.tags, tags[i]) >= 0)) {
                return null;
            }
            builder.append(i).append(" ");
        }

        return builder.toString();
    }

    public String formatTagsForDisplay(@NotNull String nonFormattedTags) {
        StringBuilder builder = new StringBuilder();
        String[] tags = nonFormattedTags.split(" ");

        for (int i = 0; i < tags.length; i++) {
            if (Integer.parseInt(tags[i]) < this.tags.length) {
                builder.append(this.tags[Integer.parseInt(tags[i])].substring(0, 1).toUpperCase())
                        .append(this.tags[Integer.parseInt(tags[i])].substring(1))
                        .append(i < tags.length - 1 ? ", " : "");
            }
        }

        return builder.toString();
    }

    public Post[] findAllPostsWithTitle(String title) {
        return postRepository.findAllPostsWithTitle(title).length >= 1 ? postRepository.findAllPostsWithTitle(title) : null;
    }

    public Post[] findAllPostsWithFuzzyTitle(String title) {
        Post[] posts = getAllPosts();
        AtomicReference<Set<Post>> strictSearchingResultSet = new AtomicReference<>();

        Thread strictSearch = new Thread(() -> strictSearchingResultSet.set(Arrays.stream(findAllPostsWithTitle(title)).collect(Collectors.toSet())));

        Thread fuzzySearch = new Thread(() -> {
            for (Post post : posts) {

            }
        });

        return null;
    }

    public Post[] findAllPostsByAuthor(String authorName) {
        return postRepository.findAllPostsByAuthor(authorName); //!!!!!!!!!
    }

    public void save(Post post) {
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
        String[] filterTags = formatTagsForEntry(tags).split(" ");
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
