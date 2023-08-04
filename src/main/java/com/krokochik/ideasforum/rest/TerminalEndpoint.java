package com.krokochik.ideasforum.rest;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Slf4j
@Controller
@RequestMapping("terminal")
public class TerminalEndpoint {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public String model() {
        return "terminal";
    }

    @PostMapping
    @ResponseBody
    public HashMap<String, Object> processCommand(@RequestBody @NonNull HashMap<String, String> request, HttpServletResponse response) {
        request.forEach((s, s2) -> System.out.println(s + ": " + s2));
        HashMap<String, Object> responseBody = new HashMap<>();
        String responseStr;
        int responseCode = 200;

        String sql;
        if ((sql = request.get("sql")) != null) {
            System.out.println("reached");
            try {
                jdbcTemplate.queryForList(sql).forEach((s) -> s.forEach((s1, o) -> System.out.println(s1 + ": " + o)));
                responseStr = "hello :)";
            } catch (Exception exc) {
//                String cause = sqlExc.getCause().toString();
//                responseStr = cause.substring(cause.indexOf(':') + 1).trim();
                log.error("An error occurred", exc);
                responseStr = "";
            }
        } else {
            responseCode = 400;
            responseStr = HttpStatus.resolve(responseCode).getReasonPhrase();
        }

        responseBody.put("response", responseStr);
        response.setStatus(responseCode);
        return responseBody;
    }

}
