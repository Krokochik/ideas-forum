package com.krokochik.ideasforum.hcaptcha;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HCaptchaClient {
    public static final String HCAPTCHA_API_ENDPOINT = "https://hcaptcha.com/siteverify";
    private static final Gson gson = new Gson();

    @SneakyThrows
    private static HCaptchaResponse sendRequest(HCaptchaRequest request) {
        URL requestUrl = new URL(HCAPTCHA_API_ENDPOINT);
        HttpsURLConnection connection = ((HttpsURLConnection) requestUrl.openConnection());

        HCaptchaResponse response;

        connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(("secret=" + request.secret + "&response=" + request.response + "&sitekey=" + request.sitekey).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();


        // read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBody = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        } reader.close();

        System.out.println(responseBody);
        try {
            response = gson.fromJson(responseBody.toString(), HCaptchaResponse.class);
        } catch (JsonSyntaxException e) {
            throw new Exception("Unexpected server answer.");
        }

        return response;
    }


    public static HCaptchaResponse verify(String secret, String response, String sitekey) throws IOException, InterruptedException {
        HCaptchaRequest hCaptchaRequest = new HCaptchaRequest(secret, response, sitekey);
        return sendRequest(hCaptchaRequest);
    }
}
