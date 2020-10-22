package com.tanaguru.helper;

import org.apache.commons.validator.routines.UrlValidator;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
public class UrlHelper {
    private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);

    private UrlHelper() {
    }

    /**
     * Test an url validity
     *
     * @param url The url to check
     * @return The validity of the url
     */
    public static boolean isValid(String url) {
        boolean result = true;
        try {
            URL u = new URL(url);
            u.toURI();
        }catch (Exception e){
            result = false;
        }
        return result;
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
