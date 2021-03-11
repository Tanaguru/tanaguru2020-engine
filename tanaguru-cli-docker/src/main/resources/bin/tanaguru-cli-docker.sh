#!/bin/bash
cd "$(dirname "$0")"
java -Dspring.config.location="../../../../../tanaguru-resources/src/main/resources/" -Dspring.config.name="common,audit-docker,audit-runner,rest" \
  -Dspring.profiles.active="dev" -jar ../../../../target/tanaguru-cli-docker-*.jar \
  "$@"