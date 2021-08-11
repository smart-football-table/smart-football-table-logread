# smart-football-table-logread

[![Build Status](https://travis-ci.org/smart-football-table/smart-football-table-logread.svg?branch=master)](https://travis-ci.org/smart-football-table/smart-football-table-logread)
[![BCH compliance](https://bettercodehub.com/edge/badge/smart-football-table/smart-football-table-logread?branch=master)](https://bettercodehub.com/)
[![codecov](https://codecov.io/gh/smart-football-table/smart-football-table-logread/branch/master/graph/badge.svg)](https://codecov.io/gh/smart-football-table/smart-football-table-logread)
[![Maintainability](https://api.codeclimate.com/v1/badges/c59e78943b41b5f3329a/maintainability)](https://codeclimate.com/github/smart-football-table/smart-football-table-logread/maintainability)
[![Known Vulnerabilities](https://snyk.io/test/github/smart-football-table/smart-football-table-logread/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/smart-football-table/smart-football-table-logread?targetFile=pom.xml)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=smart-football-table/smart-football-table-logread)](https://dependabot.com)
[![GitLicense](https://gitlicense.com/badge/smart-football-table/smart-football-table-ledcontrol)](https://gitlicense.com/license/smart-football-table/smart-football-table-ledcontrol)

How to generate logs

mosquitto_sub -v -t '#' -F "%U %t %p" > tee sft-mqtt.log

~~mosquitto_sub -v -t '#' | xargs -d$'\n' -L1 sh -c 'date "+%T.%6N $0"' 2>&1 | tee sft-mqtt.log~~
