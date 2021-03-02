#!/bin/bash
set -e
echo
echo "tanaguru-entrypoint launched"
echo

echo "Container - Init Script - Tanaguru initialization"
echo

echo "Apply tanaguru-rest configuration"
sed -i "s;server.address.*;server.address=${SERVER_ADDRESS};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;server.port.*;server.port=${SERVER_PORT};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.datasource.url.*;spring.datasource.url=${DB_URL};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.jpa.properties.hibernate.default_schema.*;spring.jpa.properties.hibernate.default_schema=${DB_SCHEMA};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.datasource.username.*;spring.datasource.username=${DB_USERNAME};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.datasource.password.*;spring.datasource.password=${DB_PASSWORD};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.from.*;spring.mail.properties.mail.smtp.from=${MAIL_FROM_ADDRESS};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.host.*;spring.mail.host=${MAIL_HOST};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.port.*;spring.mail.port=${MAIL_PORT};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.username.*;spring.mail.username=${MAIL_USERNAME};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.password.*;spring.mail.password=${MAIL_PASSWORD};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.starttls.enable.*;spring.mail.properties.mail.smtp.starttls.enable=${MAIL_TTLS_ENABLED};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.auth.*;spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;crypto.key.*;crypto.key=${CRYPTO_KEY};g" /opt/tanaguru-rest/config/common.properties
sed -i "s;password.tokenValidity.*;password.tokenValidity=${PASSWORD_TOKEN_VALIDITY};g" /opt/tanaguru-rest/config/common.properties

sed -i "s;auditrunner.chrome-binary.*;auditrunner.chrome-binary=/usr/bin/google-chrome;g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.chromedriver.*;auditrunner.chromedriver=/usr/bin/chromedriver;g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.firefox-binary.*;auditrunner.firefox-binary=/opt/firefox/firefox;g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.geckodriver.*;auditrunner.geckodriver=/opt/geckodriver;g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.active.*;auditrunner.active=chrome,firefox;g" /opt/tanaguru-rest/config/audit-runner.properties

sed -i "s;auditrunner.proxy.exclusionUrls.*;auditrunner.proxy.exclusionUrls=${AUDITRUNNER_PROXY_EXCLUSION_URLS};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.proxy.username.*;auditrunner.proxy.username=${AUDITRUNNER_PROXY_USERNAME};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.proxy.password.*;auditrunner.proxy.password=${AUDITRUNNER_PROXY_PASSWORD};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.proxy.port.*;auditrunner.proxy.port=${AUDITRUNNER_PROXY_PORT};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.proxy.host.*;auditrunner.proxy.host=${AUDITRUNNER_PROXY_HOST};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.implicitlyWait.*;auditrunner.implicitlyWait=${AUDITRUNNER_IMPLICIT_WAIT};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.pageLoadTimeout.*;auditrunner.pageLoadTimeout=${AUDITRUNNER_PAGE_LOAD_TIMEOUT};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.scriptTimeout.*;auditrunner.scriptTimeout=${AUDITRUNNER_SCRIPT_TIMEOUT};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.firefox.profile.*;auditrunner.firefox.profile=${AUDITRUNNER_FIREFOX_PROFILE};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.chrome.profile.*;auditrunner.chrome.profile=${AUDITRUNNER_CHROME_PROFILE};g" /opt/tanaguru-rest/config/audit-runner.properties
sed -i "s;auditrunner.maxConcurrentAudit.*;auditrunner.maxConcurrentAudit=${AUDITRUNNER_MAX_CONCURRENT_AUDITS};g" /opt/tanaguru-rest/config/audit-runner.properties

sed -i "s;cors.origins.*;cors.origins=${CORS_ORIGIN};g" /opt/tanaguru-rest/config/rest.properties
sed -i "s;webapp.url.*;webapp.url=${WEBAPP_URL};g" /opt/tanaguru-rest/config/rest.properties
sed -i "s;server.servlet.session.timeout.*;server.servlet.session.timeout=${SESSION_TIMEOUT};g" /opt/tanaguru-rest/config/rest.properties

echo "Launch Tanaguru CLI"
chmod +x /opt/tanaguru-rest/tanaguru-cli/src/main/resources/bin/tanaguru-cli.sh
/opt/tanaguru-rest/tanaguru-cli/src/main/resources/bin/tanaguru-cli.sh

echo "Container - End Script - Tanaguru initialization"

