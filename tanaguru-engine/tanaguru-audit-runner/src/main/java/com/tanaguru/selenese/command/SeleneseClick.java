package com.tanaguru.selenese.command;

import com.tanaguru.runner.AuditRunner;
import jp.vmi.selenium.selenese.Context;
import jp.vmi.selenium.selenese.command.AbstractCommand;
import jp.vmi.selenium.selenese.command.ArgumentType;
import jp.vmi.selenium.selenese.result.Result;
import jp.vmi.selenium.selenese.result.Success;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static jp.vmi.selenium.selenese.result.Success.SUCCESS;

/**
 * @author rcharre
 */
public class SeleneseClick extends AbstractCommand {

    private static final int ARG_LOCATOR = 0;
    private final AuditRunner auditRunner;

    /**
     * Constructor.
     *
     * @param index    command index.
     * @param name     command name.
     * @param args     command args.
     * @param argTypes command argument types.
     */
    public SeleneseClick(AuditRunner auditRunner, int index, String name, String[] args, ArgumentType... argTypes) {
        super(index, name, args, ArgumentType.LOCATOR);
        this.auditRunner = auditRunner;
    }

    @Override
    protected Result executeImpl(Context context, String... curArgs) {
        String locator = curArgs[ARG_LOCATOR];
        WebDriver driver = context.getWrappedDriver();
        boolean isRetryable = !context.getCurrentTestCase().getSourceType().isSelenese();
        int timeout = context.getTimeout(); /* ms */
        WebElement element = context.getElementFinder().findElementWithTimeout(driver, locator, isRetryable, timeout);
        context.getJSLibrary().replaceAlertMethod(driver, element);
        try {
            element.click();

            String url = driver.getCurrentUrl();

            //Fire onClick event
            auditRunner.onGetNewPage(url, auditRunner.getDriver().getTitle(), false);
            return SUCCESS;
        } catch (ElementNotInteractableException e) {
            context.executeScript("arguments[0].click()", element);

            //Fire onClick event
            auditRunner.onGetNewPage(driver.getCurrentUrl(), auditRunner.getDriver().getTitle(), false);
            return new Success("Success (the element is not visible)");
        }


    }
}
