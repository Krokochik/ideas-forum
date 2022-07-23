package com.krokochik.CampfireGallery.rest;

import com.google.gson.JsonObject;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RequestsController {

    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> commandsParse(@RequestBody String stringJson) throws ParseException {
        JsonObject jsonObject = (JsonObject) new JSONParser(stringJson).parse();
        HashMap<String, String> response = new HashMap<>();
        response.put("request", stringJson);
        return response;
    }
}