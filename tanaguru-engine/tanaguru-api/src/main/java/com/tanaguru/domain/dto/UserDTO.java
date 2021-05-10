package com.tanaguru.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserDTO {
    private long id;

    @Size(min = 4, max = 30)
    @NotBlank
    private String username;

    @JsonIgnore
    private String password;

    @Email
    private String email;

    @NotNull
    private EAppRole appRole;

    private boolean isEnabled = false;

    private boolean accountNonLocked = true;

    public UserDTO() {
    }

    public UserDTO(User user) {
        new UserDTO(user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getAppRole().getName(),
                user.isEnabled(),
                user.isAccountNonLocked());
    }

    public UserDTO(long id, @Size(min = 4, max = 30) @NotBlank String username, String password, @Email String email, @NotNull EAppRole appRole, boolean isEnabled, boolean accountNonLocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.appRole = appRole;
        this.isEnabled = isEnabled;
        this.accountNonLocked = accountNonLocked;
    }


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

    public UserDTO convertToPublicEntity(){
        this.email = null;
        this.password = null;
        return this;
    }

}
