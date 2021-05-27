package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;

public class DetailedUserDTO {
    private long id;

    private String username;

    private String email;

    private AppRole appRole;

    private boolean isEnabled;

    private boolean accountNonLocked;

    public DetailedUserDTO() {
    }

    public DetailedUserDTO(User user) {
        this(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAppRole(),
                user.isEnabled(),
                user.isAccountNonLocked());
    }

    public DetailedUserDTO(long id, String username, String email, AppRole appRole, boolean isEnabled, boolean accountNonLocked) {
        this.id = id;
        this.username = username;
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

    public AppRole getAppRole() {
        return appRole;
    }

    public void setAppRole(AppRole appRole) {
        this.appRole = appRole;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public DetailedUserDTO convertToPublicEntity(){
        this.email = null;
        return this;
    }
}
