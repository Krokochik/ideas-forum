package com.krokochik.veryHardSite.rest;

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
    public Map<String, String> commandsParse(String stringJson) throws ParseException {
        HashMap<String, String> response = new HashMap<>();
        try {
            Object object = new JSONParser(stringJson).parse();
            JsonObject jsonObject = (JsonObject) object;
            response.put("request", stringJson);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Exception");
        }
        return response;
    }
}