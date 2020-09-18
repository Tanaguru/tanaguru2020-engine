package com.tanaguru.selenese.command;

import com.tanaguru.runner.AuditRunner;
import jp.vmi.selenium.selenese.Context;
import jp.vmi.selenium.selenese.command.AbstractCommand;
import jp.vmi.selenium.selenese.command.ArgumentType;
import jp.vmi.selenium.selenese.result.Result;
import org.openqa.selenium.WebDriver;

import static jp.vmi.selenium.selenese.result.Success.SUCCESS;

/**
 * @author rcharre
 */
public class SeleneseAudit extends AbstractCommand {
    private static final int ARG_VALUE = 1;
    private final AuditRunner auditRunner;

    public SeleneseAudit(AuditRunner auditRunner, int index, String name, String[] args, ArgumentType... argTypes) {
        super(index, name, args, ArgumentType.LOCATOR, ArgumentType.VALUE);
        this.auditRunner = auditRunner;
    }

    @Override
    protected Result executeImpl(Context context, String... strings) {
        WebDriver driver = context.getWrappedDriver();
        auditRunner.onGetNewPage(driver.getCurrentUrl(), strings[ARG_VALUE], true);
        return SUCCESS;
    }
}
