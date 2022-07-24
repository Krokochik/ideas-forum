package com.krokochik.CampfireGallery.rest;

import com.krokochik.CampfireGallery.repository.NumbersRepository;
import com.krokochik.CampfireGallery.service.ValueManagerService;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RequestsController {

    public final NumbersRepository numbersRepository = new NumbersRepository();
    public final ValueManagerService valueManagerService = new ValueManagerService();

    @PostMapping(path = "/")
    public Map<String, String> commandsParse(@RequestBody String stringJson) throws ParseException { ;
        short status = 200;
        HashMap<String, String> response = new HashMap<>();
        HashMap<String, String> request = (HashMap<String, String>) new JSONParser(stringJson).parse();
        String command = request.get("command");
        Integer number = -1;
        try {
            switch (command) {
                case "generateRandomNumber" -> number = numbersRepository.generateNumber();
                default -> status = 400;
            }
        }
        catch (NumberFormatException | IndexOutOfBoundsException numberFormatException){ status = 400; } catch (Exception exception){ status = 500; }
        if (status == 200) {
            response.put("number", number + "");
        }
        response.put("status", status + "");
        return response;
    }

    @PostMapping("/repositories/{id}")
    public Map<String, String> repositories(@PathVariable(name = "id") int id, @RequestBody String requestBody) throws ParseException {
        if(valueManagerService.isRepositoryExist(id)) {
            short status = 200;
            System.out.println(1);
            HashMap<String, String> response = new HashMap<>();
            System.out.println(2);
            HashMap<String, String> request = (HashMap<String, String>) new JSONParser(requestBody).parse();
            System.out.println(3);
            switch (request.get("command")) {
                case "addVariable":
                    System.out.println("adV");
                    try {
                        valueManagerService.addVariable(request.get("name"), request.get("value"), id); }
                    catch (NullPointerException e) { status = 400; }
                    catch (Exception e) { status = 500; }
                    break;
                case "getVariableValue":
                    System.out.println("getVV");
                    try {
                        valueManagerService.getVariable(request.get("name"), id);
                    } catch (NullPointerException | IndexOutOfBoundsException nullPointerException) {
                        status = 404;
                    }
                    break;
                case "changeVariableValue":
                    System.out.println("chVV");
                    try {
                        valueManagerService.changeVariable(request.get("name"), request.get("newValue"), id);
                    } catch (NullPointerException nullPointerException) {
                        status = 400;
                    }
                    break;
                default:
                    System.out.println("df");
                    status = 400;
                    break;
            }
            response.put("status", status + "");
            return response;
        } else return new HashMap<>(){{ put("status", "404"); }};
    }
}