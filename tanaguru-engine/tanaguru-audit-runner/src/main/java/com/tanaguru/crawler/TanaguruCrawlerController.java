package com.tanaguru.crawler;

import com.tanaguru.crawler.listener.TanaguruCrawlerListener;

import java.util.List;
import java.util.regex.Pattern;

public interface TanaguruCrawlerController {
    List<String> getResult();

    Pattern getExclusionRegex();

    Pattern getInclusionRegex();

    int getMaxDocument();

    long getMaxCrawlTime();

    void run();

    void addNewPage(String url);

    void addListener(TanaguruCrawlerListener tanaguruCrawlerListener);

    void removeListener(TanaguruCrawlerListener tanaguruCrawlerListener);

    void addSeed(String url);

    void waitUntilFinish();

    void shutdown();
}
