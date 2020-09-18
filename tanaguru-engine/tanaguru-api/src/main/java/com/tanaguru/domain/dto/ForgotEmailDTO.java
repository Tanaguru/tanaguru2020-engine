package com.tanaguru.domain.dto;

import javax.validation.constraints.Email;

public class ForgotEmailDTO {
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
