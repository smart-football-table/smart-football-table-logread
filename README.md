# smart-football-table-logread

How to generate logs
mosquitto_sub -v -t '#' | xargs -d$'\n' -L1 sh -c 'date "+%T.%6N $0"' 2>&1 | tee sft-mqtt.log
