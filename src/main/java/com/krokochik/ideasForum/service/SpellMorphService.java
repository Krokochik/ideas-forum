package com.krokochik.ideasForum.service;

import com.github.demidko.aot.WordformMeaning;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpellMorphService {

    private static final int MAX_REQUEST_LENGTH = 10_000; // макс длина POST запроса Ya. Speller

    public static Optional<ArrayList<String>> splitIntoWords(@NotNull String text) {
        text = text.replaceAll("[^A-Za-zА-Яа-я0-9ёЁ ]", " ")
                .replaceAll("\\s+", " ").trim().toLowerCase();
        if (text.replace(" ", "").length() >= 1)
            if (text.contains(" "))
                return Optional.of(new ArrayList<>(Arrays.stream(text.split(" ")).toList()));
            else
                return Optional.of((new ArrayList<>(Stream.of(text).toList())));
        else return Optional.empty();
    }

    public static Optional<ArrayList<String>> splitIntoParts(@NotNull String text, double maxPartLength) {
        if (text.length() > maxPartLength) {
            ArrayList<String> words;

            text = text.replaceAll("\\s+", " ").trim();
            if (text.contains(" "))
                words = new ArrayList<>(Arrays.stream(text.split(" ")).toList());
            else
                words = new ArrayList<>(Stream.of(text).toList());

            ArrayList<String> parts = new ArrayList<>();
            final double partsCount = Math.ceil(text.length() / maxPartLength);
            final double partLength = text.length() / partsCount;
            int currentLength = 0;
            StringBuilder currentPart = new StringBuilder();

            for (int i = 0, wordsSize = words.size(); i < wordsSize; i++) {
                String word = words.get(i);
                if (word.length() + currentLength < partLength) {
                    currentPart.append(word).append(" ");
                    currentLength += word.length();
                } else {
                    i--;
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                    currentLength = 0;
                }
            }

            return Optional.of(parts);
        }
        @NotNull String finalText = text;
        return Optional.of(new ArrayList<>() {{
            add(finalText);
        }});
    }

    public static Optional<ArrayList<String>> splitIntoParts(@NotNull String text) {
        return splitIntoParts(text, MAX_REQUEST_LENGTH);
    }

    public static String correctSpell(@NotNull String text, @NotNull String protocol) throws IOException {
        ArrayList<String> parts;
        StringBuilder result = new StringBuilder();

        Optional<ArrayList<String>> temp = splitIntoParts(text);
        if (temp.isPresent())
            parts = temp.get();
        else
            return text;

        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            URL url = new URL("PROTOCOL://speller.yandex.net/services/spellservice.json/checkText".replace("PROTOCOL", protocol));
            part = part.replaceAll("\\s+", " ").trim();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.write(("text=" + part).getBytes());
                writer.flush();
                writer.close();

                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    String stringResponse = "";
                    stringResponse = scanner.nextLine();

                    int offset = 0;

                    JsonArray httpResponse = new JsonParser().parse(stringResponse.toString()).getAsJsonArray();
                    for (JsonElement element : httpResponse) {
                        JsonObject object = element.getAsJsonObject();
                        if (object.get("s").getAsJsonArray().size() > 0) {
                            parts.set(i, part = (part.substring(0, (object.get("pos").getAsInt() + offset))
                                    + object.get("s").getAsJsonArray().get(0).getAsString()
                                    + part.substring(object.get("pos").getAsInt() + offset + object.get("word").getAsString().length())));
                            offset += object.get("s").getAsJsonArray().get(0).getAsString().length() - object.get("word").getAsString().length();
                        }
                    }
                }

            } finally {
                connection.disconnect();
            }
        }

        for (String part : parts) {
            result.append(part);
        }

        if (!result.isEmpty())
            return result.toString();
        else return text;
    }

    public static String correctSpell(@NotNull String text) throws Exception {
        return correctSpell(text, "http");
    }

    public static String lemmatize(@NotNull String text) {
        ArrayList<String> words = splitIntoWords(text)
                .orElseGet(() -> new ArrayList<>() {{
                    add(text);
                }});

        for (int i = 0; i < words.size(); i++) {
            List<WordformMeaning> meanings = WordformMeaning.lookupForMeanings(words.get(i));
            if (!meanings.isEmpty()) {
                String word;
                word = WordformMeaning.lookupForMeanings(words.get(i)).get(0).getLemma().toString();
                words.set(i, word);
            }
        }

        return String.join(" ", words);
    }

    public static ArrayList<String> removeRepeats(ArrayList<String> list) {
        return list.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }
}
