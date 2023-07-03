package com.krokochik.ideasForum.hcaptcha;

public class HCaptchaRequest {
    final String secret;
    final String response;
    final String sitekey;

    public HCaptchaRequest(String secret, String response, String sitekey) {
        this.secret = secret;
        this.response = response;
        this.sitekey = sitekey;
    }

    public String getSecret() {
        return secret;
    }

    public String getResponse() {
        return response;
    }

    public String getSitekey() {
        return sitekey;
    }
}
