package com.tanaguru.domain.dto;

import com.tanaguru.domain.constant.EAppRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserDTO {
    private long id;

    @Size(min = 4, max = 100)
    @NotBlank
    private String username;

    private String password;

    @Email
    private String email;

    @NotNull
    private EAppRole appRole;

    private boolean isEnabled = false;

    private boolean accountNonLocked = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public EAppRole getAppRole() {
        return appRole;
    }

    public void setAppRole(EAppRole appRole) {
        this.appRole = appRole;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
}
