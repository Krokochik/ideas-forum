package com.krokochik.ideasForum.hcaptcha;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HCaptchaClient {
    public static final String HCAPTCHA_API_ENDPOINT = "https://hcaptcha.com/siteverify";
    private static final Gson gson = new Gson();

    public static HCaptchaResponse verify(String secret, String response, String sitekey) throws IOException, InterruptedException {
        HCaptchaRequest hCaptchaRequest = new HCaptchaRequest(secret, response, sitekey);
        String json = gson.toJson(hCaptchaRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HCAPTCHA_API_ENDPOINT))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        return gson.fromJson(httpResponse.body(), HCaptchaResponse.class);
    }
}
