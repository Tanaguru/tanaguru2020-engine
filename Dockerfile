FROM ubuntu:20.04
USER root
WORKDIR /

ARG TANAGURU_CLI_ARCHIVE_PATH
ARG CHROME_DRIVER_VERSION="89.0.4389.23"
ARG FIREFOX_VERSION="69.0"
ARG GECKODRIVER_VERSION="0.21.0"
ARG CHROME_VERSION="google-chrome-stable"

RUN cd /opt                                                                                                                             && \
    apt-get update                                                                                                                      && \
    apt-get install -y openjdk-11-jre ca-certificates bzip2 gnupg2 wget unzip libgtk-3-0 libdbus-glib-1-2 libxt6 libx11-xcb1

#MAIL
RUN apt-get install -y postfix mailutils
RUN sed -i 's/inet_interfaces = all/inet_interfaces = loopback-only/g' /etc/postfix/main.cf
RUN apt-get install -y locales

#CHROME
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
  && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
  && apt-get update -qqy \
  && apt-get -qqy install \
    ${CHROME_VERSION:-google-chrome-stable} \
  && rm /etc/apt/sources.list.d/google-chrome.list \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

RUN if [ -z "$CHROME_DRIVER_VERSION" ]; \
  then CHROME_MAJOR_VERSION=$(google-chrome --version | sed -E "s/.* ([0-9]+)(\.[0-9]+){3}.*/\1/") \
    && CHROME_DRIVER_VERSION=$(wget --no-verbose -O - "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_MAJOR_VERSION}"); \
  fi \
  && echo "Using chromedriver version: "$CHROME_DRIVER_VERSION \
  && wget --no-verbose -O /tmp/chromedriver_linux64.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
  && rm -rf /opt/selenium/chromedriver \
  && unzip /tmp/chromedriver_linux64.zip -d /opt/chromedriver \
  && rm /tmp/chromedriver_linux64.zip \
  && chmod 755 /opt/chromedriver/chromedriver \
  && ln -fs /opt/chromedriver/chromedriver/usr/bin/chromedriver

# FIREFOX
RUN FIREFOX_DOWNLOAD_URL=$(if [ $FIREFOX_VERSION = "latest" ] || [ $FIREFOX_VERSION = "nightly-latest" ] || [ $FIREFOX_VERSION = "devedition-latest" ] || [ $FIREFOX_VERSION = "esr-latest" ]; then echo "https://download.mozilla.org/?product=firefox-$FIREFOX_VERSION-ssl&os=linux64&lang=en-US"; else echo "https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.bz2"; fi) \
  && wget --no-verbose -O /tmp/firefox.tar.bz2 $FIREFOX_DOWNLOAD_URL \
  && tar -C /opt -xjf /tmp/firefox.tar.bz2 \
  && rm /tmp/firefox.tar.bz2 \
  && mv /opt/firefox /opt/firefox-$FIREFOX_VERSION \
  && ln -fs /opt/firefox-$FIREFOX_VERSION /opt/firefox \
  && ln -fs /opt/firefox/firefox /usr/bin/firefox

RUN GK_VERSION=$(if [ ${GECKODRIVER_VERSION:-latest} = "latest" ]; then echo "0.27.0"; else echo $GECKODRIVER_VERSION; fi) \
  && echo "Using GeckoDriver version: "$GK_VERSION \
  && wget --no-verbose -O /tmp/geckodriver.tar.gz https://github.com/mozilla/geckodriver/releases/download/v$GK_VERSION/geckodriver-v$GK_VERSION-linux64.tar.gz \
  && tar -C /opt -zxf /tmp/geckodriver.tar.gz \
  && rm /tmp/geckodriver.tar.gz \
  && chmod +x /opt/geckodriver \
  && ln -fs /opt/geckodriver /usr/bin/geckodriver

# ENTRYPOINT
COPY ./tanaguru-entrypoint-docker.sh /
RUN chmod +x /tanaguru-entrypoint-docker.sh

# TANAGURU
ADD $TANAGURU_CLI_ARCHIVE_PATH /opt
RUN mkdir /opt/tanaguru-cli/config
RUN cp /opt/tanaguru-resources/src/main/resources/* /opt/tanaguru-cli/config

ENTRYPOINT ["bash","tanaguru-entrypoint-docker.sh"]