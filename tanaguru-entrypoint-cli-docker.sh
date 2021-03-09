#!/bin/bash
set -e
echo
echo "tanaguru-entrypoint custom cli launched"
echo

echo "Container - Init Script - Tanaguru initialization"
echo

echo "Apply tanaguru cli configuration"
sed -i "s;spring.main.web-application-type.*;spring.main.web-application-type=${WEB_ENV};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.datasource.url.*;spring.datasource.url=${DB_URL};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.jpa.properties.hibernate.default_schema.*;spring.jpa.properties.hibernate.default_schema=${DB_SCHEMA};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.datasource.username.*;spring.datasource.username=${DB_USERNAME};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.datasource.password.*;spring.datasource.password=${DB_PASSWORD};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.from.*;spring.mail.properties.mail.smtp.from=${MAIL_FROM_ADDRESS};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.host.*;spring.mail.host=${MAIL_HOST};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.port.*;spring.mail.port=${MAIL_PORT};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.username.*;spring.mail.username=${MAIL_USERNAME};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.password.*;spring.mail.password=${MAIL_PASSWORD};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.starttls.enable.*;spring.mail.properties.mail.smtp.starttls.enable=${MAIL_TTLS_ENABLED};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;spring.mail.properties.mail.smtp.auth.*;spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;crypto.key.*;crypto.key=${CRYPTO_KEY};g" /opt/tanaguru-cli-docker/config/common.properties
sed -i "s;password.tokenValidity.*;password.tokenValidity=${PASSWORD_TOKEN_VALIDITY};g" /opt/tanaguru-cli-docker/config/common.properties

sed -i "s;auditrunner.chrome-binary.*;auditrunner.chrome-binary=/usr/bin/google-chrome;g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.chromedriver.*;auditrunner.chromedriver=/usr/bin/chromedriver;g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.firefox-binary.*;auditrunner.firefox-binary=/opt/firefox/firefox;g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.geckodriver.*;auditrunner.geckodriver=/opt/geckodriver;g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.active.*;auditrunner.active=firefox,chrome;g" /opt/tanaguru-cli-docker/config/audit-runner.properties

sed -i "s;auditrunner.proxy.exclusionUrls.*;auditrunner.proxy.exclusionUrls=${AUDITRUNNER_PROXY_EXCLUSION_URLS};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.proxy.username.*;auditrunner.proxy.username=${AUDITRUNNER_PROXY_USERNAME};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.proxy.password.*;auditrunner.proxy.password=${AUDITRUNNER_PROXY_PASSWORD};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.proxy.port.*;auditrunner.proxy.port=${AUDITRUNNER_PROXY_PORT};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.proxy.host.*;auditrunner.proxy.host=${AUDITRUNNER_PROXY_HOST};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.implicitlyWait.*;auditrunner.implicitlyWait=${AUDITRUNNER_IMPLICIT_WAIT};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.pageLoadTimeout.*;auditrunner.pageLoadTimeout=${AUDITRUNNER_PAGE_LOAD_TIMEOUT};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.scriptTimeout.*;auditrunner.scriptTimeout=${AUDITRUNNER_SCRIPT_TIMEOUT};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.firefox.profile.*;auditrunner.firefox.profile=${AUDITRUNNER_FIREFOX_PROFILE};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.chrome.profile.*;auditrunner.chrome.profile=${AUDITRUNNER_CHROME_PROFILE};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.maxConcurrentAudit.*;auditrunner.maxConcurrentAudit=${AUDITRUNNER_MAX_CONCURRENT_AUDITS};g" /opt/tanaguru-cli-docker/config/audit-runner.properties
sed -i "s;auditrunner.profile.*;auditrunner.profile=${AUDITRUNNER_PROFILE};g" /opt/tanaguru-cli-docker/config/audit-runner.properties

echo "Launch Tanaguru CLI Custom"
chmod +x /opt/tanaguru-cli-docker/src/main/resources/bin/tanaguru-cli-docker.sh
/opt/tanaguru-cli-docker/src/main/resources/bin/tanaguru-cli-docker.sh "$@"

echo "Container - End Script - Tanaguru initialization"

