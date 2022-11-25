package com.krokochik.ideasForum;


import com.github.demidko.aot.WordformMeaning;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;


@SpringBootApplication
public class ideasForum {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[1];

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(ideasForum.class, args);

        Scanner scanner;
        String word;
        URL url;
        HttpURLConnection connection;
        StringBuilder builder;
        List<WordformMeaning> meanings;
        String httpResponse;
        JsonArray jsonArray;
        JsonParser jsonParser;

        scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Введите любое слово русского языка: ");
            word = scanner.nextLine();

            url = new URL("https://speller.yandex.net/services/spellservice.json/checkText?text={TEXT}" // до 10'000 символов за один GET запрос
                    .replace("{TEXT}", word.replace(" ", "+"))); // или 10кб за POST
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                builder = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.lineSeparator());
                }
            } finally {
                connection.disconnect();
            }
            httpResponse = builder.toString();

            jsonParser = new JsonParser();
            try {
                jsonArray = jsonParser.parse(httpResponse).getAsJsonArray();
                if (jsonArray.asList().size() > 0) {
                    jsonArray = jsonArray.get(0).getAsJsonObject().get("s").getAsJsonArray();
                    word = jsonArray.get(0).getAsString();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            meanings = WordformMeaning.lookupForMeanings(word);
            System.out.println(meanings.get(0).getLemma());
        }
    }
}
