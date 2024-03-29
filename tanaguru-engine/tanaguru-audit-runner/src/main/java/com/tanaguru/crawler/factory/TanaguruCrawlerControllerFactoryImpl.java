package com.tanaguru.crawler.factory;

import com.tanaguru.crawler.TanaguruCrawlerController;
import com.tanaguru.crawler.TanaguruCrawlerControllerImpl;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Component
public class TanaguruCrawlerControllerFactoryImpl implements TanaguruCrawlerControllerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCrawlerControllerFactoryImpl.class);
    private static final String USER_AGENT_NAME = "tanaguru";

    @Value("${auditrunner.crawler.outputDir}")
    private String outputDir;

    @Value("${auditrunner.proxy.host}")
    private String proxyHost;

    @Value("${auditrunner.proxy.port}")
    private Integer proxyPort;

    @Value("${auditrunner.proxy.username}")
    private String proxyUsername;

    @Value("${auditrunner.proxy.password}")
    private String proxyPassword;

    @Value("${auditrunner.proxy.exclusionUrls}")
    private String[] proxyExclusionUrls;

    @Value("${auditrunner.crawler.follow-robots}")
    private boolean followRobots;

    private void prepareEnv() {
        File outDir = new File(outputDir);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("Unable to create crawler output directory");
        }
    }

    public TanaguruCrawlerController create(
            Collection<String> seeds,
            long maxDuration,
            String inclusionRegex,
            String exclusionRegex,
            int maxPage,
            int maxDepth,
            String basicAuthUrl,
            String basicAuthLogin,
            String basicAuthPassword) throws Exception {

        LOGGER.debug("Create crawler controller");
        this.prepareEnv();
        CrawlConfig crawlerConfig = getCrawlerConfig(maxDepth, basicAuthUrl, basicAuthLogin, basicAuthPassword, seeds);
        BasicURLNormalizer normalizer = BasicURLNormalizer.newBuilder().idnNormalization(BasicURLNormalizer.IdnNormalization.NONE).build();
        PageFetcher pageFetcher = new PageFetcher(crawlerConfig, normalizer);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setUserAgentName(USER_AGENT_NAME);
        robotstxtConfig.setEnabled(followRobots);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, new SleepycatWebURLFactory());
        TanaguruCrawlerController crawlerController = new TanaguruCrawlerControllerImpl(
                crawlerConfig,
                normalizer,
                pageFetcher,
                robotstxtServer,
                new SleepycatFrontierConfiguration(crawlerConfig),
                maxDuration,
                maxPage,
                exclusionRegex,
                inclusionRegex
        );

        for (String seed : seeds) {
            crawlerController.addSeed(seed);
        }

        return crawlerController;
    }

    private CrawlConfig getCrawlerConfig(int maxDepth, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, Collection<String> seeds) throws Exception {
        CrawlConfig crawlerConfig = new CrawlConfig();
        String auditDir = String.valueOf(new Date().getTime());
	    File crawlStorage = new File(outputDir + auditDir);
	    FileUtils.forceMkdir(crawlStorage);
        crawlerConfig.setCrawlStorageFolder(outputDir + auditDir);
        crawlerConfig.setMaxDepthOfCrawling(maxDepth);
        crawlerConfig.setFollowRedirects(true);
        crawlerConfig.setIncludeHttpsPages(true);
        crawlerConfig.setUserAgentString(USER_AGENT_NAME);
        crawlerConfig.setPolitenessDelay(10);
        crawlerConfig.setRespectNoFollow(false);
        crawlerConfig.setRespectNoIndex(false);

        if (!basicAuthUrl.isEmpty() && !basicAuthLogin.isEmpty() && !basicAuthPassword.isEmpty()) {
            crawlerConfig.addAuthInfo(
                    new BasicAuthInfo(basicAuthLogin, basicAuthPassword, basicAuthUrl)
            );
        }

        setupProxy(crawlerConfig, seeds);
        crawlerConfig.validate();
        return crawlerConfig;
    }

    private void setupProxy(CrawlConfig crawlConfig, Collection<String> seeds) {
        boolean noProxy = Arrays.stream(proxyExclusionUrls)
                .anyMatch((exclusionUrl) ->
                        seeds.stream()
                                .anyMatch((seed) -> seed.contains(exclusionUrl)));

        if (!noProxy && proxyPort != null && !proxyHost.isEmpty()) {
            if (!proxyUsername.isEmpty() && !proxyPassword.isEmpty()) {
                crawlConfig.setProxyPassword(proxyPassword);
                crawlConfig.setProxyUsername(proxyUsername);
            }

            crawlConfig.setProxyHost(proxyHost);
            crawlConfig.setProxyPort(proxyPort);
        }
    }
}
