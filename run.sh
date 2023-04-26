#!/usr/bin/env bash

java -Dconfiguration.location=./config/application.properties -Dlogback.configurationFile=./config/logback.xml -jar doge.jar