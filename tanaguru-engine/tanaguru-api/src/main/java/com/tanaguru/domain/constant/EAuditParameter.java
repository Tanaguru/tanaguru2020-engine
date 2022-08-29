package com.tanaguru.domain.constant;

/**
 * @author rcharre
 */
public enum EAuditParameter {
    SCENARIO_ID,
    DOM_ID,
    SITE_SEEDS,
    PAGE_URLS,

    WAIT_TIME,
    BASICAUTH_URL,
    BASICAUTH_LOGIN, // AES encrypted
    BASICAUTH_PASSWORD, // AES encrypted
    ENABLE_SCREENSHOT,

    CRAWLER_MAX_DEPTH,
    CRAWLER_MAX_DURATION,
    CRAWLER_MAX_DOCUMENT,
    CRAWLER_EXCLUSION_REGEX,
    CRAWLER_INCLUSION_REGEX,

    WEBDRIVER_RESOLUTIONS,
    WEBDRIVER_BROWSER,
    
    ACCESSIBILITY_PAGE_URL  //param page lancement audit avec l'url de la page accesibilité par exemple
}
