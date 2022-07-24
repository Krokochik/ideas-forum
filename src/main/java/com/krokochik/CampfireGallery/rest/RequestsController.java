package com.krokochik.CampfireGallery.rest;

import com.krokochik.CampfireGallery.repository.NumbersRepository;
import com.krokochik.CampfireGallery.service.ValueManagerService;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RequestsController {

    private final NumbersRepository numbersRepository = new NumbersRepository();
    private final ValueManagerService valueManagerService = new ValueManagerService();

    @PostMapping(path = "/")
    public Map<String, String> commandsParse(@RequestBody String stringJson, HttpServletResponse httpServletResponse) throws ParseException { ;
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
        httpServletResponse.setStatus(status);
        return response;
    }

    @PostMapping("/repositories/{id}")
    public Map<String, String> repositories(@PathVariable(name = "id") int id, @RequestBody String requestBody, HttpServletResponse httpServletResponse, HttpServletRequest Rrequest)
            throws ParseException, IOException {
        System.out.println(Rrequest.getRemoteHost());
        System.out.println(Rrequest.getServerName());
        System.out.println(Rrequest.getRemoteUser());
        System.out.println(Rrequest.getRemoteAddr());
        System.out.println(Rrequest.getRemotePort());
        System.out.println(Rrequest.getLocalName());
        System.out.println(Rrequest.getLocalAddr());
        System.out.println(Rrequest.getSession().getAttributeNames());
        System.out.println(Rrequest.getSession().getId());
        System.out.println(Arrays.toString(Rrequest.getSession().getValueNames()));
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getByName("ap-plication.herokuapp.com");
            System.out.println("ur: " + ip.getHostAddress());
            StringBuilder builder = new StringBuilder();
            for( byte el : ip.getAddress() ) {
                builder.append(el);
            }
            String ipAddress = builder.toString();
            hostname =  ip.getHostName();
            System.out.println("Your current hostname: " + hostname) ;
        } catch(UnknownHostException e) {
            System.out.println("exc");
        }
        if(valueManagerService.isRepositoryExist(id)) {
            short status = 200;
            HashMap<String, String> response = new HashMap<>();
            HashMap<String, String> request = (HashMap<String, String>) new JSONParser(requestBody).parse();
            switch (request.get("command")) {
                case "addVariable":
                    try {
                        valueManagerService.addVariable(request.get("name"), request.get("value"), id);
                        status = 201;
                    }
                    catch (NullPointerException e) { status = 400; }
                    catch (Exception e) { status = 500; }
                    break;
                case "getVariableValue":
                    try {
                        response.put("value", valueManagerService.getVariable(request.get("name"), id));
                    }
                    catch (NullPointerException | IndexOutOfBoundsException nullPointerException) { status = 404; }
                    break;
                case "changeVariableValue":
                    try {
                        valueManagerService.changeVariable(request.get("name"), request.get("newValue"), id); }
                    catch (NullPointerException nullPointerException) { status = 400; }
                    break;
                default:
                    status = 400;
                    break;
            }
            response.put("status", status + "");
            httpServletResponse.setStatus(status);
            return response;
        }
        httpServletResponse.setStatus(404);
        return new HashMap<>(){{ put("status", "404"); }};
    }
}