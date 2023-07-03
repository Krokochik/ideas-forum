package com.krokochik.ideasForum.hcaptcha;

import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HCaptchaClient {
    public static final String HCAPTCHA_API_ENDPOINT = "https://hcaptcha.com/siteverify";
    private static final Gson gson = new Gson();

    public static HCaptchaResponse verify(String secret, String response, String sitekey) throws IOException, InterruptedException {
        HCaptchaRequest hCaptchaRequest = new HCaptchaRequest(secret, response, sitekey);
        Map<String, Object> map = new ObjectMapper().readValue(gson.toJson(hCaptchaRequest), Map.class);
        String requestBody = URLEncoder.encode(map.toString(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HCAPTCHA_API_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        return gson.fromJson(httpResponse.body(), HCaptchaResponse.class);
    }
}
