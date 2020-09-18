package com.tanaguru.crawler.factory;

import com.tanaguru.crawler.TanaguruCrawlerController;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
public interface TanaguruCrawlerControllerFactory {
    /**
     * @param seeds          Starting points of the crawlers
     * @param maxDuration    The max duration of the crawling
     * @param inclusionRegex Url to include in the crawling
     * @param exclusionRegex Url to exclude of the crawling
     * @param maxPage        The maximum page to crawl
     * @param maxDepth       The maximum depth to crawl to
     * @return TanaguruCrawlerController
     */
    Optional<TanaguruCrawlerController> create(Collection<String> seeds,
                                               long maxDuration,
                                               String inclusionRegex,
                                               String exclusionRegex,
                                               int maxPage,
                                               int maxDepth,
                                               String basicAuthUrl,
                                               String basicAuthPassword,
                                               String basicAuthLogin);
}
