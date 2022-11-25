package com.krokochik.ideasForum.service;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class SpellMorphService {
    public static Optional<ArrayList<String>> splitIntoWords(@NotNull String text) {
        text = text.replaceAll("[^A-Za-zА-Яа-я0-9 \n]", "").replaceAll("\n", " ");
        if (text.replace(" ", "").length() >= 1)
            if (text.contains(" "))
                return Optional.of((ArrayList<String>) Arrays.stream(text.split(" ")).toList());
            else
                return Optional.of((ArrayList<String>) Stream.of(text).toList());
        else return Optional.empty();
    }

    public static String[] splitIntoParts(@NotNull String text, double maxPartLength) {
        if (text.length() > maxPartLength) {
            ArrayList<String> textParts = new ArrayList<>();
            final double partsCount = Math.ceil(text.length() / maxPartLength);
            final double partLength = text.length() / partsCount;

            for (int i = 0; i < partsCount - 1; i++) {
                textParts.add(text.substring((int) partLength * i, (int) partLength * (i + 1)));
            }
            textParts.add(text.substring((int) (text.length() - partLength)));

            for (int i = 0; i < textParts.size(); i++) {
                if (!textParts.get(i).endsWith(" ") && !textParts.get(i+1).startsWith(" ")) {

                }
            }

            String[] res = new String[textParts.size()];
            for (int i = 0; i < textParts.size(); i++) {
                res[i] = textParts.get(i);
            }
            return res;

        }
        return new String[]{text};
    }

    public static String[] splitIntoParts(@NotNull String text) {
        return splitIntoParts(text, 10_000); // 10_000 - макс длина запроса Ya. Speller
    }
}
