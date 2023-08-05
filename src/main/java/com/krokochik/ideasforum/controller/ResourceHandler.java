package com.krokochik.ideasforum.controller;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceHandler {

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping(value = "/mfa-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> qrCode() {
        User user = userService
                .findByUsernameOrUnknown(srp.getContext().getAuthentication().getName());
        if (user.getQrcode() != null) {
            Resource resource = new ByteArrayResource(user.getQrcode());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qrcode.png")
                    .body(resource);
        } else return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] avatar(HttpServletResponse response) {
        byte[] decodedAvatar;
        if (srp.isAuthenticated()) {
            User user = userService
                    .findByUsernameOrUnknown(srp.getContext().getAuthentication().getName());
            decodedAvatar = Base64.decodeBase64(user.getAvatar());
        } else {
            decodedAvatar = Base64.decodeBase64(new User().getAvatar());
        }
        return decodedAvatar;
    }
}
