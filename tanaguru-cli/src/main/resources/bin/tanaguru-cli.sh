#!/bin/bash
cd "$(dirname "$0")"
java -jar ../lib/tanaguru-cli-*.jar \
  "$@" \
  --spring.config.location="../config/" \
  --spring.config.name="common,audit-runner" \
  --spring.profiles.active="prod" \
  --logging.file="../logs/tanaguru-cli.log" > /dev/null &