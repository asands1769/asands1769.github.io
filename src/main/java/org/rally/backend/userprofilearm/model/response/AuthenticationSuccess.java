package org.rally.backend.userprofilearm.model.response;

public class AuthenticationSuccess {
    private String success;

    public AuthenticationSuccess(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}