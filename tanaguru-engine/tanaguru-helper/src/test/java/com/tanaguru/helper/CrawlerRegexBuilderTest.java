package com.tanaguru.helper;

import org.junit.Assert;
import org.junit.Test;

public class CrawlerRegexBuilderTest {
    private static final String BEGIN_REGEX = "(?i)(.*)(";
    private static final String END_REGEX = ")(.*)$";
    private static final String OR_SEPARATOR_REGEX = "|";

    @Test
    public final void testSingleExpression() {
        String parameter = "fr";
        String expectedResult = BEGIN_REGEX + parameter + END_REGEX;
        String test = CrawlerRegexBuilder.buildRegexFromString(parameter);
        Assert.assertEquals(
                expectedResult,
                test
        );
    }

    @Test
    public final void testMultipleExpression() {
        String parameter = "fr;en;de";
        String expectedResult = BEGIN_REGEX + "fr" + OR_SEPARATOR_REGEX + "en" + OR_SEPARATOR_REGEX + "de" + END_REGEX;
        String test = CrawlerRegexBuilder.buildRegexFromString(parameter);
        Assert.assertEquals(
                expectedResult,
                test
        );
    }
}
