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

    @PostMapping(path = "/")
    public Map<String, String> commandsParse(@RequestBody String stringJson) throws ParseException {
        HashMap<String, String> response = new HashMap<>();
        try {
            JsonObject jsonObject = (JsonObject) new JSONParser(stringJson).parse();
            System.out.println(stringJson);
            response.put("request", stringJson);
        }
        catch (Exception e) { System.out.println(e.getMessage()); }
        return response;
    }
}