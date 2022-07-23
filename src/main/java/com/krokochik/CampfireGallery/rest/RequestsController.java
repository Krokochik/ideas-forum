package com.krokochik.CampfireGallery.rest;

import com.krokochik.CampfireGallery.repository.NumbersRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.web.bind.annotation.*;
import com.krokochik.CampfireGallery.model.Number;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RequestsController {

    private NumbersRepository numbersRepository = new NumbersRepository();

    @PostMapping(path = "/")
    public Map<String, String> commandsParse(@RequestBody String stringJson) throws ParseException {
        short status = 200;
        HashMap<String, String> response = new HashMap<>();
        HashMap<String, String> request = (HashMap<String, String>) new JSONParser(stringJson).parse();
        String command = request.get("command");
        Number number = new Number();
        try {
            switch (command) {
                case "generateRandomNumber" -> number = numbersRepository.generateNumber();
                case "getNumberById" -> number = numbersRepository.getNumber(Integer.parseInt(request.get("id")));
            }
        }
        catch (NumberFormatException numberFormatException){ status = 400; }
        catch (IndexOutOfBoundsException indexOutOfBoundsException){ status = 400; }
        catch (Exception exception){ status = 500; }
        if (status == 200) {
            response.put("id", number.getId() + "");
            response.put("number", number.getValue() + "");
        }
        response.put("status", status + "");
        return response;
    }
}