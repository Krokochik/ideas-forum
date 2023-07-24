package com.krokochik.ideasForum.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

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
    public HashMap<String, Object> processCommand(@RequestBody HashMap<String, String> request, HttpServletResponse response) {
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
            } catch (Exception sqlExc) {
//                String cause = sqlExc.getCause().toString();
//                responseStr = cause.substring(cause.indexOf(':') + 1).trim();
                sqlExc.printStackTrace();
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
