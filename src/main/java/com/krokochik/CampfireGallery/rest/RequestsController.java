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
            System.out.println("string Json    " + stringJson);
            JSONParser parser = new JSONParser(stringJson.toString());
            System.out.println("parser");
            Object obj = parser.parse();
            System.out.println("obj    " + obj.toString());
            JsonObject jsonObject = JsonObject.class.cast(obj);
            System.out.println(jsonObject.get("hash"));
            response.put("request", stringJson);
        }
        catch (Exception e) { System.out.println(e.getMessage()); }
        return response;
    }
}