package com.tanaguru.crawler;

import com.tanaguru.helper.CrawlerRegexBuilder;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.regex.Pattern;

@RunWith(MockitoJUnitRunner.class)
public class TanaguruCrawlerImplTest {
    @Mock
    TanaguruCrawlerController tanaguruCrawlerController;

    @InjectMocks
    private TanaguruCrawler tanaguruCrawler;



    @Before
    public void setup(){
        tanaguruCrawler.onStart();
    }

    @Test
    public void checkSameDomainTest_Valid(){
        WebURL webURL = new WebURL();
        webURL.setURL("http://test.com/");
        Page referringPage = new Page(webURL);

        WebURL toCheck = new WebURL();
        toCheck.setURL("http://test.com/test");

        Assert.assertTrue(tanaguruCrawler.checkSameDomain(referringPage, toCheck));
    }

    @Test
    public void checkSameDomainTest_Invalid(){
        WebURL webURL = new WebURL();
        webURL.setURL("http://test.com/");
        Page referringPage = new Page(webURL);

        WebURL toCheck = new WebURL();
        toCheck.setURL("http://test2.com/test");

        Assert.assertFalse(tanaguruCrawler.checkSameDomain(referringPage, toCheck));
    }

    @Test
    public void checkExclusionRegexTest_Empty_Valid(){
        Mockito.when(tanaguruCrawlerController.getExclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString(""))
        );

        Assert.assertTrue(tanaguruCrawler.checkExclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkExclusionRegexTest_Valid(){
        Mockito.when(tanaguruCrawlerController.getExclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2"))
        );

        Assert.assertTrue(tanaguruCrawler.checkExclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkExclusionRegexTest_Invalid(){
        Mockito.when(tanaguruCrawlerController.getExclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test"))
        );

        Assert.assertFalse(tanaguruCrawler.checkExclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkExclusionRegexTest_ValidMultiple(){
        Mockito.when(tanaguruCrawlerController.getExclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2;test3"))
        );

        Assert.assertTrue(tanaguruCrawler.checkExclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkExclusionRegexTest_InvalidMultiple(){
        Mockito.when(tanaguruCrawlerController.getExclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2;test"))
        );

        Assert.assertFalse(tanaguruCrawler.checkExclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkInclusionRegexTest_Empty_Valid(){
        Mockito.when(tanaguruCrawlerController.getInclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString(""))
        );

        Assert.assertTrue(tanaguruCrawler.checkInclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkInclusionRegexTest_Invalid(){
        Mockito.when(tanaguruCrawlerController.getInclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2"))
        );

        Assert.assertFalse(tanaguruCrawler.checkInclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkInclusionRegexTest_Valid(){
        Mockito.when(tanaguruCrawlerController.getInclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test"))
        );

        Assert.assertTrue(tanaguruCrawler.checkInclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkInclusionRegexTest_InvalidMultiple(){
        Mockito.when(tanaguruCrawlerController.getInclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2;test3"))
        );

        Assert.assertFalse(tanaguruCrawler.checkInclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }

    @Test
    public void checkInclusionRegexTest_ValidMultiple(){
        Mockito.when(tanaguruCrawlerController.getInclusionRegex()).thenReturn(
                Pattern.compile(CrawlerRegexBuilder.buildRegexFromString("test2;test"))
        );

        Assert.assertTrue(tanaguruCrawler.checkInclusionRegex(tanaguruCrawlerController, "http://test.com"));
    }
}
