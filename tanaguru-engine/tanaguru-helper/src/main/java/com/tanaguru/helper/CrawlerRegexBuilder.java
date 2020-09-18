package com.tanaguru.helper;

/**
 * @author rcharre
 */
public class CrawlerRegexBuilder {
    private static final String BEGIN_REGEX = "(?i)(.*)(";
    private static final String END_REGEX = ")(.*)$";
    private static final String OR_SEPARATOR_REGEX = "|";

    private CrawlerRegexBuilder() {
    }

    /**
     * Create a regex to use with the TanaguruCrawlerController
     *
     * @param rawList A String of multiple element separated by a ';'
     * @return A String containing the regex
     */
    public static String buildRegexFromString(String rawList) {
        String result = "";
        if (rawList != null && !rawList.isEmpty()) {
            String[] regexList = rawList.split(";");
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append(BEGIN_REGEX);
            boolean firstOccrurence = true;
            for (String regexPart : regexList) {
                if (!firstOccrurence) {
                    resultBuilder.append(OR_SEPARATOR_REGEX);
                } else {
                    firstOccrurence = false;
                }
                resultBuilder.append(regexPart);
            }
            resultBuilder.append(END_REGEX);
            result = resultBuilder.toString();
        }
        return result;
    }
}
