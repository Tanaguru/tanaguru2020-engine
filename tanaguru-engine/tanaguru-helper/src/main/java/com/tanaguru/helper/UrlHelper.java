package com.tanaguru.helper;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
public class UrlHelper {
    public static final UrlValidator urlValidator = new UrlValidator();

    private UrlHelper() {
    }

    /**
     * Test an url validity
     *
     * @param url The url to check
     * @return The validity of the url
     */
    public static boolean isValid(String url) {
        return urlValidator.isValid(url);
    }

    /**
     * Filter an urls list on their validity
     *
     * @param urls The urls list to filter
     * @return The filtered irls list
     */
    public static Collection<String> filterUrlList(Collection<String> urls) {
        return urls.stream()
                .filter(UrlHelper::isValid)
                .collect(Collectors.toList());
    }
}
