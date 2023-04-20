package com.tanaguru.crawler;

import com.tanaguru.crawler.listener.TanaguruCrawlerListener;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class TanaguruCrawlerControllerImpl extends CrawlController implements TanaguruCrawlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCrawlerControllerImpl.class);
    private static final int NUMBER_OF_CRAWLER = 1;
    private List<String> result = new ArrayList<>();

    private long maxCrawlTime;
    private int maxDocument;
    private Pattern exclusionRegex;
    private Pattern inclusionRegex;

    private Collection<TanaguruCrawlerListener> listeners = new ArrayList<>();

    public TanaguruCrawlerControllerImpl(CrawlConfig crawlerConfig,
    									 BasicURLNormalizer normalizer,
                                         PageFetcher pageFetcher,
                                         RobotstxtServer robotstxtServer,
                                         SleepycatFrontierConfiguration frontierConfig,
                                         long maxCrawlTime,
                                         int maxDocument,
                                         String exclusionRegex,
                                         String inclusionRegex) throws Exception {
        super(crawlerConfig, normalizer, pageFetcher, robotstxtServer, frontierConfig);
        this.maxDocument = maxDocument;
        this.maxCrawlTime = maxCrawlTime * 1000;
        this.exclusionRegex = Pattern.compile(exclusionRegex);
        this.inclusionRegex = Pattern.compile(inclusionRegex);
    }

    public List<String> getResult() {
        return result;
    }

    public Pattern getExclusionRegex() {
        return exclusionRegex;
    }

    public Pattern getInclusionRegex() {
        return inclusionRegex;
    }

    public long getMaxCrawlTime() {
        return maxCrawlTime;
    }

    public void run() {
        super.start(TanaguruCrawler.class, NUMBER_OF_CRAWLER);
    }

    public synchronized void addNewPage(String url) {
        result.add(url);
        for (TanaguruCrawlerListener tanaguruCrawlerListener : listeners) {
            tanaguruCrawlerListener.onCrawlNewPage(url);
        }
        if(result.size() >= maxDocument){
            this.shutdown();
        }
    }

    public void addListener(TanaguruCrawlerListener tanaguruCrawlerListener) {
        this.listeners.add(tanaguruCrawlerListener);
    }

    public void removeListener(TanaguruCrawlerListener tanaguruCrawlerListener) {
        this.listeners.remove(tanaguruCrawlerListener);
    }

    public int getMaxDocument() {
        return maxDocument;
    }


}
