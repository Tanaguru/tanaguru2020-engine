package com.tanaguru.selenese.command;

import com.tanaguru.runner.AuditRunner;
import jp.vmi.selenium.selenese.Context;
import jp.vmi.selenium.selenese.SeleneseRunnerRuntimeException;
import jp.vmi.selenium.selenese.command.AbstractCommand;
import jp.vmi.selenium.selenese.result.Result;
import jp.vmi.selenium.selenese.result.Success;

import java.net.URI;
import java.net.URISyntaxException;

import static jp.vmi.selenium.selenese.command.ArgumentType.VALUE;

public class SeleneseOpen extends AbstractCommand {
    private final AuditRunner auditRunner;

    public SeleneseOpen(AuditRunner auditRunner, int index, String name, String[] args) {
        super(index, name, args, VALUE);
        this.auditRunner = auditRunner;
    }

    protected Result executeImpl(Context context, String... curArgs) {
        String url = curArgs[0];
        if (!url.contains("://")) {
            String baseURL = context.getCurrentBaseURL();
            if (!baseURL.isEmpty() && baseURL.charAt(baseURL.length() - 1) != '/') {
                baseURL = baseURL + "/";
            }

            try {
                url = (new URI(baseURL)).resolve(url).toASCIIString();
            } catch (URISyntaxException var6) {
                throw new SeleneseRunnerRuntimeException("Invalid URL: baseURL=[" + baseURL + "] / parameter=[" + url + "]", var6);
            }
        }

        auditRunner.webDriverGet(url);
        return Success.SUCCESS;
    }
}
