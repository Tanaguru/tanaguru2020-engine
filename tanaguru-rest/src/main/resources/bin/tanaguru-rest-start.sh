#!/bin/bash
cd "$(dirname "$0")"
java -jar ../lib/tanaguru-rest-*.jar \
  -Djava.awt.headless=true \
  -Djsse.enableSNIExtension=false \
  --spring.config.location="../config/" \
  --spring.config.name="common,audit-runner,account,rest,oauth2" \
  --logging.file="../logs/tanaguru-rest.log" > /dev/null &
echo "$!" > pid.txt
