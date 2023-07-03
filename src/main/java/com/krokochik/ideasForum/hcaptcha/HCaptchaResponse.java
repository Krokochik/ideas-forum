package com.krokochik.ideasForum.hcaptcha;

import com.google.gson.Gson;

public class HCaptchaResponse {
    boolean success;
    String challenge_ts;
    String hostname;
    boolean credit;
    String[] errorCodes;

    public HCaptchaResponse(boolean success, String challenge_ts, String hostname, boolean credit, String... errorCodes) {
        this.success = success;
        this.challenge_ts = challenge_ts;
        this.hostname = hostname;
        this.credit = credit;
        this.errorCodes = errorCodes;
    }

    public HCaptchaResponse() { }

    public boolean isSuccess() { return success; }

    public String getChallengeTimestamp() { return challenge_ts; }

    public String getChallenge_ts() { return getChallengeTimestamp(); }

    public String getHostname() { return hostname; }

    public boolean isCredit() { return credit; }

    public String[] getErrorCodes() { return errorCodes; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
