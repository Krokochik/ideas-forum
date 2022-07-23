package com.krokochik.CampfireGallery.rest;

import com.google.gson.JsonObject;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
public class requestsController {

    @PostMapping("/")
    public Map<String, String> commandsParse(String stringJson) throws ParseException {
        JsonObject jsonObject = (JsonObject) new JSONParser(stringJson).parse();
        HashMap<String, String> response = new HashMap<>();
        response.put("request", stringJson);
        return response;
    }
}