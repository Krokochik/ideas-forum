package com.krokochik.ideasForum.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController("/mfa/")
public class MFAController {

    @ResponseBody
    @GetMapping(value = "/ping", produces = "application/json")
    public HashMap<String, Object> ping(HttpServletResponse response) {
        response.setStatus(200);
        return new HashMap<>(){{
            put("response", "pong");
        }};
    }

}
