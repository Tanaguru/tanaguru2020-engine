#!/bin/bash
cd "$(dirname "$0")"
java -Dspring.config.location="../../../../../tanaguru-resources/src/main/resources/" -Dspring.config.name="common,audit-runner,rest" \
  -Dspring.profiles.active="dev" -jar ../../../../target/tanaguru-cli-*.jar \
  "$@" -page "https://docs.python.org/3/howto/functional.html" -private -webdriverBrowser chrome