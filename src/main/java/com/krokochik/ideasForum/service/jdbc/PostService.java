package com.krokochik.ideasForum.service.jdbc;

import com.github.demidko.aot.WordformMeaning;
import com.krokochik.ideasForum.model.db.Post;
import com.krokochik.ideasForum.repository.PostRepository;
import com.krokochik.ideasForum.service.SpellMorphService;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
public class PostService {

    @Autowired
    PostRepository postRepository;
    private final String[] tags;

    @Autowired
    public PostService() {
        this.tags = new String[]{"simple", "required", "important", "actual"};
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
            } else return Optional.empty();
        }

        return Optional.of(builder.toString());
    }

    public enum SearchColumns {
        FULLTEXT_TITLE,
        FULLTEXT_CONTENT,
        EVERYWHERE
    }

    public Post[] fulltextSearch(@NotNull String query, @NotNull SearchColumns searchColumns, boolean strict) {
        query = query.toLowerCase();
        ArrayList<Post> result = new ArrayList<>();
        if (strict) {
            query = makeFulltext(query);
            for (Post post : getAllPosts()) {
                switch (searchColumns) {
                    case EVERYWHERE:
                        if (post.getTitle().equalsIgnoreCase(query) || post.getContent().equalsIgnoreCase(query))
                            result.add(post);

                    case FULLTEXT_TITLE:
                        if (post.getTitle().equalsIgnoreCase(query))
                            result.add(post);

                    case FULLTEXT_CONTENT:
                        if (post.getContent().equalsIgnoreCase(query))
                            result.add(post);
                }
            }
        } else {
            final int[] middle = {0};
            Post[] allPosts = getAllPosts();
            String finalText = query;
            for (Post post : allPosts) {
                Runnable titleSearch = () -> {
                    if (post.getTitle().equalsIgnoreCase(finalText))
                        result.add(++middle[0] - 1, post);
                    else if (post.getTitle().toLowerCase().contains(finalText))
                        result.add(middle[0], post);
                    else if (finalText.contains(post.getTitle().toLowerCase()))
                        result.add(post);
                };
                Runnable contentSearch = () -> {
                    if (post.getContent().equalsIgnoreCase(finalText))
                        result.add(++middle[0] - 1, post);
                    else if (post.getContent().toLowerCase().contains(finalText))
                        result.add(middle[0], post);
                    else if (finalText.contains(post.getContent().toLowerCase()))
                        result.add(post);
                };
                switch (searchColumns) {
                    case EVERYWHERE:
                        titleSearch.run();
                        contentSearch.run();
                    case FULLTEXT_TITLE:
                        titleSearch.run();
                    case FULLTEXT_CONTENT:
                        contentSearch.run();
                }
            }
            if (result.size() != allPosts.length) {
                result.clear();
                String ts_query_prototype = makeFulltext(query);
                ArrayList<String> words = SpellMorphService.splitIntoWords(ts_query_prototype).orElseGet(() -> new ArrayList<>() {{
                    add(ts_query_prototype);
                }});
                for (int i = 0; i < words.size(); i++) {
                    List<WordformMeaning> meanings = WordformMeaning.lookupForMeanings(words.get(i));
                    String partOfSpeech;
                    boolean isGarbage = true;

                    words.set(i, meanings.stream()
                            .map(WordformMeaning::toString)
                            .collect(Collectors.joining("|", "(", ")")));

                    for (WordformMeaning meaning : meanings) {
                        partOfSpeech = meaning.getPartOfSpeech().toString();
                        if (!(partOfSpeech.equalsIgnoreCase("Предлог")
                                || partOfSpeech.equalsIgnoreCase("Союз")
                                || partOfSpeech.equalsIgnoreCase("Частица")
                                || partOfSpeech.equalsIgnoreCase("Междометие"))) {
                            isGarbage = false;
                            break;
                        }
                    }
                    if (isGarbage) {
                        words.remove(i--);
                    }
                }
                String ts_query = String.join("&", words).replaceAll("[^aA-zZаА-яЯ0-9|()&]", "").replaceAll("[\\^\\[\\]]", "");
                return postRepository.fulltextSearch(ts_query);
            }
        }

        return result.toArray(Post[]::new);
    }

    public Post[] findAllPostsByAuthor(@NotNull String authorName) {
        ArrayList<Post> result = new ArrayList<>();
        Post[] posts = getAllPosts();

        for (Post post : posts)
            if (post.getAuthor().equals(authorName))
                result.add(post);

        return result.toArray(Post[]::new);

    }

    private String makeFulltext(@NotNull String text) {
        CompletableFuture<ArrayList<String>> httpsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return SpellMorphService.splitIntoWords(SpellMorphService.lemmatize(SpellMorphService.correctSpell(text, "https")))
                        .orElseGet(ArrayList::new);
            } catch (IOException ioException) {
                return new ArrayList<>() {{
                    add("");
                }};
            }
        });
        CompletableFuture<ArrayList<String>> httpFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return SpellMorphService.splitIntoWords(SpellMorphService.lemmatize(SpellMorphService.correctSpell(text, "http")))
                        .orElseGet(ArrayList::new);
            } catch (IOException ioException) {
                return new ArrayList<>() {{
                    add("");
                }};
            }
        });

        CompletableFuture<Void> concatAnswers = CompletableFuture.allOf(httpsFuture, httpFuture);
        var result = new Object() {
            String str;
        };

        concatAnswers.thenRun(() -> {
            List<String> words = new ArrayList<>() {{
                add(text);
            }};
            try {
                httpFuture.get().addAll(httpsFuture.get());
                words = httpFuture.get();
                result.str = String.join(" ", SpellMorphService.removeRepeats(new ArrayList<>(words)))
                        .replaceAll("[^aA-zZ аА-яЯ0-9]", "");
            } catch (InterruptedException | ExecutionException e) {
                result.str = String.join(" ", words);
            }
        });

        concatAnswers.join();
        return result.str;
    }

    @SneakyThrows
    public void save(@NotNull Post post) {
        if (!((post.getContent().length() > 60_000) || (post.getTitle().length() > 255)))
            postRepository.save(post);
        else {
            if ((post.getContent().length() > 60_000) && (post.getTitle().length() > 255))
                throw new Exception("Content and title are too long");
            if (post.getContent().length() > 60_000)
                throw new Exception("Content is too long");
            if (post.getTitle().length() > 255)
                throw new Exception("Title is too long");
        }

        if (post.getFulltextTitle().isEmpty() || post.getFulltextTitle().matches("[^aA-zZ аА-яЯ0-9]")) {
            String fulltext = makeFulltext(post.getTitle());
            post.setFulltextTitle(fulltext.isEmpty() ? post.getTitle() : fulltext);
        }
        if (post.getFulltextContent().isEmpty() || post.getFulltextContent().matches("[^aA-zZ аА-яЯ0-9]")) {
            String fulltext = makeFulltext(post.getContent());
            post.setFulltextContent(fulltext.isEmpty() ? post.getContent() : fulltext);
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

    /**
     * @param tags tags enumeration in the entry form
     */
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
