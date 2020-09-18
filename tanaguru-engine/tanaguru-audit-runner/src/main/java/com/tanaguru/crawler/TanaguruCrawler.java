package com.tanaguru.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class TanaguruCrawler extends WebCrawler {
    private static final int HTTP_SUCCESS_RETURN_CODE = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(TanaguruCrawler.class);
    private long startedTime;

    @Override
    public void onStart() {
        startedTime = new Date().getTime();
    }


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        TanaguruCrawlerController controller = (TanaguruCrawlerController) myController;

        return controller.getResult().size() < controller.getMaxDocument() &&
                new Date().getTime() - startedTime < controller.getMaxCrawlTime() &&
                super.shouldVisit(referringPage, url) &&
                checkSameDomain(referringPage, url) &&
                checkExclusionRegex(controller, url.getURL()) &&
                checkInclusionRegex(controller, url.getURL());
    }

    protected boolean checkSameDomain(Page referringPage, WebURL url){
        return url.getDomain().equals(referringPage.getWebURL().getDomain());
    }

    protected boolean checkExclusionRegex(TanaguruCrawlerController controller, String url){
        return controller.getExclusionRegex().pattern().isEmpty()
                || !controller.getExclusionRegex().matcher(url).matches();
    }

    protected boolean checkInclusionRegex(TanaguruCrawlerController controller, String url){
        return controller.getInclusionRegex().pattern().isEmpty()
                || controller.getInclusionRegex().matcher(url).matches();
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        if (page.getStatusCode() == HTTP_SUCCESS_RETURN_CODE
                && page.getContentType() != null
                && page.getContentType().contains("text/html")) {
            ((TanaguruCrawlerController) myController).addNewPage(url);
        } else {
            LOGGER.trace("Page {} content does not match html", url);
        }

        if (new Date().getTime() - startedTime >= ((TanaguruCrawlerController) myController).getMaxCrawlTime()) {
            LOGGER.info("[CRAWLER - {}] Crawler time over, stop crawling...", this.getMyId());
            myController.shutdown();
        }
    }
}

