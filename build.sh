#!/usr/bin/env bash

rm doge.jar
./gradlew clean
./gradlew shadowJar
mv doge/build/libs/doge.jar doge.jar