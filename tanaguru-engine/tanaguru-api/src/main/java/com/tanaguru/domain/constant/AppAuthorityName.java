package com.tanaguru.domain.constant;

public final class AppAuthorityName {
    // App authorities
    public static final String CREATE_USER = "CREATE_USER";
    public static final String CREATE_CONTRACT = "CREATE_CONTRACT";
    public static final String MODIFY_CONTRACT = "MODIFY_CONTRACT";
    public static final String DELETE_CONTRACT = "DELETE_CONTRACT";
    public static final String SHOW_USER = "SHOW_USER";
    public static final String PROMOTE_USER = "PROMOTE_USER";
    public static final String MODIFY_USER = "MODIFY_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String SHOW_STATISTICS = "SHOW_STATISTICS";

    public static final String CREATE_TEST = "CREATE_TEST";
    public static final String CREATE_REFERENCE = "CREATE_REFERENCE";
    public static final String DELETE_REFERENCE = "DELETE_REFERENCE";

    // Can add/modify/delete schedules on public audit
    public static final String PUBLIC_SCHEDULE_ACCESS = "PUBLIC_SCHEDULE_ACCESS";

    private AppAuthorityName() {
    }
}
