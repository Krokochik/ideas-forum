package com.krokochik.ideasForum.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping("mfa")
public class MFAController {

    @GetMapping(value = "/ping", produces = "application/json")
    public HashMap<String, Object> ping(HttpServletResponse response) {
        response.setStatus(200);
        return new HashMap<>(){{
            put("response", "pong");
        }};
    }

}
