# smart-football-table-logread

[![Build Status](https://travis-ci.org/smart-football-table/smart-football-table-logread.svg?branch=master)](https://travis-ci.org/smart-football-table/smart-football-table-logread)
[![BCH compliance](https://bettercodehub.com/edge/badge/smart-football-table/smart-football-table-logread?branch=master)](https://bettercodehub.com/)

How to generate logs
mosquitto_sub -v -t '#' | xargs -d$'\n' -L1 sh -c 'date "+%T.%6N $0"' 2>&1 | tee sft-mqtt.log
