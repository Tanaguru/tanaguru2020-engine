package com.tanaguru.crawler.factory;

import com.tanaguru.crawler.TanaguruCrawlerController;
import com.tanaguru.crawler.TanaguruCrawlerControllerImpl;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Component
public class TanaguruCrawlerControllerFactoryImpl implements TanaguruCrawlerControllerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCrawlerControllerFactoryImpl.class);

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

    private void prepareEnv() {
        File outDir = new File(outputDir);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("Unable to create crawler output directory");
        }
    }

    public Optional<TanaguruCrawlerController> create(
            Collection<String> seeds,
            long maxDuration,
            String inclusionRegex,
            String exclusionRegex,
            int maxPage,
            int maxDepth,
            String basicAuthUrl,
            String basicAuthPassword,
            String basicAuthLogin) {

        TanaguruCrawlerController crawlerController = null;

        this.prepareEnv();
        try {
            CrawlConfig crawlerConfig = getCrawlerConfig(maxDepth, basicAuthUrl, basicAuthLogin, basicAuthPassword, seeds);
            PageFetcher pageFetcher = new PageFetcher(crawlerConfig);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
            crawlerController = new TanaguruCrawlerControllerImpl(
                    crawlerConfig,
                    pageFetcher,
                    robotstxtServer,
                    maxDuration,
                    maxPage,
                    exclusionRegex,
                    inclusionRegex
            );

            for (String seed : seeds) {
                crawlerController.addSeed(seed);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return Optional.ofNullable(crawlerController);
    }

    private CrawlConfig getCrawlerConfig(int maxDepth, String basicAuthUrl, String basicAuthLogin, String basicAuthPassword, Collection<String> seeds) throws Exception {
        CrawlConfig crawlerConfig = new CrawlConfig();
        String auditDir = String.valueOf(new Date().getTime());
        crawlerConfig.setCrawlStorageFolder(outputDir + auditDir);
        crawlerConfig.setMaxDepthOfCrawling(maxDepth);
        crawlerConfig.setFollowRedirects(true);
        crawlerConfig.setIncludeHttpsPages(true);
        crawlerConfig.setUserAgentString("tanaguru");

        if(!basicAuthLogin.isEmpty()){
            crawlerConfig.addAuthInfo(
                    new BasicAuthInfo(basicAuthLogin, basicAuthPassword, basicAuthUrl)
            );
        }

        setupProxy(crawlerConfig, seeds);
        crawlerConfig.validate();
        return crawlerConfig;
    }

    private void setupProxy(CrawlConfig crawlConfig, Collection<String> seeds){
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
